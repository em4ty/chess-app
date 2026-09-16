package com.example.kp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.kp.databinding.ActivityQuizBinding
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var questions: List<Question>
    private var currentQuestionIndex = 0
    private var userAnswers = mutableMapOf<Int, Int>()
    private var userResults = mutableMapOf<Int, Boolean>()

    private var totalCorrectInQuiz = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Тест по основам шахмат"

        initializeQuestions()
        setupQuestion()
        setupClickListeners()

        totalCorrectInQuiz = 0
    }

    private fun initializeQuestions() {
        questions = listOf(
            Question("Как называется эта фигура?", R.drawable.ic_pawn,
                listOf("Пешка", "Ладья", "Конь", "Слон"), 0),
            Question("Как ходит конь?", null,
                listOf("По прямой", "Буквой Г", "По диагонали", "На одну клетку"), 1),
            Question("Сколько клеток на шахматной доске?", null,
                listOf("81", "64", "100", "32"), 1),
            Question("Какая фигура самая сильная?", null,
                listOf("Король", "Ладья", "Ферзь", "Конь"), 2),
            Question("Как называется ситуация, когда королю некуда ходить?", null,
                listOf("Мат", "Шах", "Пат", "Рокировка"), 2),
            Question("Какой ход могут сделать король и ладья вместе?", null,
                listOf("Взятие на проходе", "Превращение пешки", "Рокировка", "Шах"), 2),
            Question("Сколько пешек у каждого игрока в начале игры?", null,
                listOf("6", "10", "8", "4"), 2),
            Question("Как бьет пешка?", null,
                listOf("По прямой", "Как конь", "По диагонали", "В любую сторону"), 2),
            Question("Что такое 'энпассан'?", null,
                listOf("Особая рокировка", "Шах конем", "Взятие на проходе", "Ничья"), 2),
            Question("Можно ли поставить шах самому себе?", null,
                listOf("Да", "Только в пате", "Нет", "Только в эндшпиле"), 2),
            Question("Сколько клеток проходит слон за ход?", null,
                listOf("Всегда одну", "Всегда две", "Любое количество по диагонали", "Только на краю"), 2),
            Question("Что делает пешка, достигнув последней горизонтали?", null,
                listOf("Возвращается назад", "Исчезает", "Становится королем", "Превращается"), 3),
            Question("Можно ли рокироваться через шах?", null,
                listOf("Да", "Только длинная", "Только короткая", "Нет"), 3),
            Question("Сколько фигур, кроме короля и пешек, у каждого игрока?", null,
                listOf("5", "8", "7", "6"), 2),
            Question("Что такое 'мат'?", null,
                listOf("Король сам под шахом", "Все фигуры съедены", "Патовая ситуация", "Королю некуда уйти от шаха"), 3)
        )
    }

    private fun setupQuestion() {
        val question = questions[currentQuestionIndex]

        binding.tvCurrentQuestion.text = "Вопрос ${currentQuestionIndex + 1} из ${questions.size}"
        binding.tvQuestion.text = question.text

        if (question.imageRes != null) {
            binding.ivQuestionImage.setImageResource(question.imageRes)
            binding.ivQuestionImage.visibility = View.VISIBLE
        } else {
            binding.ivQuestionImage.visibility = View.GONE
        }

        binding.tvAnswer1.text = question.answers[0]
        binding.tvAnswer2.text = question.answers[1]
        binding.tvAnswer3.text = question.answers[2]
        binding.tvAnswer4.text = question.answers[3]

        resetAnswerCards()

        val selectedAnswer = userAnswers[currentQuestionIndex]
        if (selectedAnswer != null) {
            highlightSelectedAnswer(selectedAnswer)
        }
    }

    private fun setupClickListeners() {
        binding.cardAnswer1.setOnClickListener { onAnswerSelected(0) }
        binding.cardAnswer2.setOnClickListener { onAnswerSelected(1) }
        binding.cardAnswer3.setOnClickListener { onAnswerSelected(2) }
        binding.cardAnswer4.setOnClickListener { onAnswerSelected(3) }

        binding.btnAnswer.setOnClickListener {
            val selectedAnswer = userAnswers[currentQuestionIndex]
            if (selectedAnswer == null) {
                Toast.makeText(this, "Выберите ответ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isCorrect = selectedAnswer == questions[currentQuestionIndex].correctAnswer
            userResults[currentQuestionIndex] = isCorrect

            if (isCorrect) {
                totalCorrectInQuiz++
            }

            showAnswerResult(isCorrect, selectedAnswer, questions[currentQuestionIndex].correctAnswer)

            binding.root.postDelayed({
                nextQuestion()
            }, 1000)
        }

        binding.btnPrevious.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                setupQuestion()
            }
        }

        binding.btnNext.setOnClickListener {
            nextQuestion()
        }
    }

    private fun onAnswerSelected(answerIndex: Int) {
        userAnswers[currentQuestionIndex] = answerIndex
        highlightSelectedAnswer(answerIndex)
        binding.btnAnswer.isEnabled = true
    }

    private fun highlightSelectedAnswer(answerIndex: Int) {
        resetAnswerCards()
        val selectedColor = ContextCompat.getColor(this, R.color.primary)

        when (answerIndex) {
            0 -> binding.cardAnswer1.setCardBackgroundColor(selectedColor)
            1 -> binding.cardAnswer2.setCardBackgroundColor(selectedColor)
            2 -> binding.cardAnswer3.setCardBackgroundColor(selectedColor)
            3 -> binding.cardAnswer4.setCardBackgroundColor(selectedColor)
        }
    }

    private fun showAnswerResult(isCorrect: Boolean, selectedAnswer: Int, correctAnswer: Int) {
        val correctColor = ContextCompat.getColor(this, android.R.color.holo_green_dark)
        val wrongColor = ContextCompat.getColor(this, android.R.color.holo_red_dark)

        when (correctAnswer) {
            0 -> binding.cardAnswer1.setCardBackgroundColor(correctColor)
            1 -> binding.cardAnswer2.setCardBackgroundColor(correctColor)
            2 -> binding.cardAnswer3.setCardBackgroundColor(correctColor)
            3 -> binding.cardAnswer4.setCardBackgroundColor(correctColor)
        }

        if (!isCorrect) {
            when (selectedAnswer) {
                0 -> if (correctAnswer != 0) binding.cardAnswer1.setCardBackgroundColor(wrongColor)
                1 -> if (correctAnswer != 1) binding.cardAnswer2.setCardBackgroundColor(wrongColor)
                2 -> if (correctAnswer != 2) binding.cardAnswer3.setCardBackgroundColor(wrongColor)
                3 -> if (correctAnswer != 3) binding.cardAnswer4.setCardBackgroundColor(wrongColor)
            }
        }
    }

    private fun resetAnswerCards() {
        val defaultColor = ContextCompat.getColor(this, android.R.color.white)
        binding.cardAnswer1.setCardBackgroundColor(defaultColor)
        binding.cardAnswer2.setCardBackgroundColor(defaultColor)
        binding.cardAnswer3.setCardBackgroundColor(defaultColor)
        binding.cardAnswer4.setCardBackgroundColor(defaultColor)
    }

    private fun nextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            setupQuestion()
            binding.btnAnswer.isEnabled = false
        } else {
            showResults()
        }
    }

    private fun showResults() {
        val totalQuestions = questions.size
        val correctAnswers = totalCorrectInQuiz
        val percentage = (correctAnswers.toFloat() / totalQuestions * 100).toInt()

        // Отправляем на сервер РЕЗУЛЬТАТ ТЕСТА (а не добавляем к существующим)
        sendTestResultsToServer(correctAnswers)

        AlertDialog.Builder(this)
            .setTitle("Тест завершен!")
            .setMessage("Правильных ответов: $correctAnswers из $totalQuestions\nРезультат: $percentage%")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .show()
    }

    private fun sendTestResultsToServer(correctAnswers: Int) {
        lifecycleScope.launch {
            try {
                val authPrefs = getSharedPreferences("chess_auth", MODE_PRIVATE)
                val userId = authPrefs.getLong("user_id", 0)
                val token = authPrefs.getString("auth_token", null)

                if (userId > 0 && token != null) {
                    // ВАЖНО: Обновляем статистику теста, а не добавляем к ней

                    // 1. Получаем текущую статистику
                    val currentStatsResponse = RetrofitClient.instance.getStats(userId)

                    if (currentStatsResponse.isSuccessful && currentStatsResponse.body() != null) {
                        val currentStats = currentStatsResponse.body()!!

                        // 2. Обновляем только правильные ответы теста
                        // Вычитаем старые и добавляем новые
                        val newCorrectAnswers = correctAnswers // Просто результат текущего теста

                        // 3. Обновляем на сервере
                        val updateRequest = UpdateStatsRequest(
                            correctAnswers = newCorrectAnswers,
                            exercisesCompleted = currentStats.exercisesCompleted,
                            totalProgressPercent = calculateTotalProgress(newCorrectAnswers, currentStats.exercisesCompleted)
                        )

                        RetrofitClient.instance.updateStats(userId, updateRequest)
                    } else {
                        // Если статистики нет, создаем новую
                        val updateRequest = UpdateStatsRequest(
                            correctAnswers = correctAnswers,
                            exercisesCompleted = 0,
                            totalProgressPercent = calculateTotalProgress(correctAnswers, 0)
                        )

                        RetrofitClient.instance.updateStats(userId, updateRequest)
                    }
                }
            } catch (e: Exception) {
                println("Ошибка отправки результатов теста: ${e.message}")
            }
        }
    }

    private fun calculateTotalProgress(correctAnswers: Int, exercisesCompleted: Int): Int {
        val totalPossible = 30 // 15 вопросов теста + 15 упражнений
        val totalCompleted = minOf(correctAnswers, 15) + minOf(exercisesCompleted, 15)

        return if (totalPossible > 0) {
            (totalCompleted * 100) / totalPossible
        } else 0
    }

    data class Question(
        val text: String,
        val imageRes: Int?,
        val answers: List<String>,
        val correctAnswer: Int
    )
}