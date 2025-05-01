package com.mfoo.shogi.kif

import com.mfoo.shogi.Position
import com.mfoo.shogi.PositionImpl
import com.mfoo.shogi.Square
import com.mfoo.shogi.Tree
import com.mfoo.shogi.bod.parseBod
import com.mfoo.shogi.readFile
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

fun readKifFromShiftJisStream(inputStream: InputStream): KifAst.Game<KifAst.Move>? {
    val tokens = InputStreamReader(inputStream, Charset.forName("SHIFT-JIS"))
        .let(::BufferedReader)
        .let(::tokenise)
    val (gameInfo, state1) = parseGameInformation(ParseState(tokens))
    val (moveList, state2) = parseMainMoveSection(state1)
    val (variationList, _) = parseVariationSection(state2)

    val rootNode = makeMoveTree(moveList, variationList)
    return rootNode?.let {
        KifAst.Game(
            gameInfo.startPosition,
            rootNode,
            gameInfo.headers,
        )
    }
}

/**
 * Parse a provided KIF file.
 */
fun readKifFile(filename: String): KifAst.Game<KifAst.Move>? {
    val buf = readFile(filename)
    val tokens = tokenise(buf)
    val (gameInfo, state1) = parseGameInformation(ParseState(tokens))
    val (moveList, state2) = parseMainMoveSection(state1)
    val (variationList, _) = parseVariationSection(state2)

    val rootNode = makeMoveTree(moveList, variationList)
    return rootNode?.let {
        KifAst.Game(
            gameInfo.startPosition,
            rootNode,
            gameInfo.headers,
        )
    }
}

private typealias Parser<T> = (input: ParseState) -> ParseResult<T>

/**
 * Represents the state of a parser as it traverses the input list of tokens.
 * To be provided as input to a parsing function.
 */
private data class ParseState(val input: List<Token>, val parsePos: Int = 0) {
    fun peek(): Token? {
        return input.getOrNull(parsePos)
    }

    fun advance(numTokens: Int = 1): ParseState {
        return this.copy(parsePos = this.parsePos + numTokens)
    }
}

/**
 * The output of a parsing function, comprising the result of the parse and
 * the state of the parser after the parse.
 */
private data class ParseResult<T>(val value: T, val state: ParseState)

// Store handicap positions as SFEN for now because it's easier to read the code,
// but KIF parsing becomes dependent on SFEN parsing.
private val SfenFromHandicap = mapOf(
    "平手" to "lnsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL b - 1",
    "香落ち" to "lnsgkgsn1/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "右香落ち" to "1nsgkgsnl/1r5b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "角落ち" to "lnsgkgsnl/1r7/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "飛車落ち" to "lnsgkgsnl/7b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "飛香落ち" to "lnsgkgsn1/7b1/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "二枚落ち" to "lnsgkgsnl/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "三枚落ち" to "1nsgkgsnl/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "四枚落ち" to "1nsgkgsn1/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "五枚落ち" to "2sgkgsn1/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "左五枚落ち" to "1nsgkgs2/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "六枚落ち" to "2sgkgs2/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "八枚落ち" to "3gkg3/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1",
    "十枚落ち" to "4k4/9/ppppppppp/9/9/9/PPPPPPPPP/1B5R1/LNSGKGSNL w - 1"
)

private class GameInformation(
    val headers: List<KifAst.Header>,
    val startPosition: Position,
)

/**
 * Parse the headers/game information section of the KIF.
 */
private fun parseGameInformation(input: ParseState): ParseResult<GameInformation> {
    var state = input
    var token = state.peek()
    val headers: MutableList<KifAst.Header> = mutableListOf()
    var startPos: Position? = null
    while (token != null) {
        when (token) {
            is Token.Handicap -> startPos =
                SfenFromHandicap[token.name]?.let { PositionImpl.fromSfen(it) }

            is Token.Bod -> startPos =
                parseBod(token.lines)?.let { PositionImpl.fromBodAst(it) }

            is Token.Comment -> Unit // Should be attached to first move
            is Token.Escape -> Unit
            is Token.HeaderKeyValuePair -> headers.add(
                KifAst.Header(
                    key = token.key,
                    value = token.value
                )
            )

            is Token.MoveLine -> break
            Token.MoveSectionDelineation -> break
            Token.Unknown -> break // Unexpected - unknown token
            is Token.VariationStart -> break // Unexpected - missing mainline
        }
        state = state.advance()
        token = state.peek()
    }
    return ParseResult(
        GameInformation(
            headers,
            startPos ?: PositionImpl.empty()
        ), state
    )
}

/**
 * Parses the mainline moves of the KIF, including skipping the optional line:
 *
 * `手数----指手---------消費時間--`
 */
private fun parseMainMoveSection(input: ParseState): ParseResult<List<KifAst.Move>> {
    var state = input
    if (state.peek() is Token.MoveSectionDelineation) {
        state = state.advance()
    }
    val (moveList, endState) = parseMoveSequence(state)
    return ParseResult(moveList, endState)
}

/**
 * Parses a series of lines containing moves.
 */
