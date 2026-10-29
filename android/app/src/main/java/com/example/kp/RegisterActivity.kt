package com.example.kp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Кнопка регистрации
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword).text.toString()

            if (validateInput(username, email, password, confirmPassword)) {
                performRegister(username, email, password)
            }
        }

        // Кнопка входа (переход к LoginActivity)
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Проверка логина
        if (username.isEmpty()) {
            findViewById<EditText>(R.id.etUsername).error = "Введите логин"
            isValid = false
        } else if (username.length < 3) {
            findViewById<EditText>(R.id.etUsername).error = "Логин должен содержать минимум 3 символа"
            isValid = false
        }

        // Проверка email (опционально)
        if (email.isNotEmpty() && !isValidEmail(email)) {
            findViewById<EditText>(R.id.etEmail).error = "Введите корректный email"
            isValid = false
        }

        // Проверка пароля
        if (password.isEmpty()) {
            findViewById<EditText>(R.id.etPassword).error = "Введите пароль"
            isValid = false
        } else if (password.length < 6) {
            findViewById<EditText>(R.id.etPassword).error = "Пароль должен содержать минимум 6 символов"
            isValid = false
        }

        // Проверка подтверждения пароля
        if (confirmPassword.isEmpty()) {
            findViewById<EditText>(R.id.etConfirmPassword).error = "Подтвердите пароль"
            isValid = false
        } else if (password != confirmPassword) {
            findViewById<EditText>(R.id.etConfirmPassword).error = "Пароли не совпадают"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    private fun performRegister(username: String, email: String, password: String) {
        if (!RetrofitClient.isConfigured) {
            Toast.makeText(
                this,
                "Укажите адрес сервера: API_BASE_URL в android/gradle.properties",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val button = findViewById<Button>(R.id.btnRegister)

        lifecycleScope.launch {
            try {
                button.isEnabled = false
                button.text = "Регистрация..."

                val response = RetrofitClient.instance.register(
                    RegisterRequest(
                        username = username,
                        password = password,
                        email = if (email.isNotEmpty()) email else null
                    )
                )

                // Retrofit не бросает исключение на 400/409, поэтому код ответа нужно проверять самим
                if (!response.isSuccessful) {
                    val message = ApiErrors.message(response) ?: "Ошибка регистрации: ${response.code()}"
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Сразу входим под только что созданным аккаунтом
                val loginResponse = RetrofitClient.instance.login(LoginRequest(username, password))
                val loginBody = loginResponse.body()
                if (loginResponse.isSuccessful && loginBody != null) {
                    AuthStore.save(loginBody)
                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация успешна! Добро пожаловать, $username!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(
                        Intent(this@RegisterActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация успешна. Теперь войдите в аккаунт",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                }
                finish()
            } catch (e: Exception) {
                val errorMessage = when {
                    e is java.net.SocketTimeoutException ->
                        "Превышено время ожидания. Сервер мог «просыпаться», попробуйте ещё раз"
                    e is java.io.IOException ->
                        "Не удалось подключиться к серверу. Проверьте интернет"
                    else -> "Ошибка регистрации: ${e.message}"
                }
                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
            } finally {
                button.isEnabled = true
                button.text = "Зарегистрироваться"
            }
        }
    }
}
