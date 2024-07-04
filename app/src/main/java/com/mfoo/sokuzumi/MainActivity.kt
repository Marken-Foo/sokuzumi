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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfoo.sokuzumi.ui.theme.SokuzumiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokuzumiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Board(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Contains just the (9x9) shogi board and komas on it
@Composable
fun Board(modifier: Modifier = Modifier) {
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
        Koma(
            KomaType.KE,
            true,
            modifier = Modifier
                .width(sqWidth)
                .height(sqHeight)
                .offset(sqX(7), sqY(7))
        )
        Koma(
            KomaType.NG,
            false,
            modifier = Modifier
                .width(sqWidth)
                .height(sqHeight)
                .offset(sqX(1), sqY(2))
        )
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
    Board()
}