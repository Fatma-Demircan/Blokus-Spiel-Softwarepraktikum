package service

import entity.Block
import entity.BlockID
import entity.PlayerColor


/**
 * Provides methods for block manipulation, block placement and undo/redo.
 */
class PlayerService(private val rootService: RootService): AbstractRefreshingService() {

    /**
     * Places a given [block] on the game board if the placement at the position
     * given by [coordX] and [coordX] is valid
     *
     * To make moves re- and undoable the block is placed on the board of a new
     * BlokusGameState which is pointing to the previous state
     */
    fun placeBlock(block : Block, coordY : Int, coordX : Int): Boolean {
        if (!placeBlockValid(block, coordY, coordX)) return false

        val pieceCoords = emptyList<Pair<Int, Int>>().toMutableList()

        val nextGameState = rootService.currentGameState!!.copy()
        nextGameState.previous = rootService.currentGameState
        nextGameState.next = null
        rootService.currentGameState!!.next = nextGameState
        rootService.currentGameState = nextGameState
        for (posY in 0..4) {
            for (posX in 0..4) {
                if (block.shape[posY][posX]) {
                    rootService.currentGameState!!.gameBoard[posY+coordY][posX+coordX] = block.color

                    //Coordinates needed for the network message
                    pieceCoords.add(Pair(posX+coordX, posY+coordY))
                }
            }
        }
        //calls for the network to send a network message containing the set piece
        if(rootService.networkService.connectionState == ConnectionState.PLAYING_MY_TURN) {
            rootService.networkService.sendPiece(pieceCoords, block.blockID)
        }

        val currentGameState = rootService.currentGameState!!
        currentGameState.currentPlayer.lastBlockEqualsOneOne = currentGameState.currentPlayer.blocks.size == 1
                && block.blockID == BlockID.ONE_ONE
        // remove the placed block from the hand blocks of the player
        rootService.currentGameState!!.currentPlayer.blocks.removeAll { it.blockID == block.blockID }
        updateIsPlayable()
        rootService.gameService.updateScore(rootService.currentGameState!!.currentPlayer)
        onAllRefreshables { refreshAfterBlockPlaced(rootService.currentGameState!!.gameBoard, block, coordY, coordX) }

        return true
    }


    /**
     * Rotates the given block by 90 degrees clockwise
     * @param block the block, which shape will be rotated
     */
    fun rotateBlock(block : Block){
        val oldShape = block.shape.map { it.clone() }.toTypedArray()
        for (posY in 0..4) {
            for (posX in 0..4) {
                block.shape[posY][posX] = oldShape[4 - posX][posY]
            }
        }

        onAllRefreshables { refreshAfterBlockManipulation(block) }
    }

    /**
     * Mirrors the given block horizontally
     * @param block the block, which shape will be flipped
     */
    fun flipBlock(block: Block){
        val oldShape = block.shape.map { it.clone() }.toTypedArray()
        for (posY in 0..4) {
            for (posX in 0..4) {
                block.shape[posY][posX] = oldShape[posY][4 - posX]
            }
        }

        onAllRefreshables { refreshAfterBlockManipulation(block) }
    }



    /**
     * Checks if a given [block] can be placed at the position specified by [coordY] and [coordX].
     *
     * @param block the block in question if it can be placed at the position
     * @param coordY the vertical placement of the upper left tile of the block
     * @param coordX the horizontal placement of the upper left tile of the block
     *
     * @return true if the block can be placed at the given position. Otherwise return false
     */
    fun placeBlockValid(block: Block, coordY: Int, coordX: Int): Boolean {
        val boardSize = rootService.currentGameState!!.boardSize
        val board = rootService.currentGameState!!.gameBoard
        val playerColor = block.color

        var hasCorner = false

        for (posY in 0 until 5) {
            for (posX in 0 until 5) {
                if (block.shape[posY][posX]) {
                    val targetY = coordY + posY
                    val targetX = coordX + posX
                    // check if the tile is inside the game board und if the game board is free at this tile
                    if(targetY !in 0 until boardSize || targetX !in 0 until boardSize)
                        return false
                    // check if the game board is free at this tile
                    if(board[targetY][targetX] != PlayerColor.BLANK)
                        return false
                    // check if current tile is parallel adjacent to a tile of the same color
                    if (playerColor in getAllParallelAdjacentTileColors(board, targetY, targetX))
                        return false
                    // check if current tile is cornering the same color
                    if (playerColor in getAllDiagonalAdjacentTileColors(board, targetY, targetX)
                        || isStartCorner(targetY, targetX, playerColor)) hasCorner = true
                }
            }
        }

        return hasCorner
    }

