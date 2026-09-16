package com.example.kp_chess.service;

import com.example.kp_chess.dto.*;
import com.example.kp_chess.model.User;
import com.example.kp_chess.model.UserStatistics;
import com.example.kp_chess.repository.UserRepository;
import com.example.kp_chess.repository.UserStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStatisticsRepository statisticsRepository;

    // Проверка существования пользователя
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Проверка существования пользователя
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Создание пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword()); // Пароль без хэширования
        user.setEmail(request.getEmail());

        User savedUser = userRepository.save(user);

        // Создание статистики для пользователя
        UserStatistics statistics = new UserStatistics(savedUser);
        statisticsRepository.save(statistics);

        return toUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        // Найти пользователя
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверить пароль (простое сравнение)
        if (!request.getPassword().equals(user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        // Получить статистику
        UserStatistics statistics = statisticsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Statistics not found"));

        // Создать токен (упрощённо)
        String token = "token_" + user.getId() + "_" + System.currentTimeMillis();

        return new LoginResponse(
                token,
                toUserResponse(user),
                toStatisticsResponse(statistics)
        );
    }

    // ДОБАВЬТЕ ЭТИ МЕТОДЫ:

    public StatisticsResponse getUserStats(Long userId) {
        // Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        // Получаем статистику
        UserStatistics statistics = statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Statistics not found for user id: " + userId));

        return toStatisticsResponse(statistics);
    }

    @Transactional
    public StatisticsResponse updateUserStats(Long userId, UpdateStatsRequest request) {
        // Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        // Получаем статистику
        UserStatistics statistics = statisticsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Statistics not found for user id: " + userId));

        // Обновляем только переданные поля
        if (request.getCorrectAnswers() != null) {
            statistics.setCorrectAnswers(request.getCorrectAnswers());
        }

        if (request.getExercisesCompleted() != null) {
            statistics.setExercisesCompleted(request.getExercisesCompleted());
        }

        if (request.getTotalProgressPercent() != null) {
            // Проверяем, что процент в пределах 0-100
            int percent = Math.max(0, Math.min(100, request.getTotalProgressPercent()));
            statistics.setTotalProgressPercent(percent);
        }

        // Обновляем время последнего изменения
        statistics.setLastUpdated(LocalDateTime.now());

        UserStatistics updatedStats = statisticsRepository.save(statistics);
        return toStatisticsResponse(updatedStats);
    }

    // Вспомогательные методы для обновления статистики
    @Transactional
    public StatisticsResponse addCorrectAnswer(Long userId) {
        StatisticsResponse currentStats = getUserStats(userId);

        UpdateStatsRequest request = new UpdateStatsRequest();
        request.setCorrectAnswers(currentStats.getCorrectAnswers() + 1);

        return updateUserStats(userId, request);
    }

    @Transactional
    public StatisticsResponse addExerciseCompleted(Long userId) {
        StatisticsResponse currentStats = getUserStats(userId);

        UpdateStatsRequest request = new UpdateStatsRequest();
        request.setExercisesCompleted(currentStats.getExercisesCompleted() + 1);

        return updateUserStats(userId, request);
    }

    // Приватные методы преобразования

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private StatisticsResponse toStatisticsResponse(UserStatistics stats) {
        return new StatisticsResponse(
                stats.getCorrectAnswers(),
                stats.getExercisesCompleted(),
                stats.getTotalProgressPercent(),
                stats.getLastUpdated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}