package com.mfoo.sokuzumi.position

import androidx.lifecycle.ViewModel
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
import com.mfoo.shogi.doMove
import com.mfoo.shogi.isValid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


// For Android lifecycle persistence across configuration change
class PositionViewModel : ViewModel() {
    private val _vm = PositionVM()
    private val _uiState: MutableStateFlow<PosUiState> =
        MutableStateFlow(_vm.toPositionUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.update { _vm.toPositionUiState() }
    }

    fun cancelSelection() {
        _vm.cancelSelection()
        refresh()
    }

    fun onSquareClick(x: Int, y: Int) {
        _vm.onSquareClick(x, y)
        refresh()
    }

    fun onSenteHandClick(komaType: KomaType) {
        _vm.onSenteHandClick(komaType)
        refresh()
    }

    fun onGoteHandClick(komaType: KomaType) {
        _vm.onGoteHandClick(komaType)
        refresh()
    }

    fun onPromote() {
        _vm.onPromote()
        refresh()
    }

    fun onUnpromote() {
        _vm.onUnpromote()
        refresh()
    }
}


// MVVM "ViewModel" in the app
class PositionVM {
    private sealed interface Selected {
        data object None : Selected
        class Square(val t: com.mfoo.shogi.Square) : Selected
        class HandKoma(val side: Side, val komaType: KomaType) : Selected
    }

    private data class PromotionInfo(val move: Move.Regular)

    // private val tempSfen = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1"
    private val tempSfen =
        "5+N2l/1+R1p3k1/2+N1p2p1/L4p2p/2Pr1np2/3gP3P/p4PPP1/5SK2/5G1NL b 2G2S4P2bslp 87"
    private var pos: PositionImpl =
        PositionImpl.fromSfen(tempSfen)
            ?: PositionImpl.empty()

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
                    return if (isValid(move, pos)) {
                        pos = pos.doMove(move) as PositionImpl
                        this.selection = Selected.None
                    } else {
                        selectSquareIfAlly(sq)
                    }
                }

                val promotion = move.copy(isPromotion = true)
                val isPromotionValid = isValid(promotion, pos)
                val isUnpromotionValid = isValid(move, pos)

                if (isPromotionValid && isUnpromotionValid) {
                    pendingPromotion = PromotionInfo(move)
                    this.selection = Selected.None
                    return
                }
                if (isPromotionValid && !isUnpromotionValid) {
                    this.selection = Selected.None
                    pos = pos.doMove(promotion) as PositionImpl
                    return
                }
                if (!isPromotionValid && isUnpromotionValid) {
                    this.selection = Selected.None
                    pos = pos.doMove(move) as PositionImpl
                    return
                }
                if (!isPromotionValid && !isUnpromotionValid) {
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
                if (isValid(move, pos)) {
                    //TODO: should be isLegal()
                    pos = pos.doMove(move) as PositionImpl
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
        pos = pos.doMove(move) as PositionImpl
        pendingPromotion = null
    }

    fun onUnpromote() {
        val move = pendingPromotion?.move
        if (move == null) {
            // should not be the case, should signal error
            return
        }
        pos = pos.doMove(move) as PositionImpl
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
