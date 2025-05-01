package com.mfoo.sokuzumi.position

import com.mfoo.shogi.Col
import com.mfoo.shogi.Koma
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Move
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Row
import com.mfoo.shogi.Side
import com.mfoo.shogi.Square
import com.mfoo.shogi.canBePromotion
import com.mfoo.shogi.isLegal


// MVVM "ViewModel" in the app
class PositionVM(private var pos: PositionImpl) {
    private sealed interface Selected {
        data object None : Selected
        class Square(val t: com.mfoo.shogi.Square) : Selected
        class HandKoma(val side: Side, val komaType: KomaType) : Selected
    }

    private data class PromotionInfo(val move: Move.Regular)

    private var selection: Selected = Selected.None
    private var pendingPromotion: PromotionInfo? = null

    private fun xYToSquare(x: Int, y: Int): Square {
        val numCols = 9
        return Square(Col(numCols - x), Row(y + 1))
    }

    private fun calculateBoardState(pos: Position): BoardState {
        return pos
            .getAllKoma()
            .map { (sq, k) ->
                SquareXY(sq) to BoardKoma(k.komaType, !k.side.isSente())
            }
            .toMap()
            .let(::BoardState)
    }

    private fun toUiSelection(selected: Selected): SelectedElement {
        return when (selected) {
            Selected.None -> SelectedElement.None
            is Selected.Square -> SelectedElement.Square(
                selected.t.let(::SquareXY)
            )

            is Selected.HandKoma -> SelectedElement.HandKoma(
                Koma(selected.side, selected.komaType)
            )
        }
    }

    fun toPositionUiState(): PosUiState {
        return PosUiState(
            calculateBoardState(pos),
            toUiSelection(selection),
            senteHand = pos.getHandOfSide(Side.SENTE).getAmounts(),
            goteHand = pos.getHandOfSide(Side.GOTE).getAmounts(),
            promotionPrompt = pendingPromotion?.let {
                SquareXY(it.move.endSq) to BoardKoma(
                    it.move.komaType,
                    !it.move.side.isSente(),
                )
            }
        )
    }

    fun cancelSelection() {
        selection = Selected.None
        pendingPromotion = null
    }

    fun onSquareClick(x: Int, y: Int) {
        val sq = xYToSquare(x, y)

        when (val prevSelection = selection) {
            is Selected.None -> {
                selectSquareIfAlly(sq)
                return
            }

            is Selected.Square -> {
                val move = makeMoveFromSquares(pos, prevSelection.t, sq)
                if (move == null) {
                    this.selection = Selected.None
                    return
                }
                if (!canBePromotion(move)) {
                    return if (isLegal(move, pos)) {
                        pos = pos.doMove(move)
                        this.selection = Selected.None
                    } else {
                        selectSquareIfAlly(sq)
                    }
                }

                val promotion = move.copy(isPromotion = true)
                val isPromotionLegal = isLegal(promotion, pos)
                val isUnpromotionLegal = isLegal(move, pos)

                if (isPromotionLegal && isUnpromotionLegal) {
                    pendingPromotion = PromotionInfo(move)
                    this.selection = Selected.None
                    return
                }
                if (isPromotionLegal && !isUnpromotionLegal) {
                    this.selection = Selected.None
                    pos = pos.doMove(promotion)
                    return
                }
                if (!isPromotionLegal && isUnpromotionLegal) {
                    this.selection = Selected.None
                    pos = pos.doMove(move)
                    return
                }
                if (!isPromotionLegal && !isUnpromotionLegal) {
                    selectSquareIfAlly(sq)
                    return
                }
            }

            is Selected.HandKoma -> {
                val move = Move.Drop(
                    sq,
                    prevSelection.side,
                    prevSelection.komaType
                )
                if (isLegal(move, pos)) {
                    pos = pos.doMove(move)
                    this.selection = Selected.None
                } else {
                    selectSquareIfAlly(sq)
                }
            }
        }
    }

    private fun selectSquareIfAlly(sq: Square) {
        this.selection = if (isAllyOnSquare(pos.getSideToMove(), sq, pos)) {
            Selected.Square(sq)
        } else {
            Selected.None
        }
    }

    private fun isAllyOnSquare(side: Side, sq: Square, pos: Position): Boolean {
        val koma = pos.getKoma(sq).getOrNull()
        return koma != null && side == koma.side
    }

    fun onSenteHandClick(komaType: KomaType) {
        val currentSelected = selection
        if (pos.getSideToMove() != Side.SENTE) {
            return
        }
        this.selection = if (currentSelected is Selected.HandKoma
            && currentSelected.side == Side.SENTE
            && currentSelected.komaType == komaType
        ) {
            Selected.None
        } else {
            Selected.HandKoma(Side.SENTE, komaType)
        }
    }

    fun onGoteHandClick(komaType: KomaType) {
        val currentSelected = selection
        if (pos.getSideToMove() != Side.GOTE) {
            return
        }
        this.selection = if (currentSelected is Selected.HandKoma
            && currentSelected.side == Side.GOTE
            && currentSelected.komaType == komaType
        ) {
            Selected.None
        } else {
            Selected.HandKoma(Side.GOTE, komaType)
        }
    }

    fun onPromote() {
        val unpromotion = pendingPromotion?.move
        if (unpromotion == null) {
            // should not be the case, should signal error
            return
        }
        val move = unpromotion.copy(isPromotion = true)
        pos = pos.doMove(move)
        pendingPromotion = null
    }

    fun onUnpromote() {
        val move = pendingPromotion?.move
        if (move == null) {
            // should not be the case, should signal error
            return
        }
        pos = pos.doMove(move)
        pendingPromotion = null
    }
}

private fun makeMoveFromSquares(
    pos: Position,
    startSq: Square,
    endSq: Square,
): Move.Regular? {
    if (startSq == endSq) {
        return null
    }
    val koma = pos.getKoma(startSq).getOrNull() ?: return null
    if (pos.getSideToMove() != koma.side) {
        return null
    }
    return Move.Regular(
        startSq,
        endSq,
        false,
        koma.side,
        koma.komaType,
        capturedKoma = pos.getKoma(endSq).getOrNull()
    )
}
