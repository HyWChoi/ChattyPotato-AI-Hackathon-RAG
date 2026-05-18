package gladhee.ruby.controller;

import gladhee.ruby.dto.GenerateDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.ai.chat.prompt.Prompt;

@RestController
@RequestMapping(path = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class GenerateController {
	private final ChatClient chatClient; // 기본 모델은 application.yml 설정 따름

	@PostMapping("/generate")
	public Map<String,Object> generate(@Valid @RequestBody GenerateDtos.GenerateRequest req) {
		String user = req.messages().stream()
				.filter(m -> "user".equalsIgnoreCase(m.role()))
				.map(GenerateDtos.Message::content)
				.reduce("", (a,b) -> a + " " + b);

						String answer = chatClient.prompt()
								.system("You are a helpful assistant. Use Memory when relevant.")
								.user(user)
								.call().content();

		return Map.of("provider", "openai", "answer", answer);
	}
}