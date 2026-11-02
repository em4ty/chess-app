package com.example.kp

import retrofit2.Response
import retrofit2.http.*

interface ChessApiService {
    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body user: RegisterRequest): Response<UserResponse>

    @POST("api/auth/login")
    suspend fun login(@Body user: LoginRequest): Response<LoginResponse>


    @GET("api/stats/{userId}")
    suspend fun getStats(@Path("userId") userId: Long): Response<StatisticsResponse>

    /** Результат теста: на сервере сохраняется лучший результат. */
    @POST("api/stats/{userId}/quiz")
    suspend fun saveQuizResult(
        @Path("userId") userId: Long,
        @Body request: QuizResultRequest
    ): Response<StatisticsResponse>

    /** Упражнение пройдено. Повторное прохождение того же упражнения счётчик не увеличивает. */
    @POST("api/stats/{userId}/exercise")
    suspend fun completeExercise(
        @Path("userId") userId: Long,
        @Body request: ExerciseCompletedRequest
    ): Response<StatisticsResponse>
}

// === DTO классы ===

// Для регистрации
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null
)

// Ответ от регистрации
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String?,
    val createdAt: String
)

// Запрос на логин
data class LoginRequest(
    val username: String,
    val password: String
)

// Ответ от логина
data class LoginResponse(
    val token: String,
    val user: UserResponse,
    val statistics: StatisticsResponse
)

// Статистика
data class StatisticsResponse(
    val correctAnswers: Int,
    val exercisesCompleted: Int,
    val totalProgressPercent: Int,
    val lastUpdated: String
)

// Результат теста (0..15 правильных ответов)
data class QuizResultRequest(val correctAnswers: Int)

// Пройденное упражнение: тип "defense" | "attack" | "checkmate" и уровень 1..5
data class ExerciseCompletedRequest(val exerciseType: String, val level: Int)

// Тело ошибки сервера: {"error": "..."}
data class ApiError(val error: String?)
