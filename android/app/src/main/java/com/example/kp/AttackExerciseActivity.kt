package com.example.kp

import android.os.Bundle

class AttackExerciseActivity : BaseExerciseActivity() {

    private val attackExercises = listOf(
        Exercise(
            level = 1,
            fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
            question = "Белые сыграли e4. Как атаковать слабую пешку f2?",
            correctAnswers = listOf("d5", "c5", "e5"),
            hint = "Контролируйте центр или готовьте атаку."
        ),
        Exercise(
            level = 2,
            fen = "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
            question = "Как создать угрозу белым?",
            correctAnswers = listOf("d5", "Nc6", "Qh4"),
            hint = "Атакуйте центр или выводите ферзя."
        ),
        Exercise(
            level = 3,
            fen = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 0 3",
            question = "Найдите тактический удар.",
            correctAnswers = listOf("Nxe4", "d5", "Bg4"),
            hint = "Тактический удар конем может быть выигрышным."
        ),
        Exercise(
            level = 4,
            fen = "r1bqkbnr/ppp2ppp/2np4/4p3/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 0 4",
            question = "Создайте сильную атаку.",
            correctAnswers = listOf("Bb4+", "Nf6", "Qe7"),
            hint = "Шах слоном может создать проблемы."
        ),
        Exercise(
            level = 5,
            fen = "r1bqk1nr/ppp2ppp/2np4/2b1p3/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 2 5",
            question = "Мастерская атака. Найдите комбинацию.",
            correctAnswers = listOf("Bb4+", "Nxe4", "Qh4"),
            hint = "Шах слоном Bb4+ создает тактические возможности."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.tvExerciseTitle.text = "Атака на короля (черные)"
        binding.tvExerciseDescription.text = "Вы играете за черных"
        updateProgress()
    }

    override fun loadLevel(level: Int) {
        val exercise = attackExercises[level - 1]
        setChessPosition(exercise.fen, exercise.question)
    }

    override fun getCorrectAnswersForLevel(level: Int): List<String> {
        return attackExercises[level - 1].correctAnswers
    }

    override fun getHintForLevel(level: Int): String {
        return attackExercises[level - 1].hint
    }

    override fun getExerciseType(): String = "attack"

    override fun getExerciseTitle(): String = "Атака на короля (черные)"

    override fun getLevelDescription(level: Int): String {
        return when (level) {
            1 -> "Базовый: Начало атаки"
            2 -> "Средний: Создание угроз"
            3 -> "Продвинутый: Тактические удары"
            4 -> "Эксперт: Атака с шахом"
            5 -> "Мастер: Комбинационная атака"
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