package com.danzo.multi_currency_expense_tracker.expense;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String description,
        Long amountCents,
        String currency,
        long amountEurCents,
        BigDecimal rateUsed,
        LocalDate spentOn,
        Category category,
        Instant createdAt

) {
    public static ExpenseResponse from(Expense expense){
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmountCents(),
                expense.getCurrency(),
                expense.getAmountEurCents(),
                expense.getRateUsed(),
                expense.getSpentOn(),
                expense.getCategory(),
                expense.getCreatedAt()
        );
    }
}
