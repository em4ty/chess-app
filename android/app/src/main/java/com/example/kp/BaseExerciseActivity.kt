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

    // true, пока верный ответ засчитан и идёт переход на следующий уровень
    private var awaitingNext = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем userId
        userId = AuthStore.userId()

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
        if (awaitingNext) return
        if (isBoardMoveCorrect(move)) {
            handleCorrectAnswer()
        } else {
            handleWrongAnswer()
        }
    }

    private fun isBoardMoveCorrect(move: String): Boolean {
        // Сравниваем фигуру и поле назначения, а не только поле: "Qh4#" и "Nxe4" должны распознаваться
        return MoveMatcher.matches(
            move,
            binding.chessBoard.lastMovedPieceType,
            getCorrectAnswersForLevel(currentLevel)
        )
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
        awaitingNext = true
        val solvedLevel = currentLevel
        lifecycleScope.launch {
            try {
                // Сообщаем серверу, какое именно упражнение пройдено: повтор не накручивает счётчик
                var saved = true
                if (userId > 0) {
                    val response = RetrofitClient.instance.completeExercise(
                        userId,
                        ExerciseCompletedRequest(getExerciseType(), solvedLevel)
                    )
                    saved = response.isSuccessful
                }

                runOnUiThread {
                    binding.tvStatus.text = "✅ Правильно!"
                    binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))

                    Toast.makeText(
                        this@BaseExerciseActivity,
                        if (saved) "Правильно! Упражнение засчитано" else "Правильно! Но прогресс сохранить не удалось",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.root.postDelayed({
                        nextLevel()
                    }, 1500)
                }
            } catch (e: Exception) {
                // Нет сети: ответ верный, просто прогресс не сохранился - не блокируем обучение
                runOnUiThread {
                    binding.tvStatus.text = "✅ Правильно!"
                    Toast.makeText(
                        this@BaseExerciseActivity,
                        "Правильно! Но нет связи с сервером, прогресс не сохранён",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.root.postDelayed({ nextLevel() }, 1500)
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
        awaitingNext = false
        loadLevel(currentLevel)
        binding.tvStatus.text = ""
    }

    private fun nextLevel() {
        awaitingNext = false
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