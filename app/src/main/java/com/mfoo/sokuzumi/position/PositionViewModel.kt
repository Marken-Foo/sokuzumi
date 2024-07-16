package com.mfoo.sokuzumi.position

import androidx.lifecycle.ViewModel
import com.mfoo.shogi.Col
import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Row
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class KomaOnBoard(
    val komaType: KomaType,
    val x: Int,
    val y: Int,
    val isUpsideDown: Boolean,
)

data class BoardState(
    val komas: List<KomaOnBoard>,
)

data class SquareXY(val x: Int, val y: Int)

data class PositionUiState(
    val board: BoardState,
    val selectedSq: SquareXY?,
)

// For Android lifecycle persistence across configuration change
class PositionViewModel : ViewModel() {
    private val _vm = PositionVM()
    private val _uiState =
        MutableStateFlow(
            PositionUiState(
                board = _vm.calculateVm(_vm.pos),
                selectedSq = null
            )
        )
    val uiState: StateFlow<PositionUiState> = _uiState.asStateFlow()

    fun onSquareClick(x: Int, y: Int) {
        _vm.onSquareClick(x, y)
        _uiState.update { currentState ->
            currentState.copy(
                board = _vm.calculateVm(_vm.pos),
                selectedSq = _vm.selectedSq?.let(_vm::squareToXY),
            )
        }
    }
}

// MVVM "ViewModel" in the app
class PositionVM() {
    var pos: Position = PositionImpl.empty()
        .setKoma(
            Square(Col(1), Row(1)),
            Koma(Side.GOTE, KomaType.KY)
        )
        .setKoma(
            Square(Col(1), Row(3)),
            Koma(Side.GOTE, KomaType.FU)
        )
        .setKoma(
            Square(Col(5), Row(9)),
            Koma(Side.SENTE, KomaType.OU)
        )
        .setKoma(
            Square(Col(8), Row(8)),
            Koma(Side.SENTE, KomaType.KA)
        )

    var selectedSq: Square? = null

    fun squareToXY(sq: Square): SquareXY {
        val numCols = 9
        return SquareXY(x = numCols - sq.col.int, y = sq.row.int - 1)
    }

    private fun xYToSquare(x: Int, y: Int): Square {
        val numCols = 9
        return Square(Col(numCols - x), Row(y + 1))
    }

    fun calculateVm(position: Position): BoardState {
        val allKomas = position.getAllKoma().map { (sq, koma) ->
            val (x, y) = squareToXY(sq)
            KomaOnBoard(koma.komaType, x, y, !koma.side.isSente())
        }
        return BoardState(allKomas)
    }

    fun onSquareClick(x: Int, y: Int) {
        val sq = xYToSquare(x, y)
        selectedSq = sq
        // toggle to test
        pos.getKoma(sq).map {
            pos = if (it == null) {
                pos.setKoma(sq, Koma(Side.GOTE, KomaType.TO))
            } else {
                pos.removeKoma(sq)
            }
        }
    }
}
