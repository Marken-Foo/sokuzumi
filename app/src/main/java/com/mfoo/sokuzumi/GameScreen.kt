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
private fun GameScreen(gameScreenViewModel: GameScreenViewModel) {
    val state = gameScreenViewModel.uiState.collectAsState()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Position(
                viewModel,
                Modifier.padding(innerPadding)
            )
            MoveButtons(
                goToStart = { /*TODO*/ },
                goBackward = { /*TODO*/ },
                goForward = { /*TODO*/ },
                goToEnd = { /*TODO*/ },
                modifier = Modifier
            )
            MoveList(moves = listOf("P-76", "P-34", "R-68", "Bx88+", "Sx"))
        }
    }
}