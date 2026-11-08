package com.danzo.multi_currency_expense_tracker.auth;

import java.util.UUID;

public record RegisterResponse(
        String email,
        UUID id
) {
}
