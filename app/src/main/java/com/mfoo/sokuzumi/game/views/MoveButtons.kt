package com.mfoo.sokuzumi.game.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoveButtons(
    goToStart: () -> Unit,
    goBackward: () -> Unit,
    goForward: () -> Unit,
    goToEnd: () -> Unit,
    modifier: Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = goToStart, shape = RoundedCornerShape(4.dp)) {
            Text(text = "|<")
        }
        Button(onClick = goBackward, shape = RoundedCornerShape(4.dp)) {
            Text(text = "<")
        }
        Button(onClick = goForward, shape = RoundedCornerShape(4.dp)) {
            Text(text = ">")
        }
        Button(onClick = goToEnd, shape = RoundedCornerShape(4.dp)) {
            Text(text = ">|")
        }
    }
}
