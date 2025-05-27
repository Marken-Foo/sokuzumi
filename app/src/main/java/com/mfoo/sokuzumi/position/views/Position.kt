package com.mfoo.sokuzumi.position.views

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.mfoo.shogi.KomaType
import com.mfoo.shogi.Side
import com.mfoo.sokuzumi.position.PosUiState
import com.mfoo.sokuzumi.position.SelectedElement

@Composable
fun Position(
    positionUiState: PosUiState,
    cancelSelection: () -> Unit,
    onSquareClick: (Int, Int) -> Unit,
    onSenteHandClick: (KomaType) -> Unit,
    onGoteHandClick: (KomaType) -> Unit,
    onPromote: () -> Unit,
    onUnpromote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.pointerInput(Unit) {
        detectTapGestures(onTap = { cancelSelection() })
    }) {
        val (selectedHandSide, selectedHandKomaType) = positionUiState.selection.let {
            if (it is SelectedElement.HandKoma) {
                Pair(it.t.side, it.t.komaType)
            } else {
                Pair(null, null)
            }
        }
        Hand(
            handAmounts = positionUiState.goteHand,
            onClick = onGoteHandClick,
            selected = if (selectedHandSide == Side.GOTE) selectedHandKomaType else null,
        )
        Board(
            positionUiState.board,
            positionUiState.selection,
            positionUiState.promotionPrompt,
            onSquareClick = onSquareClick,
            onCancel = cancelSelection,
            onPromote = onPromote,
            onUnpromote = onUnpromote,
            // zIndex higher than hands for promotion prompt at edge
            // of board to be above hand koma
            modifier.zIndex(2f),
        )
        Hand(
            handAmounts = positionUiState.senteHand,
            onClick = onSenteHandClick,
            selected = if (selectedHandSide == Side.SENTE) selectedHandKomaType else null,
        )
    }
}