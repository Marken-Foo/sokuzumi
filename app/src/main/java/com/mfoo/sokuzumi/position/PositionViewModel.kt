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
}


// MVVM "ViewModel" in the app
class PositionVM() {
    private sealed interface Selected {
        data object None : Selected
        class Square(val t: com.mfoo.shogi.Square) : Selected
        class HandKoma(val side: Side, val komaType: KomaType) : Selected
    }

    // val tempSfen = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1"
    val tempSfen =
        "5+N2l/1+R1p3k1/2+N1p2p1/L4p2p/2Pr1np2/3gP3P/p4PPP1/5SK2/5G1NL b 2G2S4P2bslp 87"
    private var pos: PositionImpl =
        PositionImpl.fromSfen(tempSfen)
            ?: PositionImpl.empty()

    private var selection: Selected = Selected.None

    private fun xYToSquare(x: Int, y: Int): Square {
        val numCols = 9
        return Square(Col(numCols - x), Row(y + 1))
    }

    private fun calculateBoardState(pos: Position): PosUiState.BoardState {
        val allKomas = pos
            .getAllKoma()
            .map { (sq, k) ->
                val (x, y) = PosUiState.SquareXY(sq)
                PosUiState.BoardKoma(k.komaType, x, y, !k.side.isSente())
            }
        return PosUiState.BoardState(allKomas)
    }

    private fun toUiSelection(selected: Selected): PosUiState.SelectedElement {
        return when (selected) {
            Selected.None -> PosUiState.SelectedElement.None
            is Selected.Square -> PosUiState.SelectedElement.Square(
                selected.t.let(PosUiState::SquareXY)
            )

            is Selected.HandKoma -> PosUiState.SelectedElement.HandKoma(
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
        )
    }

    fun cancelSelection() {
        selection = Selected.None
    }

    fun onSquareClick(x: Int, y: Int) {
        val sq = xYToSquare(x, y)

        when (val currentSelection = selection) {
            is Selected.None -> {
                val koma = pos.getKoma(sq).getOrNull() ?: return
                if (pos.getSideToMove() == koma.side) {
                    this.selection = Selected.Square(sq)
                }
                return
            }

            is Selected.Square -> {
                if (currentSelection.t == sq) {
                    this.selection = Selected.None
                    return
                }
                val koma = pos.getKoma(currentSelection.t).getOrNull() ?: return
                if (pos.getSideToMove() != koma.side) {
                    this.selection = Selected.None
                    return
                }

                //TODO: isPromotion should be prompted if necessary
                val candidateMove = Move.Regular(
                    currentSelection.t,
                    sq,
                    false,
                    koma.side,
                    koma.komaType,
                    capturedKoma = pos.getKoma(sq).getOrNull()
                )

                //TODO: should be isLegal()
                if (isValid(candidateMove, pos)) {
                    pos = pos.doMove(candidateMove) as PositionImpl
                    this.selection = Selected.None
                } else {
                    this.selection = Selected.Square(sq)
                }
            }

            is Selected.HandKoma -> {
                val candidateMove = Move.Drop(
                    sq,
                    currentSelection.side,
                    currentSelection.komaType
                )
                if (isValid(candidateMove, pos)) {
                    //TODO: should be isLegal()
                    pos = pos.doMove(candidateMove) as PositionImpl
                    this.selection = Selected.None
                } else {
                    this.selection = Selected.Square(sq)
                }
            }
        }
    }

    fun onSenteHandClick(komaType: KomaType) {
        val currentSelected = selection
        if (pos.getSideToMove() != Side.SENTE) {
            return
        } else if (currentSelected is Selected.HandKoma
            && currentSelected.side == Side.SENTE
            && currentSelected.komaType == komaType
        ) {
            selection = Selected.None
        } else {
            selection = Selected.HandKoma(Side.SENTE, komaType)
        }
    }

    fun onGoteHandClick(komaType: KomaType) {
        val currentSelected = selection
        if (pos.getSideToMove() != Side.GOTE) {
            return
        } else if (currentSelected is Selected.HandKoma
            && currentSelected.side == Side.GOTE
            && currentSelected.komaType == komaType
        ) {
            selection = Selected.None
        } else {
            selection = Selected.HandKoma(Side.GOTE, komaType)
        }
    }
}
