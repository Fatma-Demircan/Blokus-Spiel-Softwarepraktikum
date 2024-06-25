package service.ai

import entity.PlayerColor

/**
 * Heuristic that is used in games with more than one player. Does not factor in the opponents positions.
 * It tries to place the largest blocks first and to increase the count of own corners.
 */
class LargestFirstIncreaseNoDuoHeuristic (private val aiService: AIService): Heuristic {
    override fun heuristicValue(state: AIGameState, ownColor: PlayerColor, opponentColor: PlayerColor): Double {
        val value = state.countPoints(ownColor) + aiService.countCorners(state.gameBoard, ownColor) * 2
        return value.toDouble()
    }
}