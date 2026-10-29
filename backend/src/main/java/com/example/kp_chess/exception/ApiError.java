package com.example.kp_chess.exception;

/** Единый формат тела ошибки: {"error": "..."} */
public record ApiError(String error) {
}
