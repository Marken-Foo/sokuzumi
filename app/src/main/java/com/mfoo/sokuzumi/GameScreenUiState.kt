package com.mfoo.sokuzumi

import com.mfoo.sokuzumi.position.PosUiState

data class GameScreenUiState(
    val posUi: PosUiState,
    val moveList: List<String>,
)
