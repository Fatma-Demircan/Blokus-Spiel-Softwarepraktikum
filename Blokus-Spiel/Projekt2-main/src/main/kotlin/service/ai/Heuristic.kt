package service.ai

import entity.PlayerColor
import service.ai.AIGameState

/**
 * Some AIs may use Heuristics which should be implemented using the Strategy pattern to support the co-existence of
 * multiple heuristics approaches.
 */
interface Heuristic {

    /**
     * takes the computed game state and returns the heuristic value of it
     *
     * @param state the computed game state
     * @param ownColor the color of the player who should win
     * @param opponentColor the color of the player who should lose
     * @return a [Double] value evaluating the game state
     */
    fun heuristicValue(state: AIGameState, ownColor: PlayerColor, opponentColor: PlayerColor): Double
}