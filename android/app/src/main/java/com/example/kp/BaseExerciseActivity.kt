package com.example.kp

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kp.databinding.ActivityExerciseBinding
import kotlinx.coroutines.launch

abstract class BaseExerciseActivity : AppCompatActivity() {

    protected lateinit var binding: ActivityExerciseBinding

    // Данные упражнения
    protected var currentLevel = 1
    protected val totalLevels = 5
    protected var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем userId
        val prefs = getSharedPreferences("chess_auth", MODE_PRIVATE)
        userId = prefs.getLong("user_id", 0)

        binding.chessBoard.exerciseMode = true

        setupUI()
        loadLevel(currentLevel)
        updateProgress()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnHint.setOnClickListener { showHint() }
        binding.btnReset.setOnClickListener { resetExercise() }
        binding.btnNext.setOnClickListener { nextLevel() }

        setupBoardListener()
    }

    private fun setupBoardListener() {
        binding.chessBoard.setMoveListener { move ->
            binding.tvStatus.text = "Ход черных: $move"
            binding.root.postDelayed({
                checkBoardMove(move)
            }, 500)
        }
    }

    private fun checkBoardMove(move: String) {
        if (isBoardMoveCorrect(move)) {
            handleCorrectAnswer()
        } else {
            handleWrongAnswer()
        }
    }

    private fun isBoardMoveCorrect(move: String): Boolean {
        val correctMoves = getCorrectAnswersForLevel(currentLevel)
        val moveTarget = move.takeLast(2).lowercase()

        return correctMoves.any { correctMove ->
            val correctTarget = correctMove.takeLast(2).lowercase()
            moveTarget == correctTarget
        }
    }

    private fun showHint() {
        val hint = getHintForLevel(currentLevel)
        AlertDialog.Builder(this)
            .setTitle("Подсказка для уровня $currentLevel")
            .setMessage(hint)
            .setPositiveButton("Понятно", null)
            .setNeutralButton("Показать ответ") { _, _ ->
                showCorrectAnswer()
            }
            .show()
    }

    private fun showCorrectAnswer() {
        val correctAnswersList = getCorrectAnswersForLevel(currentLevel)
        val answersText = if (correctAnswersList.size == 1) {
            "Правильный ход: ${correctAnswersList.first()}"
        } else {
            "Возможные правильные ходы:\n${correctAnswersList.joinToString("\n")}"
        }

        AlertDialog.Builder(this)
            .setTitle("Правильный ответ")
            .setMessage(answersText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun handleCorrectAnswer() {
        lifecycleScope.launch {
            try {
                // СРАЗУ отправляем на сервер, что упражнение выполнено
                if (userId > 0) {
                    // Добавляем выполненное упражнение
                    RetrofitClient.instance.addExerciseCompleted(userId)

                    // Добавляем правильный ответ
                    RetrofitClient.instance.addCorrectAnswer(userId)
                }

                runOnUiThread {
                    binding.tvStatus.text = "✅ Правильно!"
                    binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))

                    Toast.makeText(
                        this@BaseExerciseActivity,
                        "Правильно! Упражнение засчитано",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.root.postDelayed({
                        nextLevel()
                    }, 1500)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@BaseExerciseActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleWrongAnswer() {
        binding.tvStatus.text = "❌ Неверно. Попробуйте еще раз!"
        binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))

        Toast.makeText(this, "Неверный ход. Попробуйте еще раз!", Toast.LENGTH_SHORT).show()
    }

    private fun resetExercise() {
        loadLevel(currentLevel)
        binding.tvStatus.text = ""
    }

    private fun nextLevel() {
        if (currentLevel < totalLevels) {
            currentLevel++
            loadLevel(currentLevel)
            updateProgress()
            binding.tvStatus.text = ""
        } else {
            // Все уровни пройдены, начинаем заново
            currentLevel = 1
            loadLevel(currentLevel)
            updateProgress()

            AlertDialog.Builder(this)
                .setTitle("Упражнения завершены!")
                .setMessage("Вы прошли все 5 упражнений. Начинаем заново!")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    fun updateProgress() {
        binding.tvLevel.text = "Упражнение: $currentLevel/$totalLevels"
        binding.tvExerciseTitle.text = getExerciseTitle()
        binding.tvExerciseDescription.text = getLevelDescription(currentLevel)
    }

    abstract fun loadLevel(level: Int)
    abstract fun getCorrectAnswersForLevel(level: Int): List<String>
    abstract fun getHintForLevel(level: Int): String
    abstract fun getExerciseType(): String
    abstract fun getExerciseTitle(): String
    abstract fun getLevelDescription(level: Int): String

    protected fun setChessPosition(fen: String, question: String) {
        try {
            binding.chessBoard.setPositionFromFEN(fen)
            binding.chessBoard.clearMoveHistory()
        } catch (e: Exception) {
            binding.chessBoard.resetBoard()
        }

        binding.tvQuestion.text = "$question\n\nВы играете за ЧЕРНЫХ (фигуры вверху)"
        binding.tvStatus.text = ""
    }
}