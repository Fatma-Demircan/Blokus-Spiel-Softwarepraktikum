package service

import entity.*
import kotlin.test.Test

/**
 * Test class for ensuring that the rotating and flipping of the blocks functions properly
 */
class BlockManipulationTest {

    private val rootService = RootService()
    private val playerService = rootService.playerService

    /**
     * test for [PlayerService.rotateBlock] method
     */
    @Test
    fun rotationTest() {
        val blockFiveFour = Block(BlockID.FIVE_FOUR)
        val blockFiveFourRotShape: Array<Array<Boolean>> =
            (0..4).map { arrayOf(false, false, true, false, false) }.toTypedArray()
        playerService.rotateBlock(blockFiveFour)
        assert(areShapesEqual(blockFiveFour.shape, blockFiveFourRotShape))
        playerService.rotateBlock(blockFiveFour)
        assert(areShapesEqual(blockFiveFour.shape, Block(BlockID.FIVE_FOUR).shape))

        val blockFourOne = Block(BlockID.FOUR_ONE)
        val blockFourOneRotShape: Array<Array<Boolean>> = arrayOf(
            arrayOf(false, false, false, false, false),
            arrayOf(false, true, true, false, false),
            arrayOf(false, false, true, true, false),
            arrayOf(false, false, false, false, false),
            arrayOf(false, false, false, false, false),
        )
        playerService.rotateBlock(blockFourOne)
        assert(areShapesEqual(blockFourOne.shape, blockFourOneRotShape))
    }

    /**
     * test for [PlayerService.flipBlock] method
     */
    @Test
    fun flipTest() {
        // flipping of the following block should not change the block
        val blockFiveFour = Block(BlockID.FIVE_FOUR)
        playerService.flipBlock(blockFiveFour)
        assert(areShapesEqual(blockFiveFour.shape, Block(BlockID.FIVE_FOUR).shape))

        val blockFourOne = Block(BlockID.FOUR_ONE)
        val blockFourOneFlipShape: Array<Array<Boolean>> = arrayOf(
            arrayOf(false, false, false, false, false),
            arrayOf(false, false, true, false, false),
            arrayOf(false, false, true, true, false),
            arrayOf(false, false, false, true, false),
            arrayOf(false, false, false, false, false),
        )
        playerService.flipBlock(blockFourOne)
        assert(areShapesEqual(blockFourOne.shape, blockFourOneFlipShape))
    }

    private fun areShapesEqual(shape1: Array<Array<Boolean>>, shape2: Array<Array<Boolean>>): Boolean {
        for (posY in 0..4) {
            for (posX in 0..4) {
                if (shape1[posY][posX] != shape2[posY][posX]) return false
            }
        }
        return true
    }
}