package com.example.kp

import android.os.Bundle

class DefenseExerciseActivity : BaseExerciseActivity() {

    private val defenseExercises = listOf(
        Exercise(
            level = 1,
            fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
            question = "Белые сыграли e4. Как черным защитить своего короля?",
            correctAnswers = listOf("e5", "e6", "d6", "c5"),
            hint = "Можно ответить e5, контролируя центр."
        ),
        Exercise(
            level = 2,
            fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
            question = "Белые сыграли Nf3. Как защитить пешку e5?",
            correctAnswers = listOf("d6", "Nc6", "Qe7"),
            hint = "Можно защитить пешку d6 или развить коня."
        ),
        Exercise(
            level = 3,
            fen = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 3 3",
            question = "Как защитить короля от возможных атак?",
            correctAnswers = listOf("d6", "Be7", "Nf6"),
            hint = "Развивайте фигуры и защищайте короля."
        ),
        Exercise(
            level = 4,
            fen = "r1bqkbnr/ppp2ppp/2np4/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 0 4",
            question = "Белые угрожают атакой. Найдите лучшую защиту.",
            correctAnswers = listOf("Be7", "Nf6", "g6"),
            hint = "Выведите слона или коня для защиты."
        ),
        Exercise(
            level = 5,
            fen = "r1bqk1nr/ppp2ppp/2np4/2b1p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 2 5",
            question = "Сложная защита. Найдите безопасный ход.",
            correctAnswers = listOf("Nf6", "d5", "Qe7"),
            hint = "Развивайте фигуры: вывод коня g8 подготовит рокировку."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvExerciseTitle.text = "Защита короля (черные)"
        binding.tvExerciseDescription.text = "Вы играете за черных"
        updateProgress()
    }

    override fun loadLevel(level: Int) {
        val exercise = defenseExercises[level - 1]
        setChessPosition(exercise.fen, exercise.question)
    }

    override fun getCorrectAnswersForLevel(level: Int): List<String> {
        return defenseExercises[level - 1].correctAnswers
    }

    override fun getHintForLevel(level: Int): String {
        return defenseExercises[level - 1].hint
    }

    override fun getExerciseType(): String = "defense"

    override fun getExerciseTitle(): String = "Защита короля (черные)"

    override fun getLevelDescription(level: Int): String {
        return when (level) {
            1 -> "Базовый: Ответ на e4"
            2 -> "Средний: Защита центра"
            3 -> "Продвинутый: Развитие фигур"
            4 -> "Эксперт: Защита короля"
            5 -> "Мастер: Сложная защита"
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