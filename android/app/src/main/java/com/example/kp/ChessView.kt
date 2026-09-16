package com.example.kp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boardSize = 8
    private var cellSize = 0f
    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val piecePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val movePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val capturePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var currentPlayer: Player = Player.BLACK  // По умолчанию черные ходят в упражнениях
    private var selectedPiece: Piece? = null
    private var selectedPosition: Pair<Int, Int>? = null
    private var possibleMoves = mutableListOf<Pair<Int, Int>>()

    // Режим упражнений: true = ходят черные, false = обычная игра
    var exerciseMode: Boolean = false

    private val board = Array(boardSize) { Array<Piece?>(boardSize) { null } }
    private val moveHistory = mutableListOf<String>()
    private var onMoveMadeListener: ((String) -> Unit)? = null

    init {
        setupPaints()
        resetBoard()  // Инициализируем стандартную доску
    }

    private fun setupPaints() {
        boardPaint.style = Paint.Style.FILL
        piecePaint.textAlign = Paint.Align.CENTER
        piecePaint.textSize = 40f

        selectedPaint.style = Paint.Style.FILL
        selectedPaint.color = Color.argb(128, 173, 216, 230)

        movePaint.style = Paint.Style.FILL
        movePaint.color = Color.argb(128, 144, 238, 144)

        capturePaint.style = Paint.Style.FILL
        capturePaint.color = Color.argb(128, 255, 182, 193)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cellSize = width / boardSize.toFloat()
        piecePaint.textSize = cellSize * 0.6f

        drawBoard(canvas)
        drawSelectedSquare(canvas)
        drawPossibleMoves(canvas)
        drawPieces(canvas)
        drawCoordinates(canvas)

    }



    private fun drawBoard(canvas: Canvas) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val x = col * cellSize
                val y = row * cellSize

                boardPaint.color = if ((row + col) % 2 == 0) {
                    Color.parseColor("#F0D9B5")
                } else {
                    Color.parseColor("#B58863")
                }

                canvas.drawRect(x, y, x + cellSize, y + cellSize, boardPaint)
            }
        }
    }

    private fun drawCoordinates(canvas: Canvas) {
        val coordPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        coordPaint.textSize = cellSize * 0.15f
        coordPaint.textAlign = Paint.Align.CENTER

        for (col in 0 until boardSize) {
            val x = col * cellSize + cellSize / 2
            val y = height - 5f
            coordPaint.color = if (col % 2 == 0) Color.BLACK else Color.WHITE
            canvas.drawText(('A' + col).toString(), x, y, coordPaint)
        }

        for (row in 0 until boardSize) {
            val x = 5f
            val y = row * cellSize + cellSize / 2 + coordPaint.textSize / 3
            coordPaint.color = if (row % 2 == 0) Color.WHITE else Color.BLACK
            canvas.drawText((8 - row).toString(), x, y, coordPaint)
        }
    }

    private fun drawSelectedSquare(canvas: Canvas) {
        selectedPosition?.let { (row, col) ->
            val x = col * cellSize
            val y = row * cellSize
            canvas.drawRect(x, y, x + cellSize, y + cellSize, selectedPaint)
        }
    }

    private fun drawPossibleMoves(canvas: Canvas) {
        for ((row, col) in possibleMoves) {
            val centerX = col * cellSize + cellSize / 2
            val centerY = row * cellSize + cellSize / 2
            val radius = cellSize / 4

            if (board[row][col] != null) {
                canvas.drawCircle(centerX, centerY, radius, capturePaint)
            } else {
                canvas.drawCircle(centerX, centerY, radius, movePaint)
            }
        }
    }

    private fun drawPieces(canvas: Canvas) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val piece = board[row][col] ?: continue
                val x = col * cellSize + cellSize / 2
                val y = row * cellSize + cellSize / 2 + piecePaint.textSize / 3

                piecePaint.color = if (piece.player == Player.WHITE) Color.WHITE else Color.BLACK

                val symbol = when (piece.type) {
                    PieceType.KING -> if (piece.player == Player.WHITE) "♔" else "♚"
                    PieceType.QUEEN -> if (piece.player == Player.WHITE) "♕" else "♛"
                    PieceType.ROOK -> if (piece.player == Player.WHITE) "♖" else "♜"
                    PieceType.BISHOP -> if (piece.player == Player.WHITE) "♗" else "♝"
                    PieceType.KNIGHT -> if (piece.player == Player.WHITE) "♘" else "♞"
                    PieceType.PAWN -> if (piece.player == Player.WHITE) "♙" else "♟"
                }

                canvas.drawText(symbol, x, y, piecePaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellSize).toInt()
            val row = (event.y / cellSize).toInt()

            if (col in 0 until boardSize && row in 0 until boardSize) {
                handleSquareClick(row, col)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleSquareClick(row: Int, col: Int) {
        val clickedPiece = board[row][col]

        if (selectedPosition == null) {
            // Выбор фигуры
            if (clickedPiece != null && clickedPiece.player == currentPlayer) {
                selectedPiece = clickedPiece
                selectedPosition = Pair(row, col)
                possibleMoves = calculatePossibleMoves(row, col, clickedPiece)
            } else {
                // Если кликнули не на свою фигуру или пустую клетку
                if (clickedPiece != null) {
                    Toast.makeText(context,
                        "Сейчас ходят ${if (currentPlayer == Player.WHITE) "белые" else "черные"}!",
                        Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Попытка сделать ход
            val (selectedRow, selectedCol) = selectedPosition!!
            val targetPos = Pair(row, col)

            if (targetPos in possibleMoves) {
                // Сохраняем ход в истории
                val fromSquare = "${'a' + selectedCol}${8 - selectedRow}"
                val toSquare = "${'a' + col}${8 - row}"
                val moveNotation = "$fromSquare$toSquare"
                moveHistory.add(moveNotation)

                // Выполняем ход
                board[row][col] = selectedPiece
                board[selectedRow][selectedCol] = null

                // Уведомляем слушателя
                onMoveMadeListener?.invoke(moveNotation)

                // В режиме упражнений НЕ меняем игрока
                if (!exerciseMode) {
                    currentPlayer = if (currentPlayer == Player.WHITE) Player.BLACK else Player.WHITE
                }

                Toast.makeText(context, "Ход сделан: $moveNotation", Toast.LENGTH_SHORT).show()
            }

            // Сбрасываем выбор
            selectedPiece = null
            selectedPosition = null
            possibleMoves.clear()
        }
    }

    private fun calculatePossibleMoves(
        row: Int,
        col: Int,
        piece: Piece
    ): MutableList<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()

        when (piece.type) {
            PieceType.PAWN -> calculatePawnMoves(row, col, piece, moves)
            PieceType.KNIGHT -> calculateKnightMoves(row, col, piece, moves)
            PieceType.BISHOP -> calculateBishopMoves(row, col, piece, moves)
            PieceType.ROOK -> calculateRookMoves(row, col, piece, moves)
            PieceType.QUEEN -> {
                calculateBishopMoves(row, col, piece, moves)
                calculateRookMoves(row, col, piece, moves)
            }
            PieceType.KING -> calculateKingMoves(row, col, piece, moves)
        }

        return moves
    }

    private fun calculatePawnMoves(
        row: Int,
        col: Int,
        pawn: Piece,
        moves: MutableList<Pair<Int, Int>>
    ) {
        val direction = if (pawn.player == Player.WHITE) -1 else 1

        // Ход вперед на одну клетку
        val forwardRow = row + direction
        if (isValidSquare(forwardRow, col) && board[forwardRow][col] == null) {
            moves.add(Pair(forwardRow, col))

            // Ход вперед на две клетки с начальной позиции
            val doubleRow = row + 2 * direction
            if ((pawn.player == Player.WHITE && row == 6) ||
                (pawn.player == Player.BLACK && row == 1)
            ) {
                if (board[doubleRow][col] == null) {
                    moves.add(Pair(doubleRow, col))
                }
            }
        }

        // Взятия по диагонали
        for (dc in intArrayOf(-1, 1)) {
            val captureRow = row + direction
            val captureCol = col + dc

            if (isValidSquare(captureRow, captureCol)) {
                val targetPiece = board[captureRow][captureCol]
                if (targetPiece != null && targetPiece.player != pawn.player) {
                    moves.add(Pair(captureRow, captureCol))
                }
            }
        }
    }

    private fun calculateKnightMoves(
        row: Int,
        col: Int,
        knight: Piece,
        moves: MutableList<Pair<Int, Int>>
    ) {
        val knightMoves = arrayOf(
            Pair(-2, -1), Pair(-2, 1),
            Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2),
            Pair(2, -1), Pair(2, 1)
        )

        for ((dr, dc) in knightMoves) {
            val newRow = row + dr
            val newCol = col + dc

            if (isValidSquare(newRow, newCol)) {
                val targetPiece = board[newRow][newCol]
                if (targetPiece == null || targetPiece.player != knight.player) {
                    moves.add(Pair(newRow, newCol))
                }
            }
        }
    }

    private fun calculateBishopMoves(
        row: Int,
        col: Int,
        bishop: Piece,
        moves: MutableList<Pair<Int, Int>>
    ) {
        val directions = arrayOf(
            Pair(-1, -1), Pair(-1, 1),
            Pair(1, -1), Pair(1, 1)
        )

        for ((dr, dc) in directions) {
            var currentRow = row + dr
            var currentCol = col + dc

            while (isValidSquare(currentRow, currentCol)) {
                val targetPiece = board[currentRow][currentCol]

                if (targetPiece == null) {
                    moves.add(Pair(currentRow, currentCol))
                } else {
                    if (targetPiece.player != bishop.player) {
                        moves.add(Pair(currentRow, currentCol))
                    }
                    break
                }

                currentRow += dr
                currentCol += dc
            }
        }
    }

    private fun calculateRookMoves(
        row: Int,
        col: Int,
        rook: Piece,
        moves: MutableList<Pair<Int, Int>>
    ) {
        val directions = arrayOf(
            Pair(-1, 0), Pair(1, 0),
            Pair(0, -1), Pair(0, 1)
        )

        for ((dr, dc) in directions) {
            var currentRow = row + dr
            var currentCol = col + dc

            while (isValidSquare(currentRow, currentCol)) {
                val targetPiece = board[currentRow][currentCol]

                if (targetPiece == null) {
                    moves.add(Pair(currentRow, currentCol))
                } else {
                    if (targetPiece.player != rook.player) {
                        moves.add(Pair(currentRow, currentCol))
                    }
                    break
                }

                currentRow += dr
                currentCol += dc
            }
        }
    }

    private fun calculateKingMoves(
        row: Int,
        col: Int,
        king: Piece,
        moves: MutableList<Pair<Int, Int>>
    ) {
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue

                val newRow = row + dr
                val newCol = col + dc

                if (isValidSquare(newRow, newCol)) {
                    val targetPiece = board[newRow][newCol]
                    if (targetPiece == null || targetPiece.player != king.player) {
                        moves.add(Pair(newRow, newCol))
                    }
                }
            }
        }
    }

    private fun isValidSquare(row: Int, col: Int): Boolean {
        return row in 0 until boardSize && col in 0 until boardSize
    }

    fun resetBoard() {
        // Стандартная расстановка
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                board[row][col] = null
            }
        }

        // Черные фигуры (верх доски, row 0-1)
        board[0][0] = Piece(PieceType.ROOK, Player.BLACK)
        board[0][7] = Piece(PieceType.ROOK, Player.BLACK)
        board[0][1] = Piece(PieceType.KNIGHT, Player.BLACK)
        board[0][6] = Piece(PieceType.KNIGHT, Player.BLACK)
        board[0][2] = Piece(PieceType.BISHOP, Player.BLACK)
        board[0][5] = Piece(PieceType.BISHOP, Player.BLACK)
        board[0][3] = Piece(PieceType.QUEEN, Player.BLACK)
        board[0][4] = Piece(PieceType.KING, Player.BLACK)
        for (col in 0 until 8) {
            board[1][col] = Piece(PieceType.PAWN, Player.BLACK)
        }

        // Белые фигуры (низ доски, row 6-7)
        board[7][0] = Piece(PieceType.ROOK, Player.WHITE)
        board[7][7] = Piece(PieceType.ROOK, Player.WHITE)
        board[7][1] = Piece(PieceType.KNIGHT, Player.WHITE)
        board[7][6] = Piece(PieceType.KNIGHT, Player.WHITE)
        board[7][2] = Piece(PieceType.BISHOP, Player.WHITE)
        board[7][5] = Piece(PieceType.BISHOP, Player.WHITE)
        board[7][3] = Piece(PieceType.QUEEN, Player.WHITE)
        board[7][4] = Piece(PieceType.KING, Player.WHITE)
        for (col in 0 until 8) {
            board[6][col] = Piece(PieceType.PAWN, Player.WHITE)
        }

        // В упражнениях ходят черные
        currentPlayer = if (exerciseMode) Player.BLACK else Player.WHITE
        selectedPiece = null
        selectedPosition = null
        possibleMoves.clear()
        moveHistory.clear()

        invalidate()
    }

    fun setPositionFromFEN(fen: String) {
        try {
            val parts = fen.trim().split("\\s+".toRegex())
            if (parts.isEmpty()) {
                resetBoard()
                return
            }

            val rows = parts[0].split("/")
            if (rows.size != 8) {
                resetBoard()
                return
            }

            // Очищаем доску
            for (row in 0 until boardSize) {
                for (col in 0 until boardSize) {
                    board[row][col] = null
                }
            }

            // Заполняем доску из FEN
            for (row in 0 until 8) {
                var col = 0
                for (char in rows[row]) {
                    if (char.isDigit()) {
                        col += char.toString().toInt()
                    } else {
                        val pieceType = when (char.lowercaseChar()) {
                            'k' -> PieceType.KING
                            'q' -> PieceType.QUEEN
                            'r' -> PieceType.ROOK
                            'b' -> PieceType.BISHOP
                            'n' -> PieceType.KNIGHT
                            'p' -> PieceType.PAWN
                            else -> continue
                        }
                        val player = if (char.isUpperCase()) Player.WHITE else Player.BLACK
                        board[row][col] = Piece(pieceType, player)
                        col++
                    }
                }
            }

            // В упражнениях ВСЕГДА ходят черные
            if (exerciseMode) {
                currentPlayer = Player.BLACK
            } else {
                // В обычном режиме читаем из FEN
                currentPlayer = if (parts.size > 1 && parts[1] == "b") Player.BLACK else Player.WHITE
            }

            selectedPiece = null
            selectedPosition = null
            possibleMoves.clear()
            moveHistory.clear()

            invalidate()

        } catch (e: Exception) {
            e.printStackTrace()
            resetBoard()
        }
    }

    fun getCurrentPlayer(): String = if (currentPlayer == Player.WHITE) "Белые" else "Черные"
    fun getMoveHistory(): List<String> = moveHistory.toList()
    fun getLastMove(): String? = moveHistory.lastOrNull()
    fun clearMoveHistory() { moveHistory.clear() }
    fun setMoveListener(listener: (String) -> Unit) { this.onMoveMadeListener = listener }

    data class Piece(val type: PieceType, val player: Player)

    enum class PieceType {
        KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
    }

    enum class Player {
        WHITE, BLACK
    }
}