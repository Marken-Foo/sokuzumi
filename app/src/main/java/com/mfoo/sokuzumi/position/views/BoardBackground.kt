package com.mfoo.sokuzumi.position.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

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
