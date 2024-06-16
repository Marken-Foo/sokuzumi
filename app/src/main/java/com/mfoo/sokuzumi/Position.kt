package com.mfoo.sokuzumi

@JvmInline
value class Sfen(val sfen: String)

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
    fun getKoma(sq: Square): Koma
    fun setKoma(sq: Square, koma: Koma): Position
    fun getAllKoma(): List<Pair<Square, Koma>>

    // Game state functions
    fun getSideToMove(): Side
    fun toggleSideToMove(): Side

    // SFEN functions
    fun toSfen(): Sfen
}

interface PositionFactory {
    fun empty(): Position
    fun fromSfen(sfen: Sfen): Position
}

class PositionImpl() : Position {
    override fun getHandOfSide(side: Side): Hand {
        TODO("Not yet implemented")
    }

    override fun getHandAmount(side: Side, komaType: KomaType): Int {
        TODO("Not yet implemented")
    }

    override fun setHandAmount(
        side: Side,
        komaType: KomaType,
        amount: Int
    ): Position {
        TODO("Not yet implemented")
    }

    override fun incrementHandAmount(side: Side, komaType: KomaType): Position {
        TODO("Not yet implemented")
    }

    override fun decrementHandAmount(side: Side, komaType: KomaType): Position {
        TODO("Not yet implemented")
    }

    override fun getKoma(sq: Square): Koma {
        TODO("Not yet implemented")
    }

    override fun setKoma(sq: Square, koma: Koma): Position {
        TODO("Not yet implemented")
    }

    override fun getAllKoma(): List<Pair<Square, Koma>> {
        TODO("Not yet implemented")
    }

    override fun getSideToMove(): Side {
        TODO("Not yet implemented")
    }

    override fun toggleSideToMove(): Side {
        TODO("Not yet implemented")
    }

    override fun toSfen(): Sfen {
        TODO("Not yet implemented")
    }

    companion object : PositionFactory {
        override fun empty(): Position {
            TODO("Not yet implemented")
        }

        override fun fromSfen(sfen: Sfen): Position {
            TODO("Not yet implemented")
        }
    }
}
