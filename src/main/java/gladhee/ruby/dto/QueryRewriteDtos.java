package gladhee.ruby.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class QueryRewriteDtos {
	public record WindowMsg(String role, String content) {}

	public record RewriteRequest(
			@NotBlank String sessionId,
			@NotBlank String query,
			Integer topK,
			List<WindowMsg> recentWindow, // 선택: 최근 N턴 원문
			String model // 선택: 요청별로 저가 모델 override
	) {}

	public record Candidate(String id, Double score, String text) {}
	public record RewriteResponse(String rewritten, List<Candidate> support, Double confidence, String routeHint) {}
}
