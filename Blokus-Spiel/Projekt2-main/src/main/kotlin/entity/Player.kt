package entity

import java.io.Serializable
/**
 * Entity to represent a player in the game "Blokus". Besides having a [name] and a [score], it also contains the
 * information, whether the players final move was to place the "ONE_ONE Block".
 *
 * @param [name] Name of the player.
 * @param [type] Type of Player (dummy, ai, human, ...).
 * @param [color] Color of the player and his [blocks].
 * @param [blocks] A list of maximum 21 distinct player [blocks].
 * @param [score] Current score of the player. Default = -89 (points at the beginning)
 * @param [isPlayable] Whether the player can still place blocks or not
 * @param [lastBlockEqualsOneOne] A Boolean, which will be set to true, if the players final move was to place the
 * "ONE_ONE Block". Default = false.
 */
data class Player (
    var name : String,
    val type: PlayerType,
    val color : PlayerColor,
    val blocks : MutableList<Block> = BlockID.values().map { Block(it, color = color) }.toMutableList(),
    var score : Int = -89,
    var isPlayable : Boolean = true,
    var lastBlockEqualsOneOne : Boolean = false) : Serializable{


    companion object{
        private const val serialVersionUID: Long = 12345
    }
    /**
     * returns a deep copy of the Player object
     *
     * @return a new Player object
     */
    fun copy(): Player {
        return Player(name, type, color, blocks.map { it.copy() }.toMutableList(), score, isPlayable, lastBlockEqualsOneOne)
    }

    /**
     * removes the passed block on initialisation
     */
    init {
        blocks.removeAll { it.blockID == BlockID.PASSED }
    }
}