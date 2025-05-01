package com.mfoo.sokuzumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.kif.readKifFromShiftJisStream
import com.mfoo.sokuzumi.game.views.MoveButtons
import com.mfoo.sokuzumi.game.views.MoveList
import com.mfoo.sokuzumi.position.PositionViewModel
import com.mfoo.sokuzumi.position.views.Position
import com.mfoo.sokuzumi.ui.theme.SokuzumiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO: deal with different file encodings (UTF-8 as default too)
        val pos = assets.open("problems/5.kif")
            .let(::readKifFromShiftJisStream)
            ?.startPos
        val viewModel = pos
            ?.let { PositionViewModel(it as PositionImpl) }
            ?: PositionViewModel()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokuzumiTheme {
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
        }
    }
}
