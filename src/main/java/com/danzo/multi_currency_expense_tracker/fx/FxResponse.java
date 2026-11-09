package com.danzo.multi_currency_expense_tracker.fx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record FxResponse(
        BigDecimal amount,
        String base,
        LocalDate date,
        Map<String,BigDecimal> rates
) {
}
