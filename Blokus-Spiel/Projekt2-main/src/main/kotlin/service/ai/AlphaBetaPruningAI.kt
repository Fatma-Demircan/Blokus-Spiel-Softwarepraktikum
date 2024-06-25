package service.ai

import entity.BlockID
import entity.BlokusGameState
import entity.PlayerColor
import service.RootService
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * An AI class that uses the Alpha Beta pruning optimization to choose the next move.
 * It is related to [MiniMaxAI]
 */
class AlphaBetaPruningAI (private val rootService: RootService,
                          private val color: PlayerColor,
                          private val opponentColor: PlayerColor): AIType {

    private val aiService: AIService = rootService.aIService
    private val allowedEstimatedLeafNodes = 2234567 //1234567

    private var depthLimit = 0

    private val heuristic = LargestFirstIncreaseDecreaseHeuristic(aiService)
    private var allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)

    override fun getMove(currentGameState: BlokusGameState): Move {
        println("AlphaBeta Pruning AI Move")
        val state = AIGameState(true, currentGameState.gameBoard, currentGameState.currentPlayer.color,
            currentGameState.currentPlayer.blocks.size == 21,
            21 - currentGameState.currentPlayer.blocks.size)

        var maxValue: Double = Double.MIN_VALUE
        var bestMove: Move = aiService.emptyMove

        val posMoves = allPosMoves(state, listOf())
        setDepthLimit(posMoves.size)
        posMoves.forEach { move ->
            val moveValue = minValue(aiService.executeAICompMove(move, state), 1, mutableListOf(),
                Double.MIN_VALUE, Double.MAX_VALUE)
            if (moveValue > maxValue) {
                maxValue = moveValue
                bestMove = move
            }
        }
        allPosBlockOrientations.removeAll { it.blockID == bestMove.movedBlock.blockID }

        return bestMove
    }

    private fun maxValue(state: AIGameState, depth: Int, compPlacedBlocks: MutableList<BlockID>,
                         alpha: Double, beta: Double): Double {
        if (depth >= depthLimit)
            return heuristic.heuristicValue(state, color, opponentColor)

        val posMoves = allPosMoves(state, compPlacedBlocks)
        if (posMoves.isEmpty()) return heuristic.heuristicValue(state, color, opponentColor)

        var highestValue = Double.MIN_VALUE
        var lokalAlpha = alpha
        for (move in posMoves) {
            val newCompPlacedBlocks = compPlacedBlocks.map { it }.toMutableList()
            newCompPlacedBlocks.add(move.movedBlock.blockID)
            highestValue = max(highestValue, minValue(aiService.executeAICompMove(move, state),
                depth + 1, newCompPlacedBlocks, lokalAlpha, beta))
            if (highestValue >= beta) {
                //println("Skipped Max")

                return highestValue
            }
            lokalAlpha = max(lokalAlpha, highestValue)
        }
        return highestValue
    }

    private fun minValue(state: AIGameState, depth: Int, compPlacedBlocks: MutableList<BlockID>,
                         alpha: Double, beta: Double): Double {
        if (depth >= depthLimit)
            return heuristic.heuristicValue(state, color, opponentColor)

        val posMoves = allPosMoves(state, compPlacedBlocks)
        if (posMoves.isEmpty()) return heuristic.heuristicValue(state, color, opponentColor)

        var lowestValue = Double.MAX_VALUE
        var lokalBeta = beta
        for (move in posMoves) {
            val newCompPlacedBlocks = compPlacedBlocks.map { it }.toMutableList()
            newCompPlacedBlocks.add(move.movedBlock.blockID)
            lowestValue = min(lowestValue, maxValue(aiService.executeAICompMove(move, state),
                depth + 1, newCompPlacedBlocks, alpha, lokalBeta))
            if (lowestValue <= alpha) {
                //rintln("Skipped Min")
                return lowestValue
            }
            lokalBeta = min(lokalBeta, lowestValue)
        }
        return lowestValue
    }

    private fun allPosMoves(state: AIGameState, compPlacedBlocks: List<BlockID>): MutableList<Move> {
        val posMoves = state.allPosMoves(allPosBlockOrientations)
        posMoves.removeAll { it.movedBlock.blockID in compPlacedBlocks }
        //if (state.turn < 3) posMoves.removeAll { it.movedBlock.tileCount() != 5 }
        return posMoves
    }

    private fun setDepthLimit(countMoves: Int) {
        depthLimit = allowedEstimatedLeafNodes
        if (countMoves > 2) {
            depthLimit = log(allowedEstimatedLeafNodes.toDouble(), countMoves.toDouble()).toInt()
        }
        val hSt = "moves possible)(will be at around ~"
        println("Depth Limit $depthLimit ($countMoves $hSt ${countMoves.toDouble().pow(depthLimit).toInt()} leafs)")
    }

    override fun getPlayerColor(): PlayerColor {
        return color
    }

    override fun syncAfterUndoRedo() {
        allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)
    }


}