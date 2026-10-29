package com.example.kp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class PiecesFragment : Fragment() {

    // Список фигур с их данными
    private val pieces = listOf(
        Piece(
            name = "Пешка",
            imageRes = R.drawable.ic_pawn,
            moveDiagramRes = R.drawable.pawn_move,
            description = "Пешка ходит вперед на одну клетку, но бьет по диагонали. При первом ходе может пойти на две клетки вперед. Достигнув последней горизонтали, превращается в любую другую фигуру."
        ),
        Piece(
            name = "Ладья",
            imageRes = R.drawable.ic_rook,
            moveDiagramRes = R.drawable.rook_move,
            description = "Ладья ходит по горизонталям и вертикалям на любое количество клеток. Участвует в рокировке с королем."
        ),
        Piece(
            name = "Конь",
            imageRes = R.drawable.ic_knight,
            moveDiagramRes = R.drawable.knight_move,
            description = "Конь ходит буквой 'Г': на две клетки в одном направлении и на одну в перпендикулярном. Единственная фигура, которая может 'перепрыгивать' через другие."
        ),
        Piece(
            name = "Слон",
            imageRes = R.drawable.ic_bishop,
            moveDiagramRes = R.drawable.bishop_move,
            description = "Слон ходит по диагоналям на любое количество клеток. Игрок имеет двух слонов: одного на светлых полях, другого на темных."
        ),
        Piece(
            name = "Ферзь",
            imageRes = R.drawable.ic_queen,
            moveDiagramRes = R.drawable.queen_move,
            description = "Ферзь - самая сильная фигура. Ходит как ладья и слон вместе: по горизонталям, вертикалям и диагоналям."
        ),
        Piece(
            name = "Король",
            imageRes = R.drawable.ic_king,
            moveDiagramRes = R.drawable.king_move,
            description = "Король ходит на одну клетку в любом направлении. Самая важная фигура - если королю объявлен мат, партия проиграна."
        )
    )

    private var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pieces, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updatePieceInfo()

        // Обработка кнопки "Назад"
        view.findViewById<View>(R.id.btn_previous).setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                updatePieceInfo()
            }
        }

        // Обработка кнопки "Вперед"
        view.findViewById<View>(R.id.btn_next).setOnClickListener {
            if (currentPosition < pieces.size - 1) {
                currentPosition++
                updatePieceInfo()
            }
        }

        // Обработка кнопки "Закрыть урок" - возврат на главный экран с фигур
        view.findViewById<View>(R.id.btn_home).setOnClickListener {
            // Возвращаемся к фрагменту с основами
            parentFragmentManager.commit {
                replace(R.id.fragment_container, BasicsFragment())
                // Не добавляем в back stack, чтобы не возвращаться к просмотру фигур
            }
        }
    }

    private fun updatePieceInfo() {
        val piece = pieces[currentPosition]

        view?.apply {
            findViewById<android.widget.TextView>(R.id.tv_piece_name).text = piece.name
            findViewById<android.widget.TextView>(R.id.tv_piece_description).text = piece.description
            findViewById<android.widget.TextView>(R.id.tv_position).text = "${currentPosition + 1}/${pieces.size}"

            findViewById<android.widget.ImageView>(R.id.iv_piece).setImageResource(piece.imageRes)
            findViewById<android.widget.ImageView>(R.id.iv_move_diagram).setImageResource(piece.moveDiagramRes)

            // Показываем/скрываем кнопки в зависимости от позиции
            findViewById<View>(R.id.btn_previous).isEnabled = currentPosition > 0
            findViewById<View>(R.id.btn_next).isEnabled = currentPosition < pieces.size - 1
        }
    }

    // Класс для хранения данных о фигуре
    data class Piece(
        val name: String,
        val imageRes: Int,
        val moveDiagramRes: Int,
        val description: String
    )
}