package com.danzo.multi_currency_expense_tracker.fx;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
public class FxTestController {
    private final FxClient fxClient;

    public FxTestController(FxClient fxClient) {
        this.fxClient = fxClient;
    }

    @GetMapping("/api/fx/test")
    public BigDecimal test(@RequestParam String currency, @RequestParam LocalDate date) {
        return fxClient.rateToEur(currency, date);
    }
}
