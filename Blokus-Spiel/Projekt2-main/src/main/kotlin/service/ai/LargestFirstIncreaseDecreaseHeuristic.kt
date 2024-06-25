package service.ai

import entity.PlayerColor

/**
 * Heuristic that is used by the MiniMax algorithm. It tries to place the largest blocks first and to increase
 * the own corners while decreasing the corners of the opponent.
 */
class LargestFirstIncreaseDecreaseHeuristic(private val aiService: AIService): Heuristic {
    override fun heuristicValue(state: AIGameState, ownColor: PlayerColor, opponentColor: PlayerColor): Double {
        val value = state.countPoints(ownColor) + aiService.countCorners(state.gameBoard, ownColor) * 2 -
                aiService.countCorners(state.gameBoard, opponentColor)
        return value.toDouble()
    }
}