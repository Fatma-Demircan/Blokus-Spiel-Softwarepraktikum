package service.ai

import entity.Block
import entity.BlokusGameState
import entity.PlayerColor
import service.RootService

/**
 * this interface has to be implemented by every AI object
 */
interface AIType {

    /**
     * returns the move selected by the AI
     *
     * @param currentGameState the current game state
     * @return a [Move] object containing the moved block and the position
     */
    fun getMove(currentGameState: BlokusGameState): Move

    /**
     * returns the player color of the AI object to identify the responsibilities of multiple AI objects
     *
     * @return a [PlayerColor] value
     */
    fun getPlayerColor(): PlayerColor

    /**
     * Generates a list containing all different possible block orientations of the block, that the user holds.
     * The list is sorted in descending order of the size of blocks.
     *
     * @param rootService the root service
     * @param color the color that the AI is playing
     * @return a mutable list of all the block orientations
     */
    fun getAllPosBlockOrientations(rootService: RootService, color: PlayerColor): MutableList<Block> {
        val allPosBlockOrientations = rootService.aIService.allBlockOrientations.map {
            it.copy()
        }.toMutableList()
        allPosBlockOrientations.removeAll { it.blockID !in rootService.currentGameState!!.players
            .first { player -> player.color == color }.blocks.map { block -> block.blockID } }
        allPosBlockOrientations.sortByDescending { it.tileCount() }
        allPosBlockOrientations.forEach { it.color = color }
        return allPosBlockOrientations
    }

    /**
     * should be called on an AI after a user performed a undo or redo operation
     */
    fun syncAfterUndoRedo()
}