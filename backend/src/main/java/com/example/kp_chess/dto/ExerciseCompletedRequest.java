package com.example.kp_chess.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ExerciseCompletedRequest(
        @NotNull(message = "exerciseType is required")
        @Pattern(regexp = "defense|attack|checkmate", message = "exerciseType must be defense, attack or checkmate")
        String exerciseType,

        @NotNull(message = "level is required")
        @Min(value = 1, message = "level must be between 1 and 5")
        @Max(value = 5, message = "level must be between 1 and 5")
        Integer level
) {
}
