package com.mfoo.shogi.kif

import com.mfoo.shogi.Move
import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Side
import com.mfoo.shogi.Tree
import com.mfoo.shogi.isLegal


/**
 * Converts a parsed KIF AST into a validated one containing `Move`s.
 *
 * Prunes all nodes after one containing an illegal move, and
 * collapses duplicated child nodes into single ones.
 */
fun validateKif(game: KifAst.Game<KifAst.Move>): KifAst.Game<Move> {
    return KifAst.Game(
        game.startPos,
        convertRoot(
            game.rootNode,
            game.startPos as PositionImpl
        ).let(::deduplicateTree) as Tree.RootNode,
        game.headers
    )
}

/**
 * If a node has multiple children with the same (move) value,
 * collapse the child nodes into a single child node.
 */
private fun <T> deduplicateTree(node: Tree<T>): Tree<T> {
    val newChildren = node.children
        .groupBy { it.value }
        .values
        .toList()
        .map { nodes ->
            assert(nodes.isNotEmpty())
            if (nodes.size == 1) {
                nodes[0]
            } else {
                Tree.Node(
                    nodes.flatMap { it.children },
                    nodes[0].value
                )
            }
        }
        .map(::deduplicateTree) as List<Tree.Node<T>>
    return when (node) {
        is Tree.Node -> node.copy(children = newChildren)
        is Tree.RootNode -> node.copy(children = newChildren)
    }
}


private fun convertRoot(
    root: Tree.RootNode<KifAst.Move>,
    pos: PositionImpl,
): Tree.RootNode<Move> {
    return Tree.RootNode(root.children.mapNotNull { n ->
        convertTree(n, pos)
    })
}

/**
 * Plays through and prunes a tree of illegal moves and their child nodes.
 */
private fun convertTree(
    node: Tree.Node<KifAst.Move>,
    pos: PositionImpl,
): Tree.Node<Move>? {
    val move = moveFromKifAst(node.value, pos.getSideToMove(), pos)
    if (!isLegal(move, pos)) {
        return null
    }
    val nextPos = pos.doMove(move)
    return Tree.Node(
        node.children.mapNotNull { n -> convertTree(n, nextPos) },
        move
    )
}

/**
 * Converts a `KifAst.Move` to a `Move`, given the extra context
 * needed by `Move`.
 */
private fun moveFromKifAst(
    kifMove: KifAst.Move,
    side: Side,
    posBeforeMove: Position,
): Move {
    return when (kifMove) {
        is KifAst.Move.Drop -> {
            Move.Drop(kifMove.sq, side, kifMove.komaType)
        }

        is KifAst.Move.GameEnd -> {
            val gameEndType = when (kifMove.endType) {
                KifAst.Move.GameEndType.ABORT -> Move.GameEndType.ABORT
                KifAst.Move.GameEndType.RESIGN -> Move.GameEndType.RESIGN
                KifAst.Move.GameEndType.JISHOGI -> Move.GameEndType.JISHOGI
                KifAst.Move.GameEndType.SENNICHITE -> Move.GameEndType.SENNICHITE
                KifAst.Move.GameEndType.FLAG -> Move.GameEndType.FLAG
                KifAst.Move.GameEndType.ILLEGAL_WIN -> Move.GameEndType.ILLEGAL_WIN
                KifAst.Move.GameEndType.ILLEGAL_LOSS -> Move.GameEndType.ILLEGAL_LOSS
                KifAst.Move.GameEndType.NYUUGYOKU -> Move.GameEndType.NYUUGYOKU
                KifAst.Move.GameEndType.NO_CONTEST_WIN -> Move.GameEndType.NO_CONTEST_WIN
                KifAst.Move.GameEndType.NO_CONTEST_LOSS -> Move.GameEndType.NO_CONTEST_LOSS
                KifAst.Move.GameEndType.MATE -> Move.GameEndType.MATE
                KifAst.Move.GameEndType.NO_MATE -> Move.GameEndType.NO_MATE
            }
            Move.GameEnd(gameEndType)
        }

        is KifAst.Move.Regular -> {
            val capturedKoma =
                posBeforeMove.getKoma(kifMove.endSq).getOrNull()
            Move.Regular(
                startSq = kifMove.startSq,
                endSq = kifMove.endSq,
                isPromotion = kifMove.isPromotion,
                side = side,
                komaType = kifMove.komaType,
                capturedKoma = capturedKoma
            )
        }
    }
}