private fun parseMoveSequence(input: ParseState): ParseResult<List<KifAst.Move>> {
    var state = input
    var token = state.peek()

    val moveList = mutableListOf<KifAst.Move>()
    // The move currently under construction
    var currentMove: KifAst.Move? = null
    var previousSq: Square? = null

    while (token != null) {
        when (token) {
            is Token.Bod -> break // Unexpected
            is Token.Comment -> {
                currentMove = currentMove?.addCommentLine(token.line)
            }

            is Token.Escape -> Unit
            is Token.Handicap -> break // Unexpected
            is Token.HeaderKeyValuePair -> break // Unexpected
            is Token.MoveLine -> {
                val move = parseMove(token, previousSq)
                when (move) {
                    // TODO: handle error more gracefully
                    null -> return ParseResult(moveList, state)
                    is KifAst.Move.Drop -> previousSq = move.sq
                    is KifAst.Move.GameEnd -> Unit
                    is KifAst.Move.Regular -> previousSq = move.endSq
                }
                currentMove?.let { moveList.add(it) }
                currentMove = move
            }

            Token.MoveSectionDelineation -> break // Unexpected
            Token.Unknown -> break // Unexpected
            is Token.VariationStart -> break // signals start of variations section
        }
        state = state.advance()
        token = state.peek()
    }
    currentMove?.let { moveList.add(currentMove) }
    return ParseResult(moveList, state)
}

private class Variation(val moveNum: Int, val moveList: List<KifAst.Move>)

/**
 * Parses all the variations in the KIF, returning them in order of parsing (top-down).
 * Consumes all lines beginning with `変化：N手` and the moves that follow them.
 */
private fun parseVariationSection(input: ParseState): ParseResult<List<Variation>> {
    var state = input
    val variations = mutableListOf<Variation?>()
    while (state.peek() is Token.VariationStart) {
        val (variation, nextState) = parseVariation(state)
        variations.add(variation)
        state = nextState
    }
    return ParseResult(variations.filterNotNull(), state)
}

/**
 * Parses a single variation. Consumes the `変化：N手` line and the moves that follow.
 */
private fun parseVariation(input: ParseState): ParseResult<Variation?> {
    val token = input.peek()
    if (token !is Token.VariationStart) {
        return ParseResult(null, input)
    }
    val (moveList, state) = parseMoveSequence(input.advance())
    assert(moveList.isNotEmpty())
    assert(moveList[0].moveNum == token.moveNum)
    return ParseResult(Variation(token.moveNum, moveList), state)
}

/**
 * Converts the parsed moves of a KIF into a move tree.
 *
 * A KIF file stores the moves in preorder traversal order, if we consider
 * the mainline to be the leftmost branch. Given the mainline and a list of
 * variations in order of appearance in the KIF file, we can reconstruct the
 * move tree.
 */
private fun makeMoveTree(
    mainMoves: List<KifAst.Move>,
    variationList: List<Variation>,
): Tree.RootNode<KifAst.Move>? {
    if (mainMoves.isEmpty()) {
        return null
    }
    // Mainline moves can also be considered as a Variation
    val mainVariation = Variation(mainMoves[0].moveNum, mainMoves)
    val allVariations = ArrayDeque(variationList)
    allVariations.addFirst(mainVariation)

    // Construct the variation subtrees one at a time, in reverse order of
    // appearance (to construct from the leaves back towards the root).
    // Keep track of each subtree as we construct it and which move it starts from.
    // For each variation we parse, we need to include the subtrees that start
    // after the move number of the current variation's start.
    val branches = allVariations.foldRight(
        mutableMapOf()
    ) { variation, acc: BranchNodes ->
        val branchesToMerge = acc
            .filter { (n, _) -> n > variation.moveNum }
            .toMutableMap()
        val unmergedBranches = acc
            .filterNot { (n, _) -> n > variation.moveNum }
            .toMutableMap()
        val node = makeVariationNodes(variation, branchesToMerge)
        node?.let {
            unmergedBranches.addNode(variation.moveNum, node)
        }
        unmergedBranches
    }

    if (branches.size != 1) {
        return null
    }
    return Tree.RootNode(branches.values.toList()[0])
}

/**
 * A mapping of move numbers to the nodes representing the variations that start
 * at that move.
 */
private typealias BranchNodes = MutableMap<Int, ArrayDeque<Tree.Node<KifAst.Move>>>

private fun BranchNodes.addNode(
    moveNum: Int,
    node: Tree.Node<KifAst.Move>,
) {
    this.putIfAbsent(moveNum, ArrayDeque())
    this[moveNum]?.addFirst(node)
}

/**
 * Constructs the tree of nodes corresponding to a variation, given the
 * variation moves and a collection of already-constructed subtrees to be
 * included at specific move numbers.
 */
private fun makeVariationNodes(
    variation: Variation,
    branches: BranchNodes,
): Tree.Node<KifAst.Move>? {
    // As variation moves are in sequence, traverse in reverse to construct
    // the chain of nodes starting from the leaf.
    return variation.moveList.foldRight<KifAst.Move, Tree.Node<KifAst.Move>?>(
        null
    ) { move, acc ->
        val children = branches[move.moveNum + 1] ?: ArrayDeque()
        acc?.let { children.addFirst(acc) }
        Tree.Node(children, move)
    }
}

private fun main() {
    val game = readKifFile("./sample_problems/variations.kif")
    println(game)
}
