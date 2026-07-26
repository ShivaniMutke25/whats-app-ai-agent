package com.example.whatsappai.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;

@Configuration
public class SpringAiConfig {

    @Bean
    @Primary
    public ChatModel chatModel(OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }
}
