package service.ai

import entity.BlokusGameState
import entity.Player
import entity.PlayerColor
import entity.PlayerType
import service.RootService
import kotlin.system.measureTimeMillis

/**
 * just for other tests
 */
fun main() {

    val rootService = RootService()
    val aiService = rootService.aIService

    val playerNames = listOf("Peter", "Waldtraud")
    val playerColors = listOf(PlayerColor.RED, PlayerColor.BLUE)
    val playerTypes = listOf(PlayerType.MONTE_CARLO_AI, PlayerType.MINI_MAX_AI)

    val players = (0..1).map { Player(playerNames[it], playerTypes[it], playerColors[it]) }
    val startGameState = BlokusGameState(players, currentPlayer = players[0], boardSize = 14)
    rootService.currentGameState = startGameState

    // create the AI objects for the AI players
    for (i in playerTypes.indices) {
        if (playerTypes[i] in rootService.aIService.aiValueList)
            rootService.aIService.createAI(playerTypes[i], playerColors[i])
    }

    var timePlayer1: Long = 0
    val timesPlayer1: MutableList<Long> = mutableListOf()
    var timePlayer2: Long = 0
    val timesPlayer2: MutableList<Long> = mutableListOf()
    var player1madeMove: Boolean
    var player2madeMove: Boolean
    var time: Long
    var game: BlokusGameState

    while (true) {
        time = measureTimeMillis {
            player1madeMove = aiService.makeMove()
        }
        timePlayer1 += time
        timesPlayer1.add(time)
        game = rootService.currentGameState!!
        game.currentPlayer =  game.players[(game.players.indexOf(game.currentPlayer) + 1) % game.players.size]
        println(rootService.currentGameState!!)
        println()

        time = measureTimeMillis {
            player2madeMove = aiService.makeMove()
        }
        timePlayer2 += time
        timesPlayer2.add(time)
        game = rootService.currentGameState!!
        game.currentPlayer =  game.players[(game.players.indexOf(game.currentPlayer) + 1) % game.players.size]
        println(rootService.currentGameState!!)
        println()

        if (!(player1madeMove || player2madeMove)) break
    }

    println()

}