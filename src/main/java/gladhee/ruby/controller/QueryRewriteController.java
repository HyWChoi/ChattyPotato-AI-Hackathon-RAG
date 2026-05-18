package gladhee.ruby.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import gladhee.ruby.dto.QueryRewriteDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/v1/query", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class QueryRewriteController {
	private final VectorStore vectorStore;            // memory 인덱스에서 top-k
	private final ChatClient chatClient;              // 저가 모델로 옵션 override
	private final ObjectMapper om = new ObjectMapper();

	@Value("${app.rewrite.model:gpt-4o-mini}")
	private String defaultModel;

	@PostMapping("/rewrite")
	public QueryRewriteDtos.RewriteResponse rewrite(@Valid @RequestBody QueryRewriteDtos.RewriteRequest req) {
		int k = Optional.ofNullable(req.topK()).orElse(6);

		// 1) 세션 범위에서 의미기억 top‑k 수집
		String filter = "bucket == 'memory' && session_id == '" + req.sessionId() + "'";
		var hits = vectorStore.similaritySearch(
				SearchRequest.builder().query(req.query()).topK(k).filterExpression(filter).build()
		);
		var support = hits.stream().map(d -> new QueryRewriteDtos.Candidate(d.getId(), d.getScore(), d.getText())).toList();

		// 2) 프롬프트 구성 (자기완결형, 짧게, JSON only)
		StringBuilder sb = new StringBuilder();
		sb.append("You are a query rewriting assistant. ")
						.append("Rewrite the user's query into a short, self-contained form (<= 30 tokens). ")
										.append("Use the SimilarMemory if helpful. DO NOT answer, only rewrite. ")
														.append("Return strict JSON: {\"rewritten\": string, \"confidence\": number, \"route_hint\": \"cheap\"|\"expensive\\");
																sb.append("SimilarMemory: ");
		for (int i=0; i<Math.min(5, support.size()); i++) {
			var c = support.get(i);
			sb.append("- ").append(c.text()).append(" ");
		}
		if (req.recentWindow()!=null && !req.recentWindow().isEmpty()) {
			sb.append(" RecentWindow: ");
			req.recentWindow().forEach(m -> sb.append(m.role()).append(": ").append(m.content()).append(" "));
		}
		sb.append(" UserQuery: ").append(req.query()).append(" ");

		// 3) 저가형 모델 호출 (요청별 override > 기본값)
		String model = (req.model()!=null && !req.model().isBlank()) ? req.model() : defaultModel;
		String json = chatClient.prompt()
						.system("You output JSON only.")
						.user(sb.toString())
//						.options(o -> o.model(model).temperature(0.2))
				.call().content();

		// 4) 파싱 및 안전한 폴백
		String rewritten = req.query();
		double conf = 0.6; String hint = "cheap"; // defaults
		try {
			var map = om.readValue(json, Map.class);
			Object r = map.get("rewritten"); if (r instanceof String s && !s.isBlank()) rewritten = s;
			Object c = map.get("confidence"); if (c instanceof Number n) conf = n.doubleValue();
			Object h = map.get("route_hint"); if (h instanceof String s && !s.isBlank()) hint = s;
		} catch (Exception ignore) {}

		return new QueryRewriteDtos.RewriteResponse(rewritten, support, conf, hint);
	}
}