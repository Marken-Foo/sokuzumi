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


// For Android lifecycle persistence across configuration change
class PositionViewModel : ViewModel() {
    private val _vm = PositionVM()
    private val _uiState: MutableStateFlow<PosUiState> =
        MutableStateFlow(_vm.toPositionUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    fun cancelSelection() {
        _vm.cancelSelection()
        _uiState.update { _vm.toPositionUiState() }
    }

    fun onSquareClick(x: Int, y: Int) {
        _vm.onSquareClick(x, y)
        _uiState.update { _vm.toPositionUiState() }
    }

    fun onSenteHandClick(komaType: KomaType) {
        _vm.onSenteHandClick(komaType)
        _uiState.update { _vm.toPositionUiState() }
    }

    fun onGoteHandClick(komaType: KomaType) {
        _vm.onGoteHandClick(komaType)
        _uiState.update { _vm.toPositionUiState() }
    }
}


// MVVM "ViewModel" in the app
class PositionVM() {
    private sealed interface Selected {
        data object None : Selected
        class Square(val t: com.mfoo.shogi.Square) : Selected
        class Koma(val side: Side, val komaType: KomaType) : Selected
    }

    // val tempSfen = "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1"
    val tempSfen =
        "5+N2l/1+R1p3k1/2+N1p2p1/L4p2p/2Pr1np2/3gP3P/p4PPP1/5SK2/5G1NL b 2G2S4P2bslp 87"
    private var pos: Position =
        PositionImpl.fromSfen(tempSfen)
            ?: PositionImpl.empty()

    private var selectedItem: Selected = Selected.None

    private fun xYToSquare(x: Int, y: Int): Square {
        val numCols = 9
        return Square(Col(numCols - x), Row(y + 1))
    }

    private fun calculateBoardState(position: Position): PosUiState.BoardState {
        val allKomas = position.getAllKoma().map { (sq, koma) ->
            val (x, y) = PosUiState.SquareXY(sq)
            PosUiState.KomaOnBoard(koma.komaType, x, y, !koma.side.isSente())
        }
        return PosUiState.BoardState(allKomas)
    }

    private fun toUiSelection(selected: Selected): PosUiState.SelectedElement {
        return when (selected) {
            Selected.None -> PosUiState.SelectedElement.None
            is Selected.Square -> PosUiState.SelectedElement.Square(
                selected.t.let(PosUiState::SquareXY)
            )

            is Selected.Koma -> PosUiState.SelectedElement.Koma(
                Koma(selected.side, selected.komaType)
            )
        }
    }

    fun toPositionUiState(): PosUiState {
        return PosUiState(
            calculateBoardState(pos),
            toUiSelection(selectedItem),
            senteHand = pos.getHandOfSide(Side.SENTE).getAmounts(),
            goteHand = pos.getHandOfSide(Side.GOTE).getAmounts(),
        )
    }

    fun cancelSelection() {
        selectedItem = Selected.None
    }

    fun onSquareClick(x: Int, y: Int) {
        val sq = xYToSquare(x, y)

        when (val item = selectedItem) {
            is Selected.None -> {
                selectedItem = Selected.Square(sq)
            }

            is Selected.Square ->
                if (item.t == sq) {
                    selectedItem = Selected.None
                } else {
                    //TODO: make a move out of selectedsquare.t and sq
                    selectedItem = Selected.None
                }

            is Selected.Koma -> {
                //TODO: See if the drop makes a move
                selectedItem = Selected.None
            }
        }
    }

    fun onSenteHandClick(komaType: KomaType) {
        val currentSelected = selectedItem
        if (currentSelected is Selected.Koma
            && currentSelected.side == Side.SENTE
            && currentSelected.komaType == komaType
        ) {
            selectedItem = Selected.None
        } else {
            selectedItem = Selected.Koma(Side.SENTE, komaType)
        }
    }

    fun onGoteHandClick(komaType: KomaType) {
        val currentSelected = selectedItem
        if (currentSelected is Selected.Koma
            && currentSelected.side == Side.GOTE
            && currentSelected.komaType == komaType
        ) {
            selectedItem = Selected.None
        } else {
            selectedItem = Selected.Koma(Side.GOTE, komaType)
        }
    }
}
