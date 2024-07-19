package com.mfoo.sokuzumi.position

import androidx.lifecycle.ViewModel
import com.mfoo.shogi.Col
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Row
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

    fun onSquareClick(x: Int, y: Int) {
        _vm.onSquareClick(x, y)
        _uiState.update { _vm.toPositionUiState() }
    }
}


// MVVM "ViewModel" in the app
class PositionVM() {
    private sealed interface Selected {
        data object None : Selected
        class Square(val t: com.mfoo.shogi.Square) : Selected
        class Koma(val t: com.mfoo.shogi.Koma) : Selected
    }

    private var pos: Position =
        PositionImpl.fromSfen("lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1")
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
                selected.t
            )
        }
    }

    fun toPositionUiState(): PosUiState {
        return PosUiState(
            calculateBoardState(pos),
            toUiSelection(selectedItem),
        )
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
}
