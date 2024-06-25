package entity

import service.RootService
import kotlin.test.*

/**
 * test class for multiple helper functions of the entity layer
 */
class EntityLayerTests {

    private val rootService = RootService()

    /**
     * test the toString method of [Block]
     */
    @Test
    fun blockToStringTest() {
        val block = Block(BlockID.ONE_ONE)
        assertEquals("\n00000\n00000\n00100\n00000\n00000\n", block.toString())
        println(block)
    }

    /**
     * test the toString method of BlokusGameState
     */
    @Test
    fun gameStateToStringTest() {
        rootService.gameService.startGame(listOf("Someone", "Another one", "Yet another one", "Finally the last one"),
            listOf(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN), 20)
        listOf(Pair(-2, -2), Pair(-2, 17), Pair(17, 17), Pair(17, -2)).forEach {
            val currentPlayer = rootService.currentGameState!!.currentPlayer
            rootService.playerService.placeBlock(currentPlayer.blocks[0], it.first, it.second)
            rootService.gameService.nextPlayer()
        }
        assertEquals("R------------------B\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "--------------------\n" +
                "Y------------------G\n" +
                "Points RED: 1\n" +
                "Points BLUE: 1\n" +
                "Points GREEN: 1\n" +
                "Points YELLOW: 1\n", rootService.currentGameState!!.toString())

    }

    /**
     * tests the copying of the game state
     */
    @Test
    fun gameStateCopyTest() {
        rootService.gameService.startGame(listOf("Someone", "Another one", "Yet another one", "Finally the last one"),
            listOf(PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN, PlayerColor.YELLOW),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN), 20)
        val copyOfGameBoard = rootService.currentGameState!!.copy()
        // test set game board
        rootService.currentGameState!!.gameBoard = copyOfGameBoard.gameBoard
        assertEquals(rootService.currentGameState!!.gameBoard, copyOfGameBoard.gameBoard)
        // test if the dummy controller is set as null
        assertNull(copyOfGameBoard.currentDummyController)
        copyOfGameBoard.currentDummyController = copyOfGameBoard.currentPlayer
        val anotherCopyOfGameBoard = copyOfGameBoard.copy()
        assertEquals(anotherCopyOfGameBoard.currentPlayer, anotherCopyOfGameBoard.currentDummyController)
    }
}