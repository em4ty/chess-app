package com.example.kp_chess.dto;

public class LoginResponse {
    private String token;
    private UserResponse user;
    private StatisticsResponse statistics;

    public LoginResponse() {}

    // Конструктор
    public LoginResponse(String token, UserResponse user, StatisticsResponse statistics) {
        this.token = token;
        this.user = user;
        this.statistics = statistics;
    }

    // Геттеры и сеттеры
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public StatisticsResponse getStatistics() { return statistics; }
    public void setStatistics(StatisticsResponse statistics) { this.statistics = statistics; }
}