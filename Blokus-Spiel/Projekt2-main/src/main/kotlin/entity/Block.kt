package entity

import java.io.Serializable

/**
 * Data class for the single typ of game elements that the game "Blokus" knows: blocks.
 *
 * @param [blockID] An enum id, which identifies the blocks form.
 * @param [shape] A boolean matrix, which represents the blocks shape. Default = empty matrix.
 * @param [color] A color enum, which represents the blocks color.
 */
data class Block (val blockID : BlockID,
                  val shape : Array<Array<Boolean>> = Array(5){ Array(5){ false } },
                  var color : PlayerColor = PlayerColor.BLANK) : Serializable{

    companion object{
        private const val serialVersionUID: Long = 1234
    }

    /**
     * after a block is created, the shape of the block will be initialized by the specification of the blockID
     */
    init {
        when(blockID) {
            BlockID.ONE_ONE -> shape[2][2] = true
            BlockID.TWO_ONE -> {
                shape[2][1] = true
                shape[2][2] = true
            }
            BlockID.THREE_ONE -> {
                shape[2][1] = true
                shape[2][2] = true
                shape[1][2] = true
            }
            BlockID.THREE_TWO -> {
                shape[2][1] = true
                shape[2][2] = true
                shape[2][3] = true
            }
            BlockID.FOUR_ONE -> {
                shape[3][1] = true
                shape[2][1] = true
                shape[2][2] = true
                shape[1][2] = true
            }
            BlockID.FOUR_TWO -> {
                shape[1][1] = true
                shape[2][1] = true
                shape[1][2] = true
                shape[2][2] = true
            }
            BlockID.FOUR_THREE -> {
                shape[2][1] = true
                shape[2][2] = true
                shape[1][2] = true
                shape[2][3] = true
            }
            BlockID.FOUR_FOUR -> {
                shape[2][1] = true
                shape[2][2] = true
                shape[2][3] = true
                shape[3][3] = true
            }
            BlockID.FOUR_FIVE -> {
                shape[2][0] = true
                shape[2][1] = true
                shape[2][2] = true
                shape[2][3] = true
            }
            BlockID.FIVE_ONE -> {
                shape[1][2] = true
                shape[2][1] = true
                shape[2][2] = true
                shape[3][1] = true
                shape[3][2] = true
            }
            BlockID.FIVE_TWO -> {
                shape[0][2] = true
                shape[1][2] = true
                shape[2][2] = true
                shape[2][1] = true
                shape[3][1] = true
            }
            BlockID.FIVE_THREE -> {
                shape[2][0] = true
                shape[2][1] = true
                shape[2][2] = true
                shape[2][3] = true
                shape[3][3] = true
            }
            BlockID.FIVE_FOUR -> {
                shape[2][0] = true
                shape[2][1] = true
                shape[2][2] = true
                shape[2][3] = true
                shape[2][4] = true
            }
            BlockID.FIVE_FIVE -> {
                shape[1][2] = true
                shape[1][3] = true
                shape[2][2] = true
                shape[3][2] = true
                shape[3][3] = true
            }
            BlockID.FIVE_SIX -> {
                shape[3][1] = true
                shape[3][2] = true
                shape[2][2] = true
                shape[1][2] = true
                shape[1][3] = true
            }
            BlockID.FIVE_SEVEN -> {
                shape[3][1] = true
                shape[2][1] = true
                shape[2][2] = true
                shape[1][2] = true
                shape[1][3] = true
            }
            BlockID.FIVE_EIGHT -> {
                shape[2][0] = true
                shape[2][1] = true
                shape[2][2] = true
                shape[1][2] = true
                shape[0][2] = true
            }
            BlockID.FIVE_NINE -> {
                shape[2][1] = true
                shape[2][2] = true
                shape[1][3] = true
                shape[2][3] = true
                shape[3][3] = true
            }
            BlockID.FIVE_TEN -> {
                shape[3][1] = true
                shape[3][2] = true
                shape[2][2] = true
                shape[1][2] = true
                shape[2][3] = true
            }
            BlockID.FIVE_ELEVEN -> {
                shape[2][1] = true
                shape[1][2] = true
                shape[2][2] = true
                shape[2][3] = true
                shape[3][2] = true
            }
            BlockID.FIVE_TWELVE -> {
                shape[2][1] = true
                shape[1][2] = true
                shape[2][2] = true
                shape[2][3] = true
                shape[2][4] = true
            }
            BlockID.PASSED -> {}
        }
    }

    /**
     * returns a String, that visualizes the shape of the block
     *
     * @return visualisation of shape of block
     */
    override fun toString(): String {
        var shapeString = "\n"
        shape.forEach { row ->
            row.forEach { shapeString += if (it) 1 else 0 }
            shapeString += "\n"
        }
        return shapeString
    }

    /**
     * returns a deep copy of the Block object
     *
     * @return a new Block object
     */
    fun copy(): Block {
        val newBlock = Block(blockID, color = color)
        for (posY in 0..4) {
            for (posX in 0..4) {
                newBlock.shape[posY][posX] = shape[posY][posX]
            }
        }
        return newBlock
    }

    /**
     * returns the size of the block
     *
     * @return size of the block
     */
    fun tileCount(): Int {
        var tileCount = 0
        shape.forEach { row -> row.forEach {
            if (it) tileCount += 1
        }}
        return tileCount
    }
}