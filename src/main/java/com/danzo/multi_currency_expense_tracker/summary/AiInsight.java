package com.danzo.multi_currency_expense_tracker.summary;

import java.util.List;

public record AiInsight(
        String headline,
        List<String> unusualSpending,
        String savingTip
) {
}
