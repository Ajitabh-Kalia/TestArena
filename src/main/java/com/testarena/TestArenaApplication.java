package com.testarena;

import org.springframework.ai.model.vertexai.autoconfigure.gemini.VertexAiGeminiChatAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
VertexAiGeminiChatAutoConfiguration.class}
)
public class TestArenaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestArenaApplication.class, args);
	}

}
