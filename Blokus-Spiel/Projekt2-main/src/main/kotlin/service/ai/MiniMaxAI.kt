package service.ai

import entity.BlokusGameState
import entity.PlayerColor
import service.RootService
import kotlin.math.log

/**
 * An AI class that uses the MiniMax algorithm to choose the next move.
 */
class MiniMaxAI (private val rootService: RootService,
                 private val color: PlayerColor,
                 private val opponentColor: PlayerColor): AIType {

    private val aIService: AIService = rootService.aIService
    private val allowedEstimatedLeafNodes = 6400

    private var depthLimit = 0

    private val heuristic = LargestFirstIncreaseDecreaseHeuristic(aIService)
    private var allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)

    override fun getMove(currentGameState: BlokusGameState): Move {
        println("MiniMax AI Move")
        val state = AIGameState(true, currentGameState.gameBoard, currentGameState.currentPlayer.color,
            currentGameState.currentPlayer.blocks.size == 21, 21 - currentGameState.currentPlayer.blocks.size)

        var maxValue: Double = Double.MIN_VALUE
        var bestMove: Move = aIService.emptyMove

        val allPosMoves = state.allPosMoves(allPosBlockOrientations)
        setDepthLimit(allPosMoves.size)
        allPosMoves.forEach { move ->
            val moveValue = minValue(aIService.executeAICompMove(move, state), 1)
            if (moveValue > maxValue) {
                maxValue = moveValue
                bestMove = move
            }
        }

        allPosBlockOrientations.removeAll { it.blockID == bestMove.movedBlock.blockID }

        return bestMove
    }

    private fun maxValue(state: AIGameState, depth: Int): Double {
        if (depth > depthLimit) {
            return heuristic.heuristicValue(state, color, opponentColor)
        }

        val posMoves = state.allPosMoves(allPosBlockOrientations)
        if (posMoves.isEmpty()) return heuristic.heuristicValue(state, color, opponentColor)

        val maxValue = posMoves.maxOf {
                move -> minValue(aIService.executeAICompMove(move, state), depth + 1)
        }

        return maxValue
    }

    private fun minValue(state: AIGameState, depth: Int): Double {
        if (depth > depthLimit) {
            return heuristic.heuristicValue(state, color, opponentColor)
        }

        val posMoves = state.allPosMoves(allPosBlockOrientations)
        if (posMoves.isEmpty()) return heuristic.heuristicValue(state, color, opponentColor)

        val minValue = posMoves.minOf {
                move -> maxValue(aIService.executeAICompMove(move, state), depth + 1)
        }

        return minValue
    }

    override fun getPlayerColor(): PlayerColor {
        return color
    }

    override fun syncAfterUndoRedo() {
        allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)
    }

    private fun setDepthLimit(countMoves: Int) {
        depthLimit = allowedEstimatedLeafNodes
        if (countMoves > 2) {
            depthLimit = log(allowedEstimatedLeafNodes.toDouble(), countMoves.toDouble()).toInt()
        }
        println("Depth Limit $depthLimit ($countMoves moves possible)")
    }
}