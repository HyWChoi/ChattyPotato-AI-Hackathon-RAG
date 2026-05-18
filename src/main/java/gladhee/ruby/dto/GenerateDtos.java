package gladhee.ruby.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class GenerateDtos {

	public record Message(String role, String content) {
	}

	public record GenerateRequest(@NotEmpty List<Message> messages, String model) {
	}

	public record GenerateResponse(String provider, String text, Usage usage, Double costUsd) {
	}

	public record Usage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
		public static Usage of(Integer p, Integer c) {
			return new Usage(p, c, p != null && c != null ? p + c : null);
		}
	}

}
