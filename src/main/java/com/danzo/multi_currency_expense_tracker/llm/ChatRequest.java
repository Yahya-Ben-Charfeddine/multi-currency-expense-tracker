package com.danzo.multi_currency_expense_tracker.llm;

import java.util.List;

public record ChatRequest(
        String model,
        List<ChatMessage> messages
) {
}
