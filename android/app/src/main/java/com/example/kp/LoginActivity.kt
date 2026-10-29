package com.example.kp

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

        // Уже входили: сразу открываем приложение
        if (AuthStore.isLoggedIn()) {
            startMainActivity()
            return
        }

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
        if (!RetrofitClient.isConfigured) {
            Toast.makeText(
                this,
                "Укажите адрес сервера: API_BASE_URL в android/gradle.properties",
                Toast.LENGTH_LONG
            ).show()
            return
        }
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
                    val message = ApiErrors.message(response)
                        ?: if (response.code() == 401) "Неверный логин или пароль" else "Ошибка входа: ${response.code()}"
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "❌ $message", Toast.LENGTH_LONG).show()
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
        AuthStore.save(response)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}