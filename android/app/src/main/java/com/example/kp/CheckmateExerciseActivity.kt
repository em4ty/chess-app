package com.example.kp

import android.os.Bundle

class CheckmateExerciseActivity : BaseExerciseActivity() {

    private val checkmateExercises = listOf(
        Exercise(
            level = 1,
            fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
            question = "Мат в 1 ход. Найдите выигрывающий ход за черных.",
            correctAnswers = listOf("Qh4#"),
            hint = "Ферзь может поставить мат на h4."
        ),
        Exercise(
            level = 2,
            fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
            question = "Мат в 2 хода. Первый ход за черных?",
            correctAnswers = listOf("d5", "e5", "c5"),
            hint = "Начните с контроля центра."
        ),
        Exercise(
            level = 3,
            fen = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 3 3",
            question = "Классическая ловушка. Найдите матовую комбинацию.",
            correctAnswers = listOf("Nxe4", "Qh4"),
            hint = "Тактический удар конем может привести к быстрому мату."
        ),
        Exercise(
            level = 4,
            fen = "r1bqkb1r/pppp1ppp/2n2n2/4p3/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 0 4",
            question = "Мат в 3 хода. Найдите первый ход за черных.",
            correctAnswers = listOf("Nxe4", "Bb4+"),
            hint = "Начните с тактического удара."
        ),
        Exercise(
            level = 5,
            fen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 2 5",
            question = "Классический мат черными. Найдите завершающий ход.",
            correctAnswers = listOf("Qh4", "Nxe4"),
            hint = "Ферзь или конь могут поставить красивый мат."
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
            1 -> "Базовый: Мат в 1 ход"
            2 -> "Средний: Мат в 2 хода"
            3 -> "Продвинутый: Тактические ловушки"
            4 -> "Эксперт: Мат в 3 хода"
            5 -> "Мастер: Классические маты"
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