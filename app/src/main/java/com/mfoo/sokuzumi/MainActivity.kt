package com.mfoo.sokuzumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mfoo.sokuzumi.ui.theme.SokuzumiTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokuzumiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Board(PositionViewModel(), Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class KomaOnBoard(
    val komaType: KomaType,
    val x: Int,
    val y: Int,
    val isUpsideDown: Boolean,
)

data class BoardState(
    val komas: List<KomaOnBoard>,
)

// For Android lifecycle persistence across configuration change
class PositionViewModel : ViewModel() {
    private val _vm = PositionVM()
    private val _uiState = MutableStateFlow(_vm.calculateVm(_vm.pos))
    val uiState: StateFlow<BoardState> = _uiState.asStateFlow()
}

// Actual ViewModel in the app
class PositionVM {
    val pos: Position = PositionImpl.empty()
        .setKoma(Square(Col(1), Row(1)), Koma(Side.GOTE, KomaType.KY))
        .setKoma(Square(Col(1), Row(3)), Koma(Side.GOTE, KomaType.FU))
        .setKoma(Square(Col(5), Row(9)), Koma(Side.SENTE, KomaType.OU))
        .setKoma(Square(Col(8), Row(8)), Koma(Side.SENTE, KomaType.KA))

    private fun squareToXY(sq: Square): Pair<Int, Int> {
        val numCols = 9
        return Pair(numCols - sq.col.int, sq.row.int - 1)
    }

    fun calculateVm(position: Position): BoardState {
        val allKomas = position.getAllKoma().map { (sq, koma) ->
            val (x, y) = squareToXY(sq)
            KomaOnBoard(koma.komaType, x, y, !koma.side.isSente())
        }
        return BoardState(allKomas)
    }
}

// Contains just the (9x9) shogi board and komas on it
@Composable
fun Board(positionViewModel: PositionViewModel, modifier: Modifier = Modifier) {
    val positionUiState by positionViewModel.uiState.collectAsState()
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

        for (k in positionUiState.komas) {
            Koma(
                k.komaType,
                k.isUpsideDown,
                modifier = Modifier
                    .width(sqWidth)
                    .height(sqHeight)
                    .offset(sqX(k.x), sqY(k.y))
            )
        }
    }
}

fun getKomaImage(komaType: KomaType, isUpsideDown: Boolean): Int {
    return when (isUpsideDown) {
        false -> when (komaType) {
            KomaType.FU -> R.drawable.koma__kanji_light__0fu
            KomaType.KY -> R.drawable.koma__kanji_light__0ky
            KomaType.KE -> R.drawable.koma__kanji_light__0ke
            KomaType.GI -> R.drawable.koma__kanji_light__0gi
            KomaType.KI -> R.drawable.koma__kanji_light__0ki
            KomaType.KA -> R.drawable.koma__kanji_light__0ka
            KomaType.HI -> R.drawable.koma__kanji_light__0hi
            KomaType.OU -> R.drawable.koma__kanji_light__0ou
            KomaType.TO -> R.drawable.koma__kanji_light__0to
            KomaType.NY -> R.drawable.koma__kanji_light__0ny
            KomaType.NK -> R.drawable.koma__kanji_light__0nk
            KomaType.NG -> R.drawable.koma__kanji_light__0ng
            KomaType.UM -> R.drawable.koma__kanji_light__0um
            KomaType.RY -> R.drawable.koma__kanji_light__0ry
        }

        true -> when (komaType) {
            KomaType.FU -> R.drawable.koma__kanji_light__1fu
            KomaType.KY -> R.drawable.koma__kanji_light__1ky
            KomaType.KE -> R.drawable.koma__kanji_light__1ke
            KomaType.GI -> R.drawable.koma__kanji_light__1gi
            KomaType.KI -> R.drawable.koma__kanji_light__1ki
            KomaType.KA -> R.drawable.koma__kanji_light__1ka
            KomaType.HI -> R.drawable.koma__kanji_light__1hi
            KomaType.OU -> R.drawable.koma__kanji_light__1ou
            KomaType.TO -> R.drawable.koma__kanji_light__1to
            KomaType.NY -> R.drawable.koma__kanji_light__1ny
            KomaType.NK -> R.drawable.koma__kanji_light__1nk
            KomaType.NG -> R.drawable.koma__kanji_light__1ng
            KomaType.UM -> R.drawable.koma__kanji_light__1um
            KomaType.RY -> R.drawable.koma__kanji_light__1ry
        }
    }
}

@Composable
fun Koma(
    komaType: KomaType,
    isUpsideDown: Boolean,
    modifier: Modifier = Modifier,
) {
    val painter = painterResource(id = getKomaImage(komaType, isUpsideDown))
    Image(painter = painter, contentDescription = null, modifier = modifier)
}

// Paints the board background (lines and background colour)
@Composable
fun BoardBackground(
    numCols: Int,
    numRows: Int,
    lineThickness: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(251, 192, 109))
    ) {
        val lineColor = Color.Black

        // Returns the x-coordinate of the n-th line from the left (zero-indexed)
        fun lineX(col: Int): Float {
            return col * (size.width - lineThickness) / numCols + lineThickness
        }

        // Returns the y-coordinate of the n-th line from the top (zero-indexed)
        fun lineY(row: Int): Float {
            return row * (size.height - lineThickness) / numRows + lineThickness
        }

        drawRect(lineColor, style = Stroke(lineThickness))

        repeat(numCols - 1) { c ->
            val x = lineX(c + 1)
            drawLine(
                lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = lineThickness,
            )
        }

        repeat(numRows - 1) { r ->
            val y = lineY(r + 1)
            drawLine(
                lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = lineThickness,
            )
        }

        // Draw star points
        for (col in setOf(3, 6)) {
            for (row in setOf(3, 6)) {
                drawCircle(
                    lineColor,
                    lineThickness * 3,
                    Offset(lineX(col), lineY(row))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoardPreview() {
    Board(PositionViewModel())
}