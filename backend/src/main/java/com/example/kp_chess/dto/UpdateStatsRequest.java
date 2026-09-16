package com.example.kp_chess.dto;

public class UpdateStatsRequest {
    private Integer correctAnswers;
    private Integer exercisesCompleted;
    private Integer totalProgressPercent;

    // Конструкторы
    public UpdateStatsRequest() {}

    public UpdateStatsRequest(Integer correctAnswers, Integer exercisesCompleted, Integer totalProgressPercent) {
        this.correctAnswers = correctAnswers;
        this.exercisesCompleted = exercisesCompleted;
        this.totalProgressPercent = totalProgressPercent;
    }

    // Геттеры и сеттеры
    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getExercisesCompleted() {
        return exercisesCompleted;
    }

    public void setExercisesCompleted(Integer exercisesCompleted) {
        this.exercisesCompleted = exercisesCompleted;
    }

    public Integer getTotalProgressPercent() {
        return totalProgressPercent;
    }

    public void setTotalProgressPercent(Integer totalProgressPercent) {
        this.totalProgressPercent = totalProgressPercent;
    }
}