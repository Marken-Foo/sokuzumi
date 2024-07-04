package com.mfoo.sokuzumi.position.views

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mfoo.shogi.KomaType
import com.mfoo.sokuzumi.R

@Composable
fun Koma(
    komaType: KomaType,
    isUpsideDown: Boolean,
    modifier: Modifier = Modifier,
) {
    val painter = painterResource(id = getKomaImage(komaType, isUpsideDown))
    Image(painter = painter, contentDescription = null, modifier = modifier)
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
