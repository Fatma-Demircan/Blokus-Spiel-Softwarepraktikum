package service.ai

import entity.PlayerColor
import entity.PlayerType
import service.RootService

/**
 * just for other tests
 */
fun main() {

    val rootService = RootService()
    val aiService = rootService.aIService

    rootService.gameService.startGame(
        listOf("Peter", "Waldtraud"),
        listOf(PlayerColor.RED, PlayerColor.BLUE),
        listOf(PlayerType.MINI_MAX_AI, PlayerType.MINI_MAX_AI),
        boardSize = 14
    )


    aiService.makeMove()
    println(rootService.currentGameState!!)


}