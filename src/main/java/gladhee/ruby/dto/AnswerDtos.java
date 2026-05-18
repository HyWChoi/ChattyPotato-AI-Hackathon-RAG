// Replace package with your project's base package if needed
package gladhee.ruby.dto;

import java.util.List;
import java.util.Map;

public class AnswerDtos {

    public record Citation(String id, Double score, String snippet) {}

    public record AnswerRequest(
            String sessionId,
            String query,
            Integer topK,      // default 10
            Integer rerankK,   // default 5
            Double minScore,   // optional
            String modelCheap, // optional override
            String modelExpensive // optional override
    ) {}

    public record AnswerResponse(
            String rewritten,
            String route,
            double confidence,
            String answer,
            List<Citation> citations,
            Long latencyMs,
            Double costUsd,
            Map<String, Object> debug // retrieval lists, raw scores, etc.
    ) {}
}
