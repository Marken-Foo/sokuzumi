package com.mfoo.sokuzumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mfoo.shogi.GameImpl
import com.mfoo.shogi.kif.readKifFromShiftJisStream
import com.mfoo.sokuzumi.ui.theme.SokuzumiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO: deal with different file encodings (UTF-8 as default too)
        val game = assets.open("problems/5.kif")
            .let(::readKifFromShiftJisStream)
            ?.let(GameImpl.Companion::fromKifAst)
            ?: GameImpl.empty()
        val gameScreenViewModel = GameScreenViewModel(listOf(game))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SokuzumiTheme {
                GameScreen(gameScreenViewModel = gameScreenViewModel)
            }
        }
    }
}
