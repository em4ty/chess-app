package com.example.kp_chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Одно пройденное упражнение пользователя. (user, type, level) уникальны на уровне БД. */
@Entity
@Table(name = "completed_exercises")
public class CompletedExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exercise_type", nullable = false, length = 20)
    private String exerciseType;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt = LocalDateTime.now();

    public CompletedExercise() {}

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getExerciseType() { return exerciseType; }
    public Integer getLevel() { return level; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
