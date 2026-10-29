package com.example.kp_chess.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Подписанные токены без внешних библиотек: base64url("userId:expiresAt") + "." + base64url(HMAC-SHA256).
 * Подделать токен или продлить срок без знания AUTH_SECRET нельзя.
 */
@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();
    private static final String HMAC = "HmacSHA256";

    private final byte[] key;
    private final Duration ttl;

    public TokenService(@Value("${app.auth.secret:}") String secret,
                        @Value("${app.auth.token-ttl-hours:720}") long ttlHours) {
        if (secret == null || secret.length() < 32) {
            log.warn("AUTH_SECRET не задан или короче 32 символов: используется случайный ключ, "
                    + "после перезапуска все токены станут недействительными. Задайте AUTH_SECRET!");
            byte[] random = new byte[48];
            new SecureRandom().nextBytes(random);
            this.key = random;
        } else {
            this.key = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.ttl = Duration.ofHours(ttlHours);
    }

    public String issue(long userId) {
        long expiresAt = Instant.now().plus(ttl).getEpochSecond();
        String payload = ENC.encodeToString((userId + ":" + expiresAt).getBytes(StandardCharsets.UTF_8));
        return payload + "." + ENC.encodeToString(sign(payload));
    }

    /** Возвращает id пользователя или null, если токен неверный/просрочен. */
    public Long verify(String token) {
        if (token == null) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }
        try {
            byte[] given = DEC.decode(parts[1]);
            if (!MessageDigest.isEqual(sign(parts[0]), given)) {
                return null;
            }
            String[] payload = new String(DEC.decode(parts[0]), StandardCharsets.UTF_8).split(":");
            if (payload.length != 2) {
                return null;
            }
            long expiresAt = Long.parseLong(payload[1]);
            if (Instant.now().getEpochSecond() >= expiresAt) {
                return null;
            }
            return Long.parseLong(payload[0]);
        } catch (IllegalArgumentException e) { // плохой base64 или число
            return null;
        }
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            return mac.doFinal(data.getBytes(StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }
}
