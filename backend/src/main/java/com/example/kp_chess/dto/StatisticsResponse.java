package com.example.kp_chess.dto;

public class StatisticsResponse {
    private Integer correctAnswers;
    private Integer exercisesCompleted;
    private Integer totalProgressPercent;
    private String lastUpdated;

    public StatisticsResponse() {}

    // Конструктор
    public StatisticsResponse(Integer correctAnswers, Integer exercisesCompleted,
                              Integer totalProgressPercent, String lastUpdated) {
        this.correctAnswers = correctAnswers;
        this.exercisesCompleted = exercisesCompleted;
        this.totalProgressPercent = totalProgressPercent;
        this.lastUpdated = lastUpdated;
    }

    // Геттеры и сеттеры
    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Integer getExercisesCompleted() { return exercisesCompleted; }
    public void setExercisesCompleted(Integer exercisesCompleted) { this.exercisesCompleted = exercisesCompleted; }

    public Integer getTotalProgressPercent() { return totalProgressPercent; }
    public void setTotalProgressPercent(Integer totalProgressPercent) { this.totalProgressPercent = totalProgressPercent; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}