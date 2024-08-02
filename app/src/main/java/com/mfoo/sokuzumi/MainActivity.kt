package com.mfoo.sokuzumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.kif.readKifFromShiftJisStream
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
                    Position(
                        viewModel,
                        Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
