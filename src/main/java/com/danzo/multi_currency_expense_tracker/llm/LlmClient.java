package com.danzo.multi_currency_expense_tracker.llm;

import com.danzo.multi_currency_expense_tracker.common.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class LlmClient {
    private final RestClient llmRestClient;
    private final String model;

    public LlmClient(@Qualifier(value = "llmRestClient") RestClient llmRestClient,
                    @Value("${llm.model}") String model) {
        this.llmRestClient = llmRestClient;
        this.model=model;
    }
    public String complete(String systemPrompt, String userPrompt){
        List<ChatMessage> messages = List.of(
                new ChatMessage("system",systemPrompt),
                new ChatMessage("user",userPrompt)
        );
        ChatRequest request = new ChatRequest(model, messages);
        ChatResponse response = llmRestClient.post().uri("/chat/completions").body(request).retrieve().body(ChatResponse.class);
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("LLM returned no choices");
        }
        return  response.choices().get(0).message().content();



    }
}
