package com.mfoo.sokuzumi.position

import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Square


data class PosUiState(
    val board: BoardState,
    val selection: SelectedElement = SelectedElement.None,
    val senteHand: Map<KomaType, Int>,
    val goteHand: Map<KomaType, Int>,
    val promotionPrompt: Pair<SquareXY, BoardKoma>?,
)

data class BoardKoma(
    val komaType: KomaType,
    val isUpsideDown: Boolean,
)

data class BoardState(val t: Map<SquareXY, BoardKoma>)

data class SquareXY(val x: Int, val y: Int) {
    constructor(sq: Square) : this(
        x = NUM_COLS - sq.col.t,
        y = sq.row.t - 1
    )

    companion object {
        private const val NUM_COLS = 9
    }
}

sealed interface SelectedElement {
    data object None : SelectedElement
    class Square(val t: SquareXY) : SelectedElement
    class HandKoma(val t: com.mfoo.shogi.Koma) : SelectedElement
}
