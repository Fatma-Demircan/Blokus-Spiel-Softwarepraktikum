package service

import entity.BlockID
import entity.Player
import entity.PlayerColor
import entity.PlayerType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for the [GameService.updateScore] method
 */
class UpdateScoreTest {

    val rootService = RootService()
    val gameService = rootService.gameService

    /**
     * tests the [GameService.updateScore] method using one Player object and removing the blocks from it
     */
    @Test
    fun scoringUpdateTest() {
        val player = Player("Peter", PlayerType.HUMAN, PlayerColor.BLUE)
        gameService.updateScore(player)
        assertEquals(player.score, -89)

        player.blocks.removeAll { it.blockID in arrayOf(
            BlockID.FIVE_TEN, BlockID.FOUR_ONE, BlockID.ONE_ONE, BlockID.TWO_ONE
        ) }
        gameService.updateScore(player)
        assertEquals(player.score, -77)

        player.blocks.removeAll { it.blockID in arrayOf(
            BlockID.FIVE_ONE, BlockID.FIVE_TWO, BlockID.FIVE_THREE, BlockID.FIVE_FOUR
        ) }
        gameService.updateScore(player)
        assertEquals(player.score, -57)

        player.blocks.removeAll { it.blockID in arrayOf(
            BlockID.FIVE_FIVE, BlockID.FIVE_SIX, BlockID.FIVE_SEVEN, BlockID.FIVE_EIGHT, BlockID.FIVE_NINE,
            BlockID.FIVE_ELEVEN, BlockID.FIVE_TWELVE, BlockID.THREE_TWO
        ) }
        gameService.updateScore(player)
        assertEquals(player.score, -19)

        player.blocks.removeAll { it.blockID in arrayOf(
            BlockID.FOUR_TWO, BlockID.FOUR_THREE, BlockID.FOUR_FOUR, BlockID.FOUR_FIVE
        ) }
        gameService.updateScore(player)
        assertEquals(player.score, -3)

        player.blocks.removeAll { it.blockID == BlockID.THREE_ONE }
        gameService.updateScore(player)
        assertEquals(player.score, 15)

        player.lastBlockEqualsOneOne = true
        gameService.updateScore(player)
        assertEquals(player.score, 20)
    }
}