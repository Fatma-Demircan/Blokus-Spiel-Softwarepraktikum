package service

import entity.PlayerColor
import entity.PlayerType
import kotlin.test.*

/**
 * Test class for [PlayerService.undo] and [PlayerService.redo]
 */
class UndoRedoTest {

    private val rootService = RootService()

    /**
     * tests [PlayerService.undo] and [PlayerService.redo]
     *
     * two moves are made using [PlayerService.placeBlock] and the three resulting game states
     * are used to test undo/redo functionality
     */
    @Test
    fun undoRedoTest() {
        rootService.gameService.startGame(
            listOf("Robert", "Richard", "Ricarda", "Raphael"),
            listOf(PlayerColor.GREEN, PlayerColor.RED, PlayerColor.YELLOW, PlayerColor. BLUE),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 20
        )
        assertNotNull(rootService.currentGameState)

        // three game states: 1) empty   2) green in top left corner   3) green in top left, red in bottom left corner
        rootService.playerService.placeBlock(rootService.currentGameState!!.currentPlayer.blocks[0], -2 , -2)
        rootService.gameService.nextPlayer()
        rootService.playerService.placeBlock(rootService.currentGameState!!.currentPlayer.blocks[0], 17, -2)
        rootService.gameService.nextPlayer()

        // currently in third state
        assertEquals(PlayerColor.RED, rootService.currentGameState!!.gameBoard[19][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.gameBoard[0][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.previous!!.gameBoard[0][0])
        assertEquals(PlayerColor.BLANK, rootService.currentGameState!!.previous!!.previous!!.gameBoard[0][0])

        // undo should result in second state
        rootService.playerService.undo()
        assertEquals(PlayerColor.RED, rootService.currentGameState!!.next!!.gameBoard[19][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.next!!.gameBoard[0][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.gameBoard[0][0])
        assertEquals(PlayerColor.BLANK, rootService.currentGameState!!.previous!!.gameBoard[0][0])

        // redo should lead back to third state
        rootService.playerService.redo()
        assertEquals(PlayerColor.RED, rootService.currentGameState!!.gameBoard[19][0])
        assertEquals(PlayerColor.BLANK, rootService.currentGameState!!.previous!!.gameBoard[19][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.gameBoard[0][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.previous!!.gameBoard[0][0])
        assertEquals(PlayerColor.BLANK, rootService.currentGameState!!.previous!!.previous!!.gameBoard[0][0])

        // undoing twice should result in first state
        rootService.playerService.undo()
        rootService.playerService.undo()
        assertEquals(PlayerColor.RED, rootService.currentGameState!!.next!!.next!!.gameBoard[19][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.next!!.gameBoard[0][0])
        assertEquals(PlayerColor.BLANK, rootService.currentGameState!!.gameBoard[0][0])

        // redoing twice should result in third state
        rootService.playerService.redo()
        rootService.playerService.redo()
        assertEquals(PlayerColor.RED, rootService.currentGameState!!.gameBoard[19][0])
        assertEquals(PlayerColor.BLANK, rootService.currentGameState!!.previous!!.gameBoard[19][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.gameBoard[0][0])
        assertEquals(PlayerColor.GREEN, rootService.currentGameState!!.previous!!.gameBoard[0][0])
        assertEquals(PlayerColor.BLANK, rootService.currentGameState!!.previous!!.previous!!.gameBoard[0][0])
    }
}