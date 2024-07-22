package com.mfoo.shogi

import com.mfoo.shogi.MailboxCompanion.Direction

private typealias MailboxIdx = Int
private typealias Displacement = Int

/**
 * A definition for the valid movements of a koma.
 *
 * - Steps: the koma can move to a location at a given displacement
 * away from the start square (a *stepper*, in fairy chess terms).
 *
 * - Lines: the koma can move any number of squares in a given direction
 * until encountering an allied koma (a *linerider*, in fairy chess terms).
 */
internal class KomaMovement(
    val steps: Iterable<Displacement> = emptyList(),
    val lines: Iterable<Direction> = emptyList(),
) {
    /**
     * Returns the valid destination squares, given a board state,
     * as defined by the koma's allowed movements.
     */
    fun getDestinations(
        board: MailboxBoard,
        startIdx: MailboxIdx,
        side: Side,
    ): Iterable<Square> {
        val steps = this.steps
            .map { dir -> dir + startIdx }
            .filterNot { isAllyAtIndex(board, it, side) }
            .map(MailboxBoardImpl::sqFromIndex)
        val lines = lines.flatMap { getSquaresInRay(board, side, startIdx, it) }
        return steps.plus(lines).toSet()
    }
}

private fun isAllyAtIndex(
    board: MailboxBoard,
    idx: MailboxIdx,
    side: Side,
): Boolean {
    val content = board.mailbox[idx]
    return content is MailboxContent.Koma && content.t.side == side
}

/**
 * Returns the squares in a line that extends until it hits an allied unit,
 * exclusive of the allied unit's square.
 */
private fun getSquaresInRay(
    board: MailboxBoard,
    side: Side,
    startIdx: MailboxIdx,
    direction: Direction,
): Iterable<Square> {
    val res = mutableListOf<Square>()

    for (stepNum in generateSequence(1) { 1 + it }) {
        val idx = startIdx + direction.t * stepNum
        when (val content = board.mailbox[idx]) {
            MailboxContent.Invalid -> break

            is MailboxContent.Koma -> {
                if (content.t.side != side) {
                    res.add(MailboxBoardImpl.sqFromIndex(idx))
                }
                break
            }

            MailboxContent.Empty -> {
                res.add(MailboxBoardImpl.sqFromIndex(idx))
            }
        }
    }
    return res
}
