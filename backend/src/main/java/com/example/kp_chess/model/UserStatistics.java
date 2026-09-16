package com.example.kp_chess.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics")
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers = 0;

    @Column(name = "exercises_completed", nullable = false)
    private Integer exercisesCompleted = 0;

    @Column(name = "total_progress_percent", nullable = false)
    private Integer totalProgressPercent = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // Конструкторы
    public UserStatistics() {}

    public UserStatistics(User user) {
        this.user = user;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Integer getExercisesCompleted() { return exercisesCompleted; }
    public void setExercisesCompleted(Integer exercisesCompleted) { this.exercisesCompleted = exercisesCompleted; }

    public Integer getTotalProgressPercent() { return totalProgressPercent; }
    public void setTotalProgressPercent(Integer totalProgressPercent) { this.totalProgressPercent = totalProgressPercent; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}