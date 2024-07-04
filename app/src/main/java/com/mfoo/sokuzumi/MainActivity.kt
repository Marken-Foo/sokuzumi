package com.mfoo.sokuzumi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.mfoo.sokuzumi.position.views.Board
import com.mfoo.sokuzumi.position.PositionViewModel
import com.mfoo.sokuzumi.ui.theme.SokuzumiTheme

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
