package service.ai

import entity.BlokusGameState
import entity.PlayerColor
import service.RootService
import kotlin.random.Random

/**
 * AI class that chooses a random move out of all possible moves.
 */
class RandomAI (private val rootService: RootService, private val color: PlayerColor): AIType {

    private val aIService: AIService = rootService.aIService
    private var allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)

    override fun getMove(currentGameState: BlokusGameState): Move {
        val state = AIGameState(true, currentGameState.gameBoard, currentGameState.currentPlayer.color,
            currentGameState.currentPlayer.blocks.size == 21, 21 - currentGameState.currentPlayer.blocks.size)
        val moves = state.allPosMoves(allPosBlockOrientations)
        if (moves.isEmpty()) return aIService.emptyMove
        val choosenMove = moves[Random.nextInt(0, moves.size)]
        allPosBlockOrientations.removeAll { it.blockID == choosenMove.movedBlock.blockID }

        return choosenMove
    }

    override fun getPlayerColor(): PlayerColor {
        return color
    }

    override fun syncAfterUndoRedo() {
        allPosBlockOrientations = getAllPosBlockOrientations(rootService, color)
    }
}