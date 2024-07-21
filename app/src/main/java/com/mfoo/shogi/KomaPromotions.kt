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

fun KomaType.demote(): KomaType {
    return when (this) {
        KomaType.FU -> KomaType.FU
        KomaType.KY -> KomaType.KY
        KomaType.KE -> KomaType.KE
        KomaType.GI -> KomaType.GI
        KomaType.KI -> KomaType.KI
        KomaType.KA -> KomaType.KA
        KomaType.HI -> KomaType.HI
        KomaType.OU -> KomaType.OU
        KomaType.TO -> KomaType.FU
        KomaType.NY -> KomaType.KY
        KomaType.NK -> KomaType.KE
        KomaType.NG -> KomaType.GI
        KomaType.UM -> KomaType.KA
        KomaType.RY -> KomaType.HI
    }
}
