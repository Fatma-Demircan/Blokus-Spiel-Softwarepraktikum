package service

import entity.PlayerColor
import entity.PlayerType
import java.lang.IllegalArgumentException
import kotlin.test.*


/**
 * Test class to test different configurations of [GameService.startGame] including...
 * - 4 player games with mix of human players and easy or difficult AI  (20x20)
 * - 3 player games with the three players sharing control of a dummy player (20x20)
 * - 2 player game with each player controlling two colors (20x20)
 * - 2 player game on a smaller board  (14x14)
 */
class StartGameTest {

    private val rootService = RootService()

    /**
     * tests different configurations of 4 player games
     * - checks if every player is initialised correctly
     * - checks if a move was made if first player is AI (as currently implemented in startGame)
     */
    @Test
    fun fourPlayerGameTest() {
        rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3", "Player4"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(4, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[2].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[3].blocks.size)


        rootService.gameService.startGame(
            listOf("Player1", "AI1", "Player2", "AI2"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HARD_AI, PlayerType.HUMAN, PlayerType.HARD_AI),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(4, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[2].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[3].blocks.size)

        rootService.gameService.startGame(
            listOf("AI1", "AI2", "AI3", "AI4"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.HARD_AI, PlayerType.HARD_AI, PlayerType.HARD_AI, PlayerType.HARD_AI),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(4, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[2].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[3].blocks.size)

        // too many player names
        assertFailsWith<IllegalArgumentException> {    rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3", "Player4", "Player 5"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        ) }

        // too many player colors
        assertFailsWith<IllegalArgumentException> {    rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3", "Player4"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW, PlayerColor.BLANK),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        ) }

        // two player chose same color
        assertFailsWith<IllegalArgumentException> {    rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3", "Player4"),
            listOf(PlayerColor.GREEN, PlayerColor.GREEN, PlayerColor.RED, PlayerColor.YELLOW, PlayerColor.BLANK),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        ) }

        // too many player types
        assertFailsWith<IllegalArgumentException> {    rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3", "Player4"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        ) }

        // invalid board size
        assertFailsWith<IllegalArgumentException> {    rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3", "Player4"),
            listOf(PlayerColor.GREEN, PlayerColor.BLUE, PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 21
        ) }
    }

    /**
     * tests different configurations of 3 player games
     * - checks if players are initialised correctly
     * - checks if startGame() fails if the order of colors is wrong
     * - checks if a move was made if first player is AI (as currently implemented in startGame)
     */
    @Test
    fun threePlayerGameTest() {
        rootService.gameService.startGame(
            listOf("Player1", "Player2", "Player3"),
            listOf(PlayerColor.BLUE, PlayerColor.YELLOW, PlayerColor.RED),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(4, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[2].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[3].blocks.size)

        rootService.gameService.startGame(
            listOf("AI", "Player1", "Player2"),
            listOf(PlayerColor.BLUE, PlayerColor.YELLOW, PlayerColor.RED),
            listOf(PlayerType.HARD_AI, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(4, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[2].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[3].blocks.size)


    }

    /**
     * tests different configurations of 2 player games
     * - checks if players are initialised correctly
     * - checks if startGame() fails if the order of colors is wrong
     */
    @Test
    fun twoPlayer20GameTest() {
        rootService.gameService.startGame(
            listOf("Player1", "Player2"),
            listOf(PlayerColor.BLUE, PlayerColor.RED),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(4, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[2].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[3].blocks.size)

        rootService.gameService.startGame(
            listOf("AI", "Player"),
            listOf(PlayerColor.BLUE, PlayerColor.RED),
            listOf(PlayerType.HARD_AI, PlayerType.HUMAN),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(4, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[2].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[3].blocks.size)
    }

    /**
     * tests 2 player games on a 14x14 board
     * - checks if players are initialised correctly
     * - checks if a move was made if first player is AI (as currently implemented in startGame)
     */
    @Test
    fun twoPlayer14GameTest() {
        rootService.gameService.startGame(
            listOf("Player1", "Player2"),
            listOf(PlayerColor.BLUE, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 14
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(2, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)

        rootService.gameService.startGame(
            listOf("AI", "Player"),
            listOf(PlayerColor.RED, PlayerColor.GREEN),
            listOf(PlayerType.HARD_AI, PlayerType.HUMAN),
            boardSize = 14
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(2, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)

        rootService.gameService.startGame(
            listOf("AI1", "AI2"),
            listOf(PlayerColor.RED, PlayerColor.YELLOW),
            listOf(PlayerType.HARD_AI, PlayerType.HARD_AI),
            boardSize = 14
        )
        assertNotNull(rootService.currentGameState)
        assertEquals(2, rootService.currentGameState!!.players.size)
        assertEquals(21, rootService.currentGameState!!.players[0].blocks.size)
        assertEquals(21, rootService.currentGameState!!.players[1].blocks.size)
    }
}