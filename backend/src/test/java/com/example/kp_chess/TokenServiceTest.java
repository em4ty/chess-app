package com.example.kp_chess;

import com.example.kp_chess.security.TokenService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef-test-secret";

    @Test
    void validTokenReturnsUserId() {
        TokenService service = new TokenService(SECRET, 1);
        assertEquals(42L, service.verify(service.issue(42L)));
    }

    @Test
    void tamperedPayloadIsRejected() {
        TokenService service = new TokenService(SECRET, 1);
        String[] parts = service.issue(1L).split("\\.");
        String forged = service.issue(2L).split("\\.")[0] + "." + parts[1];
        assertNull(service.verify(forged));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        TokenService other = new TokenService("another-secret-another-secret-123456", 1);
        TokenService service = new TokenService(SECRET, 1);
        assertNull(service.verify(other.issue(1L)));
    }

    @Test
    void expiredTokenIsRejected() {
        TokenService service = new TokenService(SECRET, 0);
        assertNull(service.verify(service.issue(1L)));
    }

    @Test
    void garbageIsRejected() {
        TokenService service = new TokenService(SECRET, 1);
        assertNull(service.verify(null));
        assertNull(service.verify(""));
        assertNull(service.verify("abc"));
        assertNull(service.verify("a.b.c"));
        assertNull(service.verify("!!!.???"));
    }

    @Test
    void issuedTokensDifferPerUser() {
        TokenService service = new TokenService(SECRET, 1);
        assertNotEquals(service.issue(1L), service.issue(2L));
    }
}
