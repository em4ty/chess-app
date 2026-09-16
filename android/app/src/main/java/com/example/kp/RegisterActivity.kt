package com.example.kp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kp.RegisterRequest
import com.example.kp.RetrofitClient
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
        } else if (password.length < 4) {
            findViewById<EditText>(R.id.etPassword).error = "Пароль должен содержать минимум 4 символа"
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
        lifecycleScope.launch {
            try {
                // Показать прогресс
                findViewById<Button>(R.id.btnRegister).isEnabled = false
                findViewById<Button>(R.id.btnRegister).text = "Регистрация..."

                // Выполнить запрос к Backend
                val response = RetrofitClient.instance.register(
                    RegisterRequest(
                        username = username,
                        password = password,
                        email = if (email.isNotEmpty()) email else null
                    )
                )

                // Успешная регистрация
                runOnUiThread {
                    findViewById<Button>(R.id.btnRegister).isEnabled = true
                    findViewById<Button>(R.id.btnRegister).text = "Зарегистрироваться"

                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация успешна! Добро пожаловать, $username!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Автоматически входим после регистрации
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("password", password)
                    startActivity(intent)
                    finish()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    findViewById<Button>(R.id.btnRegister).isEnabled = true
                    findViewById<Button>(R.id.btnRegister).text = "Зарегистрироваться"

                    val errorMessage = when {
                        e.message?.contains("already exists", ignoreCase = true) == true ->
                            "Пользователь с таким логином уже существует"
                        e.message?.contains("connect", ignoreCase = true) == true ->
                            "Не удалось подключиться к серверу. Проверьте интернет"
                        e.message?.contains("timeout", ignoreCase = true) == true ->
                            "Превышено время ожидания"
                        else -> "Ошибка регистрации: ${e.message}"
                    }

                    Toast.makeText(
                        this@RegisterActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}