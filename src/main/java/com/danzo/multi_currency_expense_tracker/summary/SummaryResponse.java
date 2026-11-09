package com.danzo.multi_currency_expense_tracker.summary;

import com.danzo.multi_currency_expense_tracker.expense.Category;

import java.time.YearMonth;
import java.util.Map;

public record SummaryResponse(
        YearMonth month,
        long totalEurCents,
        Map<Category, Long> byCategory,
        AiInsight ai
) {
}
