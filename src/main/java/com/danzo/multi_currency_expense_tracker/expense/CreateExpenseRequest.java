package com.danzo.multi_currency_expense_tracker.expense;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotBlank(message = "please provide description")
        @Size(max = 250,message = "maximum size is 250")
        String description,

        @Positive(message = "amount must be positive")
        @NotNull(message = "amount can not be null")
        Long amountCents,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}" , message = "currency must be a 3-letter code like USD")
        String currency,

        @NotNull
        @PastOrPresent(message = "the date must be now or in the past")
        LocalDate spentOn,

        @NotNull
        Category category
) {
}
