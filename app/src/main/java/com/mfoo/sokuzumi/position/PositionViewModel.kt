package com.mfoo.sokuzumi.position

import androidx.lifecycle.ViewModel
import com.mfoo.shogi.Col
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Row
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class KomaOnBoard(
    val komaType: KomaType,
    val x: Int,
    val y: Int,
    val isUpsideDown: Boolean,
)

data class BoardState(
    val komas: List<KomaOnBoard>,
)

// For Android lifecycle persistence across configuration change
class PositionViewModel : ViewModel() {
    private val _vm = PositionVM()
    private val _uiState = MutableStateFlow(_vm.calculateVm(_vm.pos))
    val uiState: StateFlow<BoardState> = _uiState.asStateFlow()
}

// MVVM "ViewModel" in the app
class PositionVM {
    val pos: Position = PositionImpl.empty()
        .setKoma(
            Square(Col(1), Row(1)),
            com.mfoo.shogi.Koma(Side.GOTE, KomaType.KY)
        )
        .setKoma(
            Square(Col(1), Row(3)),
            com.mfoo.shogi.Koma(Side.GOTE, KomaType.FU)
        )
        .setKoma(
            Square(Col(5), Row(9)),
            com.mfoo.shogi.Koma(Side.SENTE, KomaType.OU)
        )
        .setKoma(
            Square(Col(8), Row(8)),
            com.mfoo.shogi.Koma(Side.SENTE, KomaType.KA)
        )

    private fun squareToXY(sq: Square): Pair<Int, Int> {
        val numCols = 9
        return Pair(numCols - sq.col.int, sq.row.int - 1)
    }

    fun calculateVm(position: Position): BoardState {
        val allKomas = position.getAllKoma().map { (sq, koma) ->
            val (x, y) = squareToXY(sq)
            KomaOnBoard(koma.komaType, x, y, !koma.side.isSente())
        }
        return BoardState(allKomas)
    }
}
