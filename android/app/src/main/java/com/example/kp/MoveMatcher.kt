package com.example.kp

/**
 * Сравнивает ход, сделанный на доске ("e7e5" + тип фигуры), с ожидаемыми ответами в шахматной
 * нотации ("e5", "Nf6", "Bb4+", "Qh4#", "Nxe4", "O-O").
 *
 * Раньше сравнивались последние два символа, поэтому ответы со знаками "+"/"#" и рокировка не совпадали
 * никогда, а любая фигура, попавшая на нужное поле, засчитывалась как верный ход.
 */
object MoveMatcher {

    fun matches(
        move: String,
        movedPiece: ChessBoardView.PieceType?,
        expectedAnswers: List<String>
    ): Boolean {
        if (move.length < 4 || movedPiece == null) return false
        val from = move.substring(0, 2).lowercase()
        val to = move.substring(2, 4).lowercase()
        return expectedAnswers.any { matchesOne(from, to, movedPiece, it) }
    }

    private fun matchesOne(
        from: String,
        to: String,
        piece: ChessBoardView.PieceType,
        answer: String
    ): Boolean {
        val san = answer.trim().trimEnd('+', '#', '!', '?')
        when (san) {
            "O-O", "0-0" -> return piece == ChessBoardView.PieceType.KING && from == "e8" && to == "g8"
            "O-O-O", "0-0-0" -> return piece == ChessBoardView.PieceType.KING && from == "e8" && to == "c8"
        }
        if (san.length < 2) return false

        val expectedPiece = when (san[0]) {
            'K' -> ChessBoardView.PieceType.KING
            'Q' -> ChessBoardView.PieceType.QUEEN
            'R' -> ChessBoardView.PieceType.ROOK
            'B' -> ChessBoardView.PieceType.BISHOP
            'N' -> ChessBoardView.PieceType.KNIGHT
            else -> ChessBoardView.PieceType.PAWN
        }
        return piece == expectedPiece && to == san.takeLast(2).lowercase()
    }
}
