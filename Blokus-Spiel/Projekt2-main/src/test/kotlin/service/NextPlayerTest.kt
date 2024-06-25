package service

import entity.PlayerColor
import entity.PlayerType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class to test functionality of [GameService.nextPlayer]
 */
class NextPlayerTest {

    private val rootService = RootService()

    /**
     * tests [GameService.nextPlayer] in a local four player game and the first player is AI
     * - AI needs to make a move when nextPlayer() is called
     */
    @Test
    fun nextPlayer20AI() {
        rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3", "Player4"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.RANDOM_AI, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        )
        rootService.gameService.nextPlayer()
        assert(!emptyCorners20())
    }

    /**
     * tests [GameService.nextPlayer] in a local two player game and the first player is AI
     * - AI needs to make a move when nextPlayer() is called
     */
    @Test
    fun nextPlayer14AI() {
        rootService.gameService.startGame(
            listOf("Player1", "Player2"),
            listOf(PlayerColor.RED, PlayerColor.BLUE),
            listOf(PlayerType.RANDOM_AI, PlayerType.HUMAN),
            boardSize = 14
        )
        rootService.gameService.nextPlayer()
        assert(!emptyCorners14())
    }

    /**
     * tests [GameService.nextPlayer] in a three player game with a dummy player
     * - currentDummyController has to be set to the next player after it was the dummy's turn
     */
    @Test
    fun nextPlayerDummy() {
        rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3"),
            listOf(PlayerColor.BLUE, PlayerColor.YELLOW, PlayerColor.RED),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        )
        rootService.gameService.nextPlayer()
        rootService.gameService.nextPlayer()
        rootService.gameService.nextPlayer()
        assertEquals(
            0,
            rootService.currentGameState!!.players.indexOf(rootService.currentGameState!!.currentDummyController)
        )
    }

    private fun emptyCorners20(): Boolean {
        val board = rootService.currentGameState!!.gameBoard
        return (board[0][0] != PlayerColor.BLANK
                || board[0][19] != PlayerColor.BLANK
                || board[19][0] != PlayerColor.BLANK
                || board[19][19] != PlayerColor.BLANK)
    }

    private fun emptyCorners14(): Boolean {
        val board = rootService.currentGameState!!.gameBoard
        return (board[0][0] != PlayerColor.BLANK
                || board[0][13] != PlayerColor.BLANK
                || board[13][0] != PlayerColor.BLANK
                || board[13][13] != PlayerColor.BLANK)
    }
}
