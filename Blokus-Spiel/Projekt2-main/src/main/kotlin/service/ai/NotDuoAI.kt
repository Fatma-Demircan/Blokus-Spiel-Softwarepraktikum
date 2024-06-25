package service.ai

import entity.BlokusGameState
import entity.PlayerColor
import service.RootService

/**
 * An AI class that is used in games with more than two players. It relies solely on a heuristic and does not
 * calculate into the future.
 */
class NotDuoAI (private val rootService: RootService, private val color: PlayerColor): AIType {

    private val aIService: AIService = rootService.aIService
    private val heuristic = LargestFirstIncreaseNoDuoHeuristic(aIService)
    private var allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)

    override fun getMove(currentGameState: BlokusGameState): Move {
        val state = AIGameState(true, currentGameState.gameBoard, currentGameState.currentPlayer.color,
            currentGameState.currentPlayer.blocks.size == 21, 21 - currentGameState.currentPlayer.blocks.size)

        var bestMove: Move = aIService.emptyMove
        var bestValue: Double = Double.MIN_VALUE
        state.allPosMoves(allPosBlockOrientations).forEach { move ->
            val moveValue = heuristic.heuristicValue(aIService.executeAICompMove(move, state), color, PlayerColor.BLANK)
            if (moveValue > bestValue) {
                bestMove = move
                bestValue = moveValue
            }
        }
        allPosBlockOrientations.removeAll { it.blockID == bestMove.movedBlock.blockID }

        return bestMove
    }

    override fun getPlayerColor(): PlayerColor {
        return color
    }

    override fun syncAfterUndoRedo() {
        allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)
    }
}