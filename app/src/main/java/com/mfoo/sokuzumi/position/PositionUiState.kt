package com.mfoo.sokuzumi.position

import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Square


data class PosUiState(
    val board: BoardState,
    val selection: SelectedElement = SelectedElement.None,
) {
    data class KomaOnBoard(
        val komaType: KomaType,
        val x: Int,
        val y: Int,
        val isUpsideDown: Boolean,
    )

    data class BoardState(
        val komas: List<KomaOnBoard>,
    )

    data class SquareXY(val x: Int, val y: Int) {
        constructor(sq: Square) : this(
            x = NUM_COLS - sq.col.int,
            y = sq.row.int - 1
        )

        companion object {
            private const val NUM_COLS = 9
        }
    }

    sealed interface SelectedElement {
        data object None : SelectedElement
        class Square(val t: SquareXY) :
            SelectedElement

        class Koma(val t: com.mfoo.shogi.Koma) : SelectedElement
    }
}
