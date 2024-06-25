package service.ai

import entity.Block

/**
 * Simple class that represents a move made by an AI. Contains the moved block and the position it is moved to.
 */
class Move (val movedBlock: Block, val posY: Int, val posX: Int) {

    override fun toString(): String {
        return "The block ${movedBlock}will be placed at Y=${posY} and X=${posX}"
    }
}