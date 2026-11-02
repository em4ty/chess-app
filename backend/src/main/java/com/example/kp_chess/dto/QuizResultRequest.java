package com.example.kp_chess.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuizResultRequest(
        @NotNull(message = "correctAnswers is required")
        @Min(value = 0, message = "correctAnswers must be between 0 and 15")
        @Max(value = 15, message = "correctAnswers must be between 0 and 15")
        Integer correctAnswers
) {
}
