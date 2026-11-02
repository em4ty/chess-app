package com.example.kp_chess.service;

import com.example.kp_chess.dto.*;
import com.example.kp_chess.exception.ApiException;
import com.example.kp_chess.model.User;
import com.example.kp_chess.model.UserStatistics;
import com.example.kp_chess.repository.CompletedExerciseRepository;
import com.example.kp_chess.repository.UserRepository;
import com.example.kp_chess.repository.UserStatisticsRepository;
import com.example.kp_chess.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {

    /** В тесте 15 вопросов и 15 упражнений (3 типа по 5 уровней). */
    static final int MAX_QUIZ = 15;
    static final int MAX_EXERCISES = 15;

    private static final int BCRYPT_MAX_BYTES = 72;
    private static final String BAD_CREDENTIALS = "Invalid username or password";

    private final UserRepository userRepository;
    private final UserStatisticsRepository statisticsRepository;
    private final CompletedExerciseRepository completedExerciseRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository,
                       UserStatisticsRepository statisticsRepository,
                       CompletedExerciseRepository completedExerciseRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.statisticsRepository = statisticsRepository;
        this.completedExerciseRepository = completedExerciseRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail() == null || request.getEmail().isBlank()
                ? null : request.getEmail().trim();

        if (username.length() < 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username must be between 3 and 50 characters");
        }
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is too long");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User(username, passwordEncoder.encode(request.getPassword()), email);
        User savedUser = userRepository.save(user);
        statisticsRepository.save(new UserStatistics(savedUser));

        return toUserResponse(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String rawPassword = request.getPassword();
        // Одинаковая ошибка для "нет такого пользователя" и "неверный пароль" - не раскрываем, какие логины существуют
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS));

        if (!passwordMatches(user, rawPassword)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }

        UserStatistics statistics = getOrCreateStatistics(user);
        return new LoginResponse(
                tokenService.issue(user.getId()),
                toUserResponse(user),
                toStatisticsResponse(statistics)
        );
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getUserStats(Long userId) {
        UserStatistics statistics = statisticsRepository.findByUserId(userId).orElse(null);
        if (statistics == null) {
            // Нет строки статистики: либо нет пользователя (404), либо старая запись - тогда отдаём нули
            if (!userRepository.existsById(userId)) {
                throw userNotFound(userId);
            }
            return new StatisticsResponse(0, 0, 0, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return toStatisticsResponse(statistics);
    }

    /** Сохраняет результат теста. Засчитывается лучший результат, прогресс считается на сервере. */
    @Transactional
    public StatisticsResponse recordQuizResult(Long userId, int correctAnswers) {
        UserStatistics statistics = getOrCreateStatistics(findUser(userId));
        int best = Math.max(statistics.getCorrectAnswers(), clamp(correctAnswers, MAX_QUIZ));
        statistics.setCorrectAnswers(best);
        return saveRecalculated(statistics);
    }

    /** Отмечает упражнение пройденным. Повторное прохождение того же упражнения счётчик не увеличивает. */
    @Transactional
    public StatisticsResponse recordExerciseCompleted(Long userId, String exerciseType, int level) {
        UserStatistics statistics = getOrCreateStatistics(findUser(userId));
        completedExerciseRepository.insertIfAbsent(userId, exerciseType, level);
        long done = completedExerciseRepository.countByUserId(userId);
        statistics.setExercisesCompleted(clamp((int) done, MAX_EXERCISES));
        return saveRecalculated(statistics);
    }

    // ---------------------------------------------------------------- helpers

    private StatisticsResponse saveRecalculated(UserStatistics statistics) {
        int quiz = clamp(statistics.getCorrectAnswers(), MAX_QUIZ);
        int exercises = clamp(statistics.getExercisesCompleted(), MAX_EXERCISES);
        statistics.setTotalProgressPercent((quiz + exercises) * 100 / (MAX_QUIZ + MAX_EXERCISES));
        statistics.setLastUpdated(LocalDateTime.now());
        return toStatisticsResponse(statisticsRepository.save(statistics));
    }

    private boolean passwordMatches(User user, String rawPassword) {
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            return false;
        }
        String stored = user.getPasswordHash();
        if (stored.startsWith("$2")) { // BCrypt
            return passwordEncoder.matches(rawPassword, stored);
        }
        // Старые аккаунты, созданные до хеширования, хранят пароль открытым текстом:
        // проверяем и сразу переводим на BCrypt.
        boolean legacyMatch = MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8), rawPassword.getBytes(StandardCharsets.UTF_8));
        if (legacyMatch) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
        return legacyMatch;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> userNotFound(userId));
    }

    private UserStatistics getOrCreateStatistics(User user) {
        return statisticsRepository.findByUserId(user.getId())
                .orElseGet(() -> statisticsRepository.save(new UserStatistics(user)));
    }

    private ApiException userNotFound(Long userId) {
        return new ApiException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

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
