package com.example.kp_chess.controller;

import com.example.kp_chess.dto.ExerciseCompletedRequest;
import com.example.kp_chess.dto.QuizResultRequest;
import com.example.kp_chess.dto.StatisticsResponse;
import com.example.kp_chess.exception.ApiException;
import com.example.kp_chess.security.AuthInterceptor;
import com.example.kp_chess.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "User statistics endpoints (require Bearer token)")
public class StatsController {

    private final UserService userService;

    public StatsController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user statistics", description = "Returns statistics of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Token belongs to another user"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<StatisticsResponse> getStats(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @RequestAttribute(AuthInterceptor.USER_ID_ATTR) Long authUserId) {
        requireOwner(userId, authUserId);
        return ResponseEntity.ok(userService.getUserStats(userId));
    }

    @Operation(summary = "Save quiz result",
            description = "Saves the number of correct quiz answers (0..15). The best result is kept; overall progress is computed by the server.")
    @PostMapping("/{userId}/quiz")
    public ResponseEntity<StatisticsResponse> saveQuizResult(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @RequestAttribute(AuthInterceptor.USER_ID_ATTR) Long authUserId,
            @Valid @RequestBody QuizResultRequest request) {
        requireOwner(userId, authUserId);
        return ResponseEntity.ok(userService.recordQuizResult(userId, request.correctAnswers()));
    }

    @Operation(summary = "Mark exercise as completed",
            description = "Marks one exercise (type + level 1..5) as completed. Repeating the same exercise does not increase the counter.")
    @PostMapping("/{userId}/exercise")
    public ResponseEntity<StatisticsResponse> completeExercise(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @RequestAttribute(AuthInterceptor.USER_ID_ATTR) Long authUserId,
            @Valid @RequestBody ExerciseCompletedRequest request) {
        requireOwner(userId, authUserId);
        return ResponseEntity.ok(
                userService.recordExerciseCompleted(userId, request.exerciseType(), request.level()));
    }

    /** Пользователь может читать и менять только собственную статистику. */
    private void requireOwner(Long pathUserId, Long authUserId) {
        if (!pathUserId.equals(authUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
