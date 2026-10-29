package com.example.kp_chess.exception;

import org.springframework.http.HttpStatus;

/** Ошибка с понятным HTTP-статусом (404, 409, 401...) вместо безликого 500. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
