package com.example.kp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                performLogin(username, password)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            try {
                findViewById<Button>(R.id.btnLogin).isEnabled = false
                findViewById<Button>(R.id.btnLogin).text = "Вход..."

                val response = RetrofitClient.instance.login(
                    LoginRequest(username, password)
                )

                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    if (loginResponse != null) {
                        // ВАЖНО: Сохраняем в ПРАВИЛЬНОМ месте
                        saveAuthData(loginResponse)

                        runOnUiThread {
                            Toast.makeText(
                                this@LoginActivity,
                                "✅ Вход успешен! Добро пожаловать, ${loginResponse.user.username}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            startMainActivity()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "❌ Ошибка входа: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "❌ Ошибка сети: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                runOnUiThread {
                    findViewById<Button>(R.id.btnLogin).isEnabled = true
                    findViewById<Button>(R.id.btnLogin).text = "Войти"
                }
            }
        }
    }

    private fun saveAuthData(response: LoginResponse) {
        // ВАЖНО: Сохраняем ВСЕ данные правильно
        val prefs = getSharedPreferences("chess_auth", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("auth_token", response.token)
            putLong("user_id", response.user.id)
            putString("username", response.user.username)
            response.user.email?.let { putString("email", it) }
            // Также сохраняем статистику из ответа
            putInt("correct_answers", response.statistics.correctAnswers)
            putInt("exercises_completed", response.statistics.exercisesCompleted)
            putInt("total_progress", response.statistics.totalProgressPercent)
            apply()
        }

        // ДЕБАГ: Проверим что сохранилось
        println("DEBUG: Сохранены данные для user_id: ${response.user.id}")
        println("DEBUG: Токен: ${response.token.take(10)}...")
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}