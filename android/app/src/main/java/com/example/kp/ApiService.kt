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

    @PUT("api/stats/{userId}")
    suspend fun updateStats(
        @Path("userId") userId: Long,
        @Body request: UpdateStatsRequest
    ): Response<StatisticsResponse>

    @POST("api/stats/{userId}/add-correct")
    suspend fun addCorrectAnswer(@Path("userId") userId: Long): Response<StatisticsResponse>

    @POST("api/stats/{userId}/add-exercise")
    suspend fun addExerciseCompleted(@Path("userId") userId: Long): Response<StatisticsResponse>
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

// ДОБАВЛЯЕМ: Запрос на обновление статистики
data class UpdateStatsRequest(
    val correctAnswers: Int? = null,
    val exercisesCompleted: Int? = null,
    val totalProgressPercent: Int? = null
)