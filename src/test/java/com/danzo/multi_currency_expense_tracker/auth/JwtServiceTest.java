package com.danzo.multi_currency_expense_tracker.auth;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-definitely-longer-than-32-chars";

    private final JwtService jwtService = new JwtService(SECRET, Duration.ofHours(1));

    @Test
    void roundTrip_returnsSameEmail() {
        String token = jwtService.generateToken("test@mail.com");

        String email = jwtService.extractEmail(token);

        assertEquals("test@mail.com", email);
    }
    @Test
    void tamperedToken_isRejected() {
        String token = jwtService.generateToken("test@mail.com");

        int index = token.indexOf('.') + 5;
        char original = token.charAt(index);
        char replacement = (original == 'A') ? 'B' : 'A';
        String tampered = token.substring(0, index) + replacement + token.substring(index + 1);

        assertThrows(JwtException.class, () -> jwtService.extractEmail(tampered));
    }
    @Test
    void tokenSignedWithOtherKey_isRejected() {
        JwtService otherService = new JwtService(
                "a-completely-different-secret-also-over-32-chars", Duration.ofHours(1));
        String token = otherService.generateToken("test@mail.com");

        assertThrows(JwtException.class, () -> jwtService.extractEmail(token));
    }

    @Test
    void expiredToken_isRejected() {
        JwtService expiringService = new JwtService(SECRET, Duration.ofSeconds(-1));
        String token = expiringService.generateToken("test@mail.com");

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractEmail(token));
    }
}