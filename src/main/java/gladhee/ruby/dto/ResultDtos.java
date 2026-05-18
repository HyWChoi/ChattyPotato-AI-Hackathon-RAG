package gladhee.ruby.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public class ResultDtos {

	public record NeighborBrief(String id, double score) {
	}

	public record SaveResultRequest(
			@NotBlank String query,
			@NotBlank String route,      // cheap | expensive
			@NotBlank String provider,   // local-mini | openai | bedrock ...
			@NotBlank String answer,
			Double costUsd,
			Long latencyMs,
			List<NeighborBrief> neighbors,
			Map<String, Object> extra
	) {
	}

	public record SaveResultResponse(String logId) {
	}

}
