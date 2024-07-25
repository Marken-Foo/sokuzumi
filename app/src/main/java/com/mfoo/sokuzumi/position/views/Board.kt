package com.mfoo.sokuzumi.position.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfoo.shogi.promote
import com.mfoo.sokuzumi.position.PosUiState
import com.mfoo.sokuzumi.position.PositionViewModel

// Contains just the (9x9) shogi board and komas on it
@Composable
fun Board(
    board: PosUiState.BoardState,
    selection: PosUiState.SelectedElement,
    promotionPrompt: PosUiState.BoardKoma?,
    onSquareClick: (Int, Int) -> Unit,
    onCancel: () -> Unit,
    onPromote: () -> Unit,
    onUnpromote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lineThickness = 1.dp
    BoxWithConstraints(modifier = modifier.aspectRatio(11f / 12)) {
        val numCols = 9
        val numRows = 9
        val boardWidth = maxWidth
        val boardHeight = maxHeight
        val sqWidth = (boardWidth - lineThickness) / numCols - lineThickness
        val sqHeight = (boardHeight - lineThickness) / numRows - lineThickness

        // Returns the x-coordinate of the left edge of a column (zero-indexed)
        fun sqX(col: Int): Dp {
            return lineThickness * 3 / 2 + (sqWidth + lineThickness) * col
        }

        // Returns the y-coordinate of the top edge of a row (zero-indexed)
        fun sqY(row: Int): Dp {
            return lineThickness * 3 / 2 + (sqHeight + lineThickness) * row
        }

        BoardBackground(
            numCols = numCols,
            numRows = numRows,
            lineThickness = with(
                LocalDensity.current
            ) { lineThickness.toPx() },
            Modifier.size(boardWidth, boardHeight)
        )

        if (selection is PosUiState.SelectedElement.Square) {
            val selectedSq = selection.t
            Box(
                modifier = Modifier
                    .width(sqWidth)
                    .height(sqHeight)
                    .offset(sqX(selectedSq.x), sqY(selectedSq.y))
                    .background(Color(153, 255, 0, 68))
            )
        }

        for (k in board.komas) {
            Koma(
                k.komaType,
                k.isUpsideDown,
                modifier = Modifier
                    .width(sqWidth)
                    .height(sqHeight)
                    .offset(sqX(k.x), sqY(k.y))
            )
        }

        // Clickable layer to take inputs
        for (x in (0..<numCols)) {
            for (y in (0..<numRows)) {
                Box(
                    modifier = Modifier
                        .width(sqWidth)
                        .height(sqHeight)
                        .offset(sqX(x), sqY(y))
                        .background(Color.Transparent)
                        .noRippleClickable {
                            onSquareClick(x, y)
                        }
                )
            }
        }

        promotionPrompt?.let {
            Box(
                modifier = Modifier
                    .width(boardWidth)
                    .height(boardHeight)
                    .background(Color(255, 255, 255, 153))
                    .noRippleClickable(onCancel)
            )
            Koma(
                it.komaType.promote(),
                it.isUpsideDown,
                modifier = Modifier
                    .width(sqWidth)
                    .height(sqHeight)
                    .offset(sqX(it.x), sqY(it.y))
                    .noRippleClickable(onPromote)
            )
            Koma(
                it.komaType,
                it.isUpsideDown,
                modifier = Modifier
                    .width(sqWidth)
                    .height(sqHeight)
                    .offset(
                        sqX(it.x), sqY(
                            if (it.isUpsideDown) {
                                it.y - 1
                            } else {
                                it.y + 1
                            }
                        ),
                    )
                    .noRippleClickable(onUnpromote)
            )
        }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    composed {
        this.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }) {
            onClick()
        }
    }

@Preview(showBackground = true)
@Composable
fun BoardPreview() {
    val vm = PositionViewModel()
    val uiState = vm.uiState.collectAsState().value
    Board(
        uiState.board,
        uiState.selection,
        uiState.promotionPrompt,
        onSquareClick = vm::onSquareClick,
        onCancel = vm::cancelSelection,
        onPromote = vm::onPromote,
        onUnpromote = vm::onUnpromote,
    )
}
