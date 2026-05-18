// Replace package with your project's base package if needed
package gladhee.ruby.service;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RetrievalService {
    private final VectorStore vectorStore;

    public record Retrieved(Document doc, Double score) {}

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Retrieved> retrieve(String sessionId, String query, int topK, Double minScore) {
        String filter = "bucket == 'memory' && session_id == '" + sessionId + "'";
        var req = SearchRequest.query(query)
                .withFilterExpression(filter)
                .withTopK(topK);
        var results = vectorStore.similaritySearch(req);
        List<Retrieved> list = new ArrayList<>();
        for (Document d : results) {
            Double score = (Double)d.getMetadata().getOrDefault("ai_similarity_score", null);
            if (minScore == null || (score != null && score >= minScore)) {
                list.add(new Retrieved(d, score));
            }
        }
        return list;
    }
}
