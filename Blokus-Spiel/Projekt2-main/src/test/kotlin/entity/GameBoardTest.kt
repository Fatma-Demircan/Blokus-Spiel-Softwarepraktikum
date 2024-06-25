package entity

import kotlin.test.*

/**
 * Doc
 * TODO DOC
 */
class GameBoardTest {

    /**
     * Test Initialization of game board matrix.
     */
    @Test
    fun testBoardInit() {
        val testGame = BlokusGameState(
            players = listOf(),
            currentPlayer = Player("Testplayer", PlayerType.HUMAN, PlayerColor.BLANK, mutableListOf())
        )
        assertEquals(PlayerColor.BLANK, testGame.gameBoard[0][0])
        assertEquals(PlayerColor.BLANK, testGame.gameBoard[10][10])
        assertEquals(PlayerColor.BLANK, testGame.gameBoard[19][19])
    }
}