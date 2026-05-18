package gladhee.ruby.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public class MemoryDtos {

	public record WriteRequest(
			@NotBlank String sessionId,
			@NotBlank String role,      // user | assistant
			@NotBlank String text,
			Map<String, Object> extra
	) {
	}

	public record SearchRequest(
			@NotBlank String sessionId,
			@NotBlank String query,
			Integer topK,
			Double minScore
	) {
	}

	public record Item(String id, Double score, String text, Map<String, Object> metadata) {
	}

	public record SearchResponse(List<Item> items) {
	}

}
