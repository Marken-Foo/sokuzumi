package com.mfoo.sokuzumi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mfoo.sokuzumi.game.views.MoveButtons
import com.mfoo.sokuzumi.game.views.MoveList
import com.mfoo.sokuzumi.position.views.Position

@Composable
fun GameScreen(gameScreenViewModel: GameScreenViewModel) {
    val state = gameScreenViewModel.uiState.collectAsState()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            with(gameScreenViewModel) {
                Position(
                    state.value.posUi,
                    cancelSelection = ::cancelSelection,
                    onSquareClick = ::onSquareClick,
                    onSenteHandClick = ::onSenteHandClick,
                    onGoteHandClick = ::onGoteHandClick,
                    onPromote = ::onPromote,
                    onUnpromote = ::onUnpromote,
                    Modifier.padding(innerPadding)
                )
            }
            with(gameScreenViewModel) {
                MoveButtons(
                    goToStart = ::goToStart,
                    goBackward = ::goBackward,
                    goForward = ::goForward,
                    goToEnd = ::goToEnd,
                    modifier = Modifier
                )
            }
            MoveList(moves = state.value.moveList)
        }
    }
}