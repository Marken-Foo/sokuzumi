package com.mfoo.shogi

import com.mfoo.shogi.MailboxCompanion.Direction

// This file contains the definitions for how koma types can move (possibly
// also depending on side), according to the rules of shogi.

internal fun getKomaMovement(side: Side, komaType: KomaType): KomaMovement {
    return when (komaType) {
        KomaType.FU -> when (side) {
            Side.SENTE -> senteFuMovement
            Side.GOTE -> goteFuMovement
        }

        KomaType.KY -> when (side) {
            Side.SENTE -> senteKyMovement
            Side.GOTE -> goteKyMovement
        }

        KomaType.KE -> when (side) {
            Side.SENTE -> senteKeMovement
            Side.GOTE -> goteKeMovement
        }

        KomaType.GI -> when (side) {
            Side.SENTE -> senteGiMovement
            Side.GOTE -> goteGiMovement
        }

        KomaType.KI, KomaType.TO, KomaType.NY, KomaType.NK, KomaType.NG -> when (side) {
            Side.SENTE -> senteKiMovement
            Side.GOTE -> goteKiMovement
        }

        KomaType.KA -> kaMovement
        KomaType.HI -> hiMovement
        KomaType.OU -> ouMovement
        KomaType.UM -> umMovement
        KomaType.RY -> ryMovement
    }
}


private val senteFuMovement = KomaMovement(steps = listOf(Direction.N.t))
private val goteFuMovement = KomaMovement(steps = listOf(Direction.S.t))
private val senteKyMovement = KomaMovement(lines = listOf(Direction.N))
private val goteKyMovement = KomaMovement(lines = listOf(Direction.S))
private val senteKeMovement = KomaMovement(
    steps = listOf(
        Direction.N.t + Direction.N.t + Direction.E.t,
        Direction.N.t + Direction.N.t + Direction.W.t,
    )
)
private val goteKeMovement = KomaMovement(
    steps = listOf(
        Direction.S.t + Direction.S.t + Direction.E.t,
        Direction.S.t + Direction.S.t + Direction.W.t,
    )
)
private val senteGiMovement = KomaMovement(
    steps = listOf(
        Direction.NW.t,
        Direction.N.t,
        Direction.NE.t,
        Direction.SW.t,
        Direction.SE.t,
    )
)
private val goteGiMovement = KomaMovement(
    steps = listOf(
        Direction.NW.t,
        Direction.NE.t,
        Direction.SW.t,
        Direction.S.t,
        Direction.SE.t,
    )
)
private val senteKiMovement = KomaMovement(
    steps = listOf(
        Direction.NW.t,
        Direction.N.t,
        Direction.NE.t,
        Direction.W.t,
        Direction.E.t,
        Direction.S.t,
    )
)
private val goteKiMovement = KomaMovement(
    steps = listOf(
        Direction.N.t,
        Direction.W.t,
        Direction.E.t,
        Direction.SW.t,
        Direction.S.t,
        Direction.SE.t,
    )
)
private val kaMovement = KomaMovement(
    lines = listOf(
        Direction.NW,
        Direction.NE,
        Direction.SW,
        Direction.SE,
    )
)
private val hiMovement = KomaMovement(
    lines = listOf(
        Direction.N,
        Direction.W,
        Direction.E,
        Direction.S,
    )
)
private val ouMovement = KomaMovement(
    steps = listOf(
        Direction.NW.t,
        Direction.N.t,
        Direction.NE.t,
        Direction.W.t,
        Direction.E.t,
        Direction.SW.t,
        Direction.S.t,
        Direction.SE.t,
    )
)
private val umMovement = KomaMovement(
    lines = listOf(
        Direction.NW,
        Direction.NE,
        Direction.SW,
        Direction.SE,
    ), steps = listOf(
        Direction.N.t,
        Direction.W.t,
        Direction.E.t,
        Direction.S.t,
    )
)
private val ryMovement = KomaMovement(
    lines = listOf(
        Direction.N,
        Direction.W,
        Direction.E,
        Direction.S,
    ), steps = listOf(
        Direction.NW.t,
        Direction.NE.t,
        Direction.SW.t,
        Direction.SE.t,
    )
)
