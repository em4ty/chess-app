package com.example.kp

import android.os.Bundle

class CheckmateExerciseActivity : BaseExerciseActivity() {

    // Во всех позициях ходят черные и есть ровно один матовый ход.
    private val checkmateExercises = listOf(
        Exercise(
            level = 1,
            fen = "rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq - 0 2",
            question = "Мат в 1 ход. Белые ослабили позицию короля ходами f3 и g4.",
            correctAnswers = listOf("Qh4#"),
            hint = "Ферзь d8 по диагонали d8-h4 бьёт короля e1, а закрыться или съесть ферзя нечем."
        ),
        Exercise(
            level = 2,
            fen = "4r1k1/5ppp/8/8/8/8/5PPP/6K1 b - - 0 1",
            question = "Мат в 1 ход. Король белых заперт собственными пешками.",
            correctAnswers = listOf("Re1#"),
            hint = "Ладья по открытой линии «e» может дойти до первой горизонтали."
        ),
        Exercise(
            level = 3,
            fen = "8/8/8/8/8/5k2/1q6/7K b - - 0 1",
            question = "Мат в 1 ход. Ферзь и король против короля.",
            correctAnswers = listOf("Qg2#"),
            hint = "Ферзь должен встать рядом с белым королём так, чтобы его защищал ваш король."
        ),
        Exercise(
            level = 4,
            fen = "7k/8/8/8/8/1r6/r7/6K1 b - - 0 1",
            question = "Мат в 1 ход. Две ладьи против короля.",
            correctAnswers = listOf("Rb1#"),
            hint = "Одна ладья перекрывает вторую горизонталь, другая даёт шах по первой («лесенка»)."
        ),
        Exercise(
            level = 5,
            fen = "7k/8/8/8/4n3/8/6PP/6RK b - - 0 1",
            question = "Мат в 1 ход. Белый король окружён своими фигурами.",
            correctAnswers = listOf("Nf2#"),
            hint = "Конь ходит «буквой Г» и бьёт короля h1, а король не может ни уйти, ни съесть коня."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvExerciseTitle.text = "Матовые практики (черные)"
        binding.tvExerciseDescription.text = "Вы играете за черных"
        updateProgress()
    }

    override fun loadLevel(level: Int) {
        val exercise = checkmateExercises[level - 1]
        setChessPosition(exercise.fen, exercise.question)
    }

    override fun getCorrectAnswersForLevel(level: Int): List<String> {
        return checkmateExercises[level - 1].correctAnswers
    }

    override fun getHintForLevel(level: Int): String {
        return checkmateExercises[level - 1].hint
    }

    override fun getExerciseType(): String = "checkmate"

    override fun getExerciseTitle(): String = "Матовые практики (черные)"

    override fun getLevelDescription(level: Int): String {
        return when (level) {
            1 -> "Базовый: Детский мат наоборот"
            2 -> "Средний: Мат по последней линии"
            3 -> "Продвинутый: Ферзь и король"
            4 -> "Эксперт: Мат двумя ладьями"
            5 -> "Мастер: Удушающий мат конём"
            else -> "Упражнение $level"
        }
    }

    data class Exercise(
        val level: Int,
        val fen: String,
        val question: String,
        val correctAnswers: List<String>,
        val hint: String
    )
}