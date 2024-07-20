package com.mfoo.shogi

fun canBePromotion(move: Move.Regular): Boolean {
    return move.komaType.isPromotable() &&
        (move.startSq.isInPromotionZone(move.side)
            || move.endSq.isInPromotionZone(move.side))
}

private fun Square.isInPromotionZone(side: Side): Boolean {
    return when (side) {
        Side.SENTE -> this.row.int in 1..3
        Side.GOTE -> this.row.int in 7..9
    }
}

fun isLegal(move: Move, pos: Position): Boolean {
    TODO("implement move legality checking")
}

fun isValid(move: Move, pos: Position): Boolean {
    TODO("implement move validity checking")
}
