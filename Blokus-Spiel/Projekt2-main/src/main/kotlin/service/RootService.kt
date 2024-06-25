package service

import entity.BlokusGameState
import service.ai.AIService
import view.Refreshable


/**
 * Main class of the service layer for the Swim game. Provides access
 * to all other service classes and holds the [currentGameState] state for the
 * services to access.
 */
class RootService {
    val gameService = GameService(this)
    val playerService = PlayerService(this)
    val aIService = AIService(this)
    val networkService = NetworkService(this)

    /**
     * The currently active game. Can be `null`, if no game has started yet.
     */
    var currentGameState : BlokusGameState? = null

    /**
     * Adds the provided [newRefreshable] to all services connected
     * to this root service
     */
    private fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerService.addRefreshable(newRefreshable)
        aIService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}