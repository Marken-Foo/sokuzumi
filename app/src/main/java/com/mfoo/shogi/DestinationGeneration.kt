package com.mfoo.shogi

import com.mfoo.shogi.MailboxCompanion.Direction

/**
 * Returns the end squares (destinations) of valid moves on the board
 * that start from a given square.
 */
internal fun getValidDestinations(
    board: MailboxBoard,
    startSq: Square,
): List<Square> {
    val startIdx = MailboxBoardImpl.indexFromSq(startSq)
    return when (val k = board.mailbox[startIdx]) {
        MailboxContent.Invalid -> emptyList()
        MailboxContent.Empty -> emptyList()
        is MailboxContent.Koma -> getValidDestinations(
            board,
            startIdx,
            k.value.side,
            k.value.komaType
        )
    }
}

private fun getValidDestinations(
    board: MailboxBoard,
    startIdx: Int,
    side: Side,
    komaType: KomaType,
): List<Square> {
    return when (komaType) {
        KomaType.FU -> {
            listOf(startIdx + forward(side))
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.KY -> {
            val forward = when (side) {
                Side.SENTE -> Direction.N
                Side.GOTE -> Direction.S
            }
            return getSquaresInRay(board, side, startIdx, forward).toList()
        }

        KomaType.KE -> {
            val forward = when (side) {
                Side.SENTE -> Direction.N
                Side.GOTE -> Direction.S
            }
            listOf(
                forward.t + forward.t + Direction.E.t,
                forward.t + forward.t + Direction.W.t,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.GI -> {
            val forward = forward(side)
            listOf(
                Direction.NE.t,
                Direction.SE.t,
                Direction.SW.t,
                Direction.NW.t,
                forward,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.KI, KomaType.TO, KomaType.NY, KomaType.NK, KomaType.NG -> {
            val forward = forward(side)
            listOf(
                Direction.N.t,
                Direction.S.t,
                Direction.E.t,
                Direction.W.t,
                forward + Direction.E.t,
                forward + Direction.W.t,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.KA -> {
            getSquaresInRay(
                board,
                side,
                startIdx,
                Direction.NW
            )
                .plus(getSquaresInRay(board, side, startIdx, Direction.NE))
                .plus(getSquaresInRay(board, side, startIdx, Direction.SE))
                .plus(getSquaresInRay(board, side, startIdx, Direction.SW))
                .toList()
        }

        KomaType.HI -> {
            getSquaresInRay(board, side, startIdx, Direction.N)
                .plus(getSquaresInRay(board, side, startIdx, Direction.S))
                .plus(getSquaresInRay(board, side, startIdx, Direction.E))
                .plus(getSquaresInRay(board, side, startIdx, Direction.W))
                .toList()
        }

        KomaType.OU -> {
            listOf(
                Direction.N.t,
                Direction.NE.t,
                Direction.E.t,
                Direction.SE.t,
                Direction.S.t,
                Direction.SW.t,
                Direction.W.t,
                Direction.NW.t,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
        }

        KomaType.UM -> {
            val kaMoves = getSquaresInRay(
                board,
                side,
                startIdx,
                Direction.NW
            )
                .plus(getSquaresInRay(board, side, startIdx, Direction.NE))
                .plus(getSquaresInRay(board, side, startIdx, Direction.SE))
                .plus(getSquaresInRay(board, side, startIdx, Direction.SW))
                .toList()
            val orthogonalSteps = listOf(
                Direction.N.t,
                Direction.S.t,
                Direction.E.t,
                Direction.W.t,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
            kaMoves.plus(orthogonalSteps)
        }

        KomaType.RY -> {
            val hiMoves = getSquaresInRay(board, side, startIdx, Direction.N)
                .plus(getSquaresInRay(board, side, startIdx, Direction.S))
                .plus(getSquaresInRay(board, side, startIdx, Direction.E))
                .plus(getSquaresInRay(board, side, startIdx, Direction.W))
                .toList()
            val diagonalSteps = listOf(
                Direction.NW.t,
                Direction.NE.t,
                Direction.SE.t,
                Direction.SW.t,
            )
                .map { dir -> dir + startIdx }
                .filterNot { isAllyAtIndex(board, it, side) }
                .map(MailboxBoardImpl::sqFromIndex)
            hiMoves.plus(diagonalSteps)
        }
    }
}

private fun isAllyAtIndex(board: MailboxBoard, idx: Int, side: Side): Boolean {
    val content = board.mailbox[idx]
    return content is MailboxContent.Koma && content.value.side == side
}

private fun forward(side: Side): Int {
    return when (side) {
        Side.SENTE -> Direction.N.t
        Side.GOTE -> Direction.S.t
    }
}

/**
 * Returns the squares in a ray that extends until it hits an allied unit.
 */
private fun getSquaresInRay(
    board: MailboxBoard,
    side: Side,
    startIdx: Int,
    direction: Direction,
): Iterable<Square> {
    val res = mutableListOf<Square>()

    for (stepNum in generateSequence(1) { 1 + it }) {
        val idx = startIdx + direction.t * stepNum
        when (val content = board.mailbox[idx]) {
            MailboxContent.Empty -> {
                res.add(MailboxBoardImpl.sqFromIndex(idx))
            }

            MailboxContent.Invalid -> break
            is MailboxContent.Koma -> {
                if (content.value.side != side) {
                    res.add(MailboxBoardImpl.sqFromIndex(idx))
                    break
                }
                break
            }
        }
    }
    return res
}
