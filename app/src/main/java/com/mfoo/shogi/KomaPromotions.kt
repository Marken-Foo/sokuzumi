package com.mfoo.shogi

fun KomaType.isPromotable(): Boolean {
    return when (this) {
        KomaType.FU -> true
        KomaType.KY -> true
        KomaType.KE -> true
        KomaType.GI -> true
        KomaType.KI -> false
        KomaType.KA -> true
        KomaType.HI -> true
        KomaType.OU -> false
        KomaType.TO -> false
        KomaType.NY -> false
        KomaType.NK -> false
        KomaType.NG -> false
        KomaType.UM -> false
        KomaType.RY -> false
    }
}

fun KomaType.promote(): KomaType {
    return when (this) {
        KomaType.FU -> KomaType.TO
        KomaType.KY -> KomaType.NY
        KomaType.KE -> KomaType.NK
        KomaType.GI -> KomaType.NG
        KomaType.KI -> KomaType.KI
        KomaType.KA -> KomaType.UM
        KomaType.HI -> KomaType.RY
        KomaType.OU -> KomaType.OU
        KomaType.TO -> KomaType.TO
        KomaType.NY -> KomaType.NY
        KomaType.NK -> KomaType.NK
        KomaType.NG -> KomaType.NG
        KomaType.UM -> KomaType.UM
        KomaType.RY -> KomaType.RY
    }
}