    /**
     * checks if the proposed location is a starting corner
     *
     * @param coordY y-value of the location
     * @param coordX x-value of the location
     * @return true if the player can use a starting corner and if the location is a corner of the game board
     */
    fun isStartCorner(coordY: Int, coordX: Int, playerColor: PlayerColor): Boolean {
        if (rootService.currentGameState!!.players.first { it.color == playerColor }.blocks.size < 21) return false

        val boardSize = rootService.currentGameState!!.boardSize
        return coordY == 0 && coordX == 0
                || coordY == 0 && coordX == boardSize - 1
                || coordY == boardSize - 1 && coordX == 0
                || coordY == boardSize - 1 && coordX == boardSize - 1
    }

    /**
     * returns all the colors that are diagonal adjacent to a location.
     * If a diagonal adjacent tile does not exist (if the given location is at the edge of the game board), then no
     * color will be returned for that tile
     *
     * @param gameBoard the game board
     * @param posY y-value of the tile
     * @param posX x-value of the tile
     * @return list of PlayerColor objects
     */
    fun getAllDiagonalAdjacentTileColors(gameBoard: Array<Array<PlayerColor>>, posY: Int, posX: Int): List<PlayerColor> {
        return getColorsOfCoordinates(gameBoard,
            listOf(
                Pair(posY - 1, posX - 1),
                Pair(posY + 1, posX - 1),
                Pair(posY - 1, posX + 1),
                Pair(posY + 1, posX + 1),
            )
        )
    }

    /**
     * returns all the colors that are parallel adjacent to a location.
     * If a parallel adjacent tile does not exist (if the given location is at the edge of the game board), then no
     * color will be returned for that tile
     *
     * @param gameBoard the game board
     * @param posY y-value of the tile
     * @param posX x-value of the tile
     * @return list of PlayerColor objects
     */
    private fun getAllParallelAdjacentTileColors(
        gameBoard: Array<Array<PlayerColor>>, posY: Int, posX: Int): List<PlayerColor> {
        return getColorsOfCoordinates(gameBoard,
            listOf(Pair(posY - 1, posX), Pair(posY, posX - 1), Pair(posY, posX + 1), Pair(posY + 1, posX),
            )
        )
    }

    /**
     * returns the colors of all the coordinates given.
     * If coordinates are out of bounds, then no color will be returned for these coordinates
     *
     * @param gameBoard the game board
     * @param coordinates a list of Int pairs, which represent the location of the tiles that are requested
     * @return list of PlayerColor objects
     */
    private fun getColorsOfCoordinates(gameBoard: Array<Array<PlayerColor>>,
                                       coordinates: List<Pair<Int, Int>>): List<PlayerColor> {
        val colors: MutableList<PlayerColor> = mutableListOf()
        coordinates.forEach { coordinate ->
            if (coordinate.first in gameBoard.indices && coordinate.second in gameBoard.indices) {
                colors.add(gameBoard[coordinate.first][coordinate.second])
            }
        }

        return colors
    }

    /**
     * Function to update the isPlayable attribute from all player at once.
     */
    fun updateIsPlayable(){
        //todo
        val gameBoard = rootService.currentGameState!!.gameBoard

        rootService.currentGameState!!.players.forEach{ player ->
            val allRemainingBlockOrientations = rootService.aIService.allBlockOrientations.map {
                it.copy() }.toMutableList()

            allRemainingBlockOrientations.removeAll {
                    remainingBlockOrientation -> remainingBlockOrientation.blockID !in player.blocks.map { it.blockID }
            }

            allRemainingBlockOrientations.forEach { it.color = player.color }
            player.isPlayable = false
            for (posY in -2 until gameBoard.size) {
                if(player.isPlayable) break
                for (posX in -2 until gameBoard.size) {
                    if(player.isPlayable) break
                    for(i in 0 until allRemainingBlockOrientations.size){
                        if (placeBlockValid(allRemainingBlockOrientations[i], posY, posX)){
                            player.isPlayable = true
                            break
                        }
                    }
                }
            }
        }

    }

    /**
     * Sets the current game state to the previous one, undoing ones actions
     */
    fun undo(){
        if (rootService.currentGameState != null) {
            rootService.currentGameState = rootService.currentGameState!!.previous
        }
        rootService.aIService.syncAfterUndoRedo()
        onAllRefreshables { refreshAfterUndoRedo(rootService.currentGameState!!) }
    }


    /**
     * Sets the current game state to the next one, if one exists
     */
    fun redo(){
        if (rootService.currentGameState!!.next != null) {
            rootService.currentGameState = rootService.currentGameState!!.next
        }
        rootService.aIService.syncAfterUndoRedo()
        onAllRefreshables { refreshAfterUndoRedo(rootService.currentGameState!!) }
    }
}