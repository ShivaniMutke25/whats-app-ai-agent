package com.example.whatsappai.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class SpringAiChatService {
    private final ObjectProvider<ChatModel> chatModelProvider;
    public SpringAiChatService(ObjectProvider<ChatModel> chatModelProvider) { this.chatModelProvider = chatModelProvider; }
    public String generateResponse(String systemPrompt, String userPrompt) {
        ChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) return "I can help with shop and inventory questions. Configure OPENAI_API_KEY to enable AI-generated replies.";
        var response = chatModel.call(new Prompt(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) return "I could not generate a response right now.";
        return response.getResult().getOutput().getText();
    }
}
