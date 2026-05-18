// Replace package with your project's base package if needed
package gladhee.ruby.service;

import app.rag.dto.AnswerDtos;
import app.rag.util.MMR;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnswerService {
    private final ChatClient chatClient;
    private final RetrievalService retrievalService;
    private final EsLoggingService esLoggingService; // assume you have this bean

    public AnswerService(ChatClient chatClient, RetrievalService retrievalService, EsLoggingService esLoggingService) {
        this.chatClient = chatClient;
        this.retrievalService = retrievalService;
        this.esLoggingService = esLoggingService;
    }

    public AnswerDtos.AnswerResponse answer(AnswerDtos.AnswerRequest req) {
        long t0 = System.nanoTime();
        int topK = Optional.ofNullable(req.topK()).orElse(10);
        int rerankK = Optional.ofNullable(req.rerankK()).orElse(5);

        // 1) rewrite (cheap model)
        var rewriteJson = chatClient.prompt()
                .system("Rewrite the user query for retrieval. Decide route_hint: cheap|expensive. " +
                        "Return JSON: {\"rewritten\":\"...\", \"confidence\":0.x, \"route_hint\":\"cheap|expensive\"}.")
                .user(req.query())
                .call()
                .content();
        String rewritten = req.query();
        double confidence = 0.6;
        String route = "cheap";
        try {
            var map = parseJson(rewriteJson);
            rewritten = (String) map.getOrDefault("rewritten", req.query());
            Object c = map.get("confidence");
            if (c instanceof Number n) confidence = n.doubleValue();
            Object rh = map.get("route_hint");
            if (rh instanceof String s) route = s;
        } catch (Exception ignore) {}

        // 2) retrieve
        var retrieved = retrievalService.retrieve(req.sessionId(), rewritten, topK, req.minScore());

        // 3) simple MMR (reuse scores; sim unknown -> assume 0 to prefer diversity by relevance only)
        double[] rel = new double[retrieved.size()];
        for (int i=0;i<rel.length;i++) rel[i] = Optional.ofNullable(retrieved.get(i).score()).orElse(0.0);
        var order = MMR.select(Math.min(rerankK, rel.length), 0.75, rel, null);
        var chosen = order.stream().map(retrieved::get).toList();

        // context
        StringBuilder ctx = new StringBuilder();
        for (var r : chosen) {
            var id = r.doc().getId();
            var text = r.doc().getContent();
            ctx.append("ID: ").append(id).append("\n").append(snippet(text)).append("\n---\n");
        }

        // 4) generate (route -> choose model via system hint only; actual model selection can be done via ChatClient options in your project)
        String sys = "You are a grounded assistant. Use ONLY the supplied CONTEXT. " +
                "If insufficient, say you don't know. Cite IDs. " +
                "Return JSON: {\"answer\":\"...\",\"citations\":[{\"id\":\"...\",\"snippet\":\"...\"}]}.";

        var answerJson = chatClient.prompt()
                .system(sys + " Route: " + route)
                .user("[QUERY]\n" + rewritten + "\n\n[CONTEXT]\n" + ctx)
                .call()
                .content();

        String answer = "정보가 부족해요.";
        List<AnswerDtos.Citation> cites = new ArrayList<>();
        try {
            var map = parseJson(answerJson);
            answer = (String) map.getOrDefault("answer", answer);
            Object arr = map.get("citations");
            if (arr instanceof List<?> l) {
                for (Object o : l) {
                    if (o instanceof Map<?,?> m) {
                        String id = (String)m.getOrDefault("id", "");
                        String sn = (String)m.getOrDefault("snippet", "");
                        cites.add(new AnswerDtos.Citation(id, null, sn));
                    }
                }
            }
        } catch (Exception ignore) {}

        // fall back citations: use our chosen docs
        if (cites.isEmpty()) {
            cites = chosen.stream().map(r -> new AnswerDtos.Citation(
                    r.doc().getId(),
                    r.score(),
                    snippet(r.doc().getContent())
            )).collect(Collectors.toList());
        }

        long latency = (System.nanoTime() - t0)/1_000_000;

        // 5) log (best-effort)
        try {
            Map<String,Object> extra = new HashMap<>();
            extra.put("timestamp", Instant.now().toString());
            extra.put("retrievedCount", retrieved.size());
            extra.put("chosenCount", chosen.size());
            extra.put("rewritten", rewritten);
            extra.put("route", route);
            esLoggingService.saveMap(extra);
        } catch (Exception ignore) {}

        Map<String,Object> debug = new HashMap<>();
        debug.put("retrievedIds", retrieved.stream().map(r -> r.doc().getId()).toList());
        debug.put("chosenIds", chosen.stream().map(r -> r.doc().getId()).toList());

        return new AnswerDtos.AnswerResponse(
                rewritten, route, confidence, answer, cites, latency, null, debug
        );
    }

    private static String snippet(String text) {
        if (text == null) return "";
        return text.length() <= 320 ? text : text.substring(0, 320) + "...";
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> parseJson(String json) {
        // For brevity in this patch, throw to force project to wire Jackson/ObjectMapper and replace.
        throw new RuntimeException("Replace with Jackson ObjectMapper in your project.");
    }
}
