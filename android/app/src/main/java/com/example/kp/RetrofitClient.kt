package com.example.kp

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /** Адрес сервера задаётся в android/gradle.properties (API_BASE_URL), а не в коде. */
    private val baseUrl: String = BuildConfig.API_BASE_URL.let { if (it.endsWith("/")) it else "$it/" }

    /** false, пока в gradle.properties остался адрес-заглушка. */
    val isConfigured: Boolean = !baseUrl.contains("YOUR-SERVER")

    /** Подставляет токен в каждый запрос; при 401 на защищённых методах сбрасывает устаревший вход. */
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val builder = request.newBuilder()
        AuthStore.token()?.let { builder.header("Authorization", "Bearer $it") }
        val response = chain.proceed(builder.build())
        if (response.code == 401 && !request.url.encodedPath.startsWith("/api/auth/")) {
            AuthStore.clear()
        }
        response
    }

    val instance: ChessApiService by lazy {
        // В логах только строки запросов; тела (там пароли) и токены не пишем
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            // Бесплатные хостинги "просыпаются" до минуты после простоя
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChessApiService::class.java)
    }
}

/** Достаёт текст ошибки из ответа сервера вида {"error": "..."}. */
object ApiErrors {
    fun message(response: Response<*>): String? = try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null else Gson().fromJson(body, ApiError::class.java)?.error
    } catch (e: Exception) {
        null
    }
}
