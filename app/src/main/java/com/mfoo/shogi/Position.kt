package com.mfoo.shogi

import arrow.core.Either
import com.mfoo.shogi.bod.BodAst
import com.mfoo.shogi.sfen.SfenAst

/**
 * Represents a shogi position state, equivalent to the information in an SFEN.
 */
interface Position {
    // Hand functions
    fun getHandOfSide(side: Side): Hand
    fun getHandAmount(side: Side, komaType: KomaType): Int
    fun setHandAmount(side: Side, komaType: KomaType, amount: Int): Position
    fun incrementHandAmount(side: Side, komaType: KomaType): Position
    fun decrementHandAmount(side: Side, komaType: KomaType): Position

    // Board functions
    fun getKoma(sq: Square): Either<Unit, Koma?>
    fun setKoma(sq: Square, koma: Koma): Position
    fun removeKoma(sq: Square): Position
    fun getAllKoma(): Map<Square, Koma>

    // Game state functions
    fun getSideToMove(): Side
    fun setSideToMove(side: Side): Position
    fun toggleSideToMove(): Position

    /**
     * Applies the move to the given position, regardless of whether the move is legal.
     */
    fun doMove(move: Move): Position
}

interface PositionFactory {
    fun empty(): Position
    fun fromSfen(sfen: String): Position?
    fun fromSfenAst(sfenTree: SfenAst.ShogiPosition): Position
    fun fromBodAst(bodPosition: BodAst.Position): Position?
}
