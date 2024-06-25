package service

import entity.Block
import entity.BlockID
import entity.PlayerColor
import entity.PlayerType
import kotlin.test.*

/**
 * Test class for all the methods related to the block placement. Especially [PlayerService.placeBlock] and
 * [PlayerService.placeBlockValid]
 */
class PlaceBlockTest {

    private val rootService = RootService()
    private val playerService = rootService.playerService

    /**
     * tests [PlayerService.placeBlockValid] with three Blocks on a 14x14 board with two players
     */
    @Test
    fun placeBlockValidTest() {
        rootService.gameService.startGame(
            listOf("Robert", "Richard"),
            listOf(PlayerColor.GREEN, PlayerColor.RED),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 14
        )

        val blockOneOne = Block(BlockID.ONE_ONE, color = PlayerColor.GREEN)
        // try to place 1x1 block outside the game board, hence false is expected
        assertFalse(playerService.placeBlockValid(blockOneOne, 2, -2303943))
        // check 1x1 block placed in upper left corner
        assert(playerService.placeBlockValid(blockOneOne, -2, -2))
        // check 1x1 block placed out of game board
        assert(!playerService.placeBlockValid(blockOneOne, -3, -2))
        // check 1x1 block placed in the middle of game board with no diagonal adjacent same color tiles
        assert(!playerService.placeBlockValid(blockOneOne,5, 5))

        // place 1x1 block in upper left corner
        rootService.currentGameState!!.gameBoard[0][0] = PlayerColor.GREEN
        rootService.gameService.nextPlayer()

        val blockFourTwo= Block(BlockID.FOUR_TWO, color = PlayerColor.RED)
        // check 2x2 block placed in upper right corner
        assert(playerService.placeBlockValid(blockFourTwo, -1, 11))
        // check 2x2 block placed next to 1x1 red block
        assert(!playerService.placeBlockValid(blockFourTwo, 0, 0))

        // place 2x2 block in upper right corner
        rootService.currentGameState!!.gameBoard[0][12] = PlayerColor.RED
        rootService.currentGameState!!.gameBoard[0][13] = PlayerColor.RED
        rootService.currentGameState!!.gameBoard[1][12] = PlayerColor.RED
        rootService.currentGameState!!.gameBoard[1][13] = PlayerColor.RED
        rootService.gameService.nextPlayer()

        val blockFiveEleven = Block(BlockID.FIVE_ELEVEN, color = PlayerColor.GREEN)
        // check cross block placed below 1x1 block in upper left corner
        assert(playerService.placeBlockValid(blockFiveEleven, 0, -1))
        // check cross block placed parallel adjacent to 1x1 block in upper left corner
        assert(!playerService.placeBlockValid(blockFiveEleven, 0, 0))
        // check 1x1 block placed above 1x1 block in upper left corner
        assert(!playerService.placeBlockValid(blockOneOne, 0, 0))
    }

    /**
     * tests the [PlayerService.placeBlock] method. Specifically the aspects that are not covered by other tests.
     */
    @Test
    fun placeBlockTest() {
        rootService.gameService.startGame(
            listOf("Robert", "Richard"),
            listOf(PlayerColor.GREEN, PlayerColor.RED),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 14
        )
        assertFalse(playerService.placeBlockValid(Block(BlockID.FIVE_SEVEN), -190, 23))
        // check if the lastBlockEqualsOneOne flag is set to false
        assertFalse(rootService.currentGameState!!.currentPlayer.lastBlockEqualsOneOne)
        playerService.placeBlock(rootService.currentGameState!!.currentPlayer.blocks[1], -2, -1)
        rootService.currentGameState!!.currentPlayer.blocks.removeAll { it.blockID != BlockID.ONE_ONE }
        playerService.placeBlock(rootService.currentGameState!!.currentPlayer.blocks[0], -1, 0)
        // now the player has placed the one one block as the last block, hence the flag lastBlockEqualsOneOne
        // should be set to true
        assert(rootService.currentGameState!!.currentPlayer.lastBlockEqualsOneOne)
    }

    /**
     * test for the helper method [PlayerService.isStartCorner]
     */
    @Test
    fun isStartCornerTest() {
        rootService.gameService.startGame(
            listOf("Robert", "Richard"),
            listOf(PlayerColor.GREEN, PlayerColor.RED),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 14
        )
        assert(rootService.playerService.isStartCorner(13, 13, PlayerColor.RED))
    }
}