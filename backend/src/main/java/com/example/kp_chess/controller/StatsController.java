package com.example.kp_chess.controller;

import com.example.kp_chess.dto.StatisticsResponse;
import com.example.kp_chess.dto.UpdateStatsRequest;
import com.example.kp_chess.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
@Tag(name = "Statistics", description = "User statistics endpoints")
public class StatsController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get user statistics", description = "Returns statistics for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User or statistics not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getStats(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        try {
            StatisticsResponse stats = userService.getUserStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Update user statistics", description = "Updates statistics for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateStats(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,

            @Parameter(description = "Statistics update data", required = true)
            @RequestBody UpdateStatsRequest request) {

        try {
            StatisticsResponse updatedStats = userService.updateUserStats(userId, request);
            return ResponseEntity.ok(updatedStats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Add correct answer", description = "Increments correct answers count")
    @ApiResponse(responseCode = "200", description = "Statistics updated successfully")
    @PostMapping("/{userId}/add-correct")
    public ResponseEntity<?> addCorrectAnswer(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        try {
            StatisticsResponse updatedStats = userService.addCorrectAnswer(userId);
            return ResponseEntity.ok(updatedStats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Add completed exercise", description = "Increments completed exercises count")
    @ApiResponse(responseCode = "200", description = "Statistics updated successfully")
    @PostMapping("/{userId}/add-exercise")
    public ResponseEntity<?> addExerciseCompleted(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {

        try {
            StatisticsResponse updatedStats = userService.addExerciseCompleted(userId);
            return ResponseEntity.ok(updatedStats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        }
    }
}