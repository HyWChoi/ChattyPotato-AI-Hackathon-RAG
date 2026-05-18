package gladhee.ruby.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		// spring.ai.openai.chat.options.* 에서 기본 모델/옵션을 읽어 빌드됨
		return builder.build();
	}
}
