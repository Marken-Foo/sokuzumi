package com.mfoo.sokuzumi.position.views

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.mfoo.shogi.Side
import com.mfoo.sokuzumi.position.PosUiState
import com.mfoo.sokuzumi.position.PositionViewModel

@Composable
fun Position(
    positionViewModel: PositionViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            positionViewModel.cancelSelection()
        })
    }) {
        val positionUiState by positionViewModel.uiState.collectAsState()
        val (selectedHandSide, selectedHandKomaType) = positionUiState.selection.let {
            if (it is PosUiState.SelectedElement.HandKoma) {
                Pair(it.t.side, it.t.komaType)
            } else {
                Pair(null, null)
            }
        }
        Hand(
            handAmounts = positionUiState.goteHand,
            onClick = positionViewModel::onGoteHandClick,
            selected = if (selectedHandSide == Side.GOTE) selectedHandKomaType else null,
        )
        Board(
            positionUiState.board,
            positionUiState.selection,
            positionUiState.promotionPrompt,
            onSquareClick = positionViewModel::onSquareClick,
            onCancel = positionViewModel::cancelSelection,
            onPromote = positionViewModel::onPromote,
            onUnpromote = positionViewModel::onUnpromote,
            // zIndex higher than hands for promotion prompt at edge
            // of board to be above hand koma
            modifier.zIndex(2f),
        )
        Hand(
            handAmounts = positionUiState.senteHand,
            onClick = positionViewModel::onSenteHandClick,
            selected = if (selectedHandSide == Side.SENTE) selectedHandKomaType else null,
        )
    }
}