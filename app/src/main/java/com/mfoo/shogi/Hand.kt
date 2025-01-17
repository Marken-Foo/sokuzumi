package com.mfoo.shogi

interface Hand {
    fun getAmounts(): Map<KomaType, Int>
    fun getAmount(komaType: KomaType): Int
    fun setAmount(komaType: KomaType, amount: Int): Hand
    fun increment(komaType: KomaType): Hand
    fun decrement(komaType: KomaType): Hand
}

interface HandFactory {
    fun empty(): Hand
}

class HandImpl private constructor(
    private val amounts: Map<KomaType, Int>,
) : Hand {
    override fun equals(other: Any?) =
        (other is HandImpl) && this.amounts == other.amounts

    override fun hashCode(): Int {
        return this.amounts.hashCode()
    }

    override fun toString(): String {
        return this.amounts.toString()
    }

    override fun getAmounts(): Map<KomaType, Int> {
        return this.amounts.filter { (_, amount) -> amount > 0 }
    }

    override fun getAmount(komaType: KomaType): Int =
        this.amounts[komaType] ?: 0

    override fun setAmount(komaType: KomaType, amount: Int): HandImpl =
        if (amount < 0 || komaType !in allowedHandKomaTypes) {
            this
        } else {
            HandImpl(amounts = this.amounts + (komaType to amount))
        }

    override fun increment(komaType: KomaType): HandImpl {
        val amount = this.amounts[komaType] ?: 0
        return setAmount(komaType, amount + 1)
    }

    override fun decrement(komaType: KomaType): HandImpl {
        val amount = this.amounts[komaType] ?: 0
        return setAmount(komaType, maxOf(amount - 1, 0))
    }

    companion object : HandFactory {
        private val allowedHandKomaTypes = setOf(
            KomaType.FU,
            KomaType.KY,
            KomaType.KE,
            KomaType.GI,
            KomaType.KI,
            KomaType.KA,
            KomaType.HI
        )

        override fun empty() =
            HandImpl(allowedHandKomaTypes.associateWith { 0 })
    }
}
