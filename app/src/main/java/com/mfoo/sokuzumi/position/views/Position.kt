package com.mfoo.sokuzumi.position.views

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
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
            if (it is PosUiState.SelectedElement.Koma) {
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
        Board(positionViewModel, modifier)
        Hand(
            handAmounts = positionUiState.senteHand,
            onClick = positionViewModel::onSenteHandClick,
            selected = if (selectedHandSide == Side.SENTE) selectedHandKomaType else null,
        )
    }
}