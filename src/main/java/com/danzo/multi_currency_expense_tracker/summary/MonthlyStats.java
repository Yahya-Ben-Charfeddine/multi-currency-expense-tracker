package com.danzo.multi_currency_expense_tracker.summary;
import com.danzo.multi_currency_expense_tracker.expense.Category;
import com.danzo.multi_currency_expense_tracker.expense.ExpenseResponse;

import java.util.Map;

public record MonthlyStats(
        long totalEurCents,
        Map<Category, Long> byCategory,
        int count,
        ExpenseResponse biggest
) {
}
