package com.mfoo.sokuzumi.position.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mfoo.shogi.KomaType

@Composable
fun Hand(
    handAmounts: Map<KomaType, Int>,
    selected: KomaType?,
    onClick: (komaType: KomaType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedKomaTypes = listOf(
        KomaType.HI,
        KomaType.KA,
        KomaType.KI,
        KomaType.GI,
        KomaType.KE,
        KomaType.KY,
        KomaType.FU
    )
    Row(modifier = modifier.background(Color(251, 192, 109))) {
        for (komaType in displayedKomaTypes) {
            val amount = handAmounts[komaType] ?: 0
            val backgroundColor = if (selected == komaType && amount > 0) {
                Color(153, 255, 0, 68)
            } else {
                Color.Transparent
            }
            Koma(
                komaType = komaType,
                isUpsideDown = false,
                modifier = Modifier
                    .width(36.dp)
                    .height(33.dp)
                    .background(backgroundColor)
                    .alpha(if (amount == 0) 0.25f else 1.0f)
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null
                    ) { onClick(komaType) }
            )
            Text(text = amount.toString())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HandPreview() {
    Hand(
        mapOf(KomaType.FU to 3, KomaType.KI to 1, KomaType.HI to 2),
        KomaType.KI,
        {})
}
