package service.ai

import entity.Block
import entity.PlayerColor

/**
 * Game state class for AI computations. Makes calculations more efficient and uses some slightly different methods.
 */
class AIGameState (var ownTurn: Boolean,
                   val gameBoard: Array<Array<PlayerColor>>,
                   var currentPlayerColor: PlayerColor,
                   var firstTurn: Boolean,
                   var turn: Int) {

    /**
     * returns a deep copy of the AIGameState object
     * @return deep copy of AIGameState object
     */
    fun copy(): AIGameState {
        return AIGameState(ownTurn, gameBoard.map { row -> row.map { it }.toTypedArray() }.toTypedArray(),
            currentPlayerColor, firstTurn, turn)
    }

    /**
     * calculates how many tiles of the game board are filled with the given player color
     *
     * @param playerColor the color searched for
     * @return the count of appearances of the given color
     */
    fun countPoints(playerColor: PlayerColor): Int {
        var count = 0
        gameBoard.forEach { row ->
            row.forEach {
                if (it == playerColor) count += 1
            } }
        return count
    }

    /**
     * uses the information from the AIGameState to computate which moves are possible under the restriction of the
     * alloweded blocks
     *
     * @param blocks a list of orientated blocks, that the player has not used yet
     * @return a list of moves that are valid
     */
    fun allPosMoves(blocks: List<Block>): MutableList<Move> {
        val allPosMoves: MutableList<Move> = mutableListOf()
        for (posY in -2 until gameBoard.size - 2) {
            for (posX in -2 until gameBoard.size - 2) {
                blocks.forEach {
                    if (placeBlockValid(it, posX, posY))
                        allPosMoves.add(Move(it, posX, posY))
                }
            }
        }

        return allPosMoves
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
        val boardSize = gameBoard.size
        val board = gameBoard
        val playerColor = currentPlayerColor

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
                    if (gameBoard[targetY][targetX] != PlayerColor.BLANK)
                        return false
                    // check if current tile is parallel adjacent to a tile of the same color
                    if (playerColor in getAllParallelAdjacentTileColors(board, targetY, targetX))
                        return false
                    // check if current tile is cornering the same color
                    if (playerColor in getAllDiagonalAdjacentTileColors(board, targetY, targetX)
                        || isStartCorner(targetY, targetX)) hasCorner = true
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
    private fun isStartCorner(coordY: Int, coordX: Int): Boolean {
        if (!firstTurn) return false

        val boardSize = gameBoard.size
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
    private fun getAllDiagonalAdjacentTileColors(
        gameBoard: Array<Array<PlayerColor>>, posY: Int, posX: Int): List<PlayerColor> {
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
     * Rotates the given block by 90 degrees clockwise
     * @param block the block, which shape will be rotated
     */
    fun rotateBlock(block : Block): Block{
        val oldShape = block.shape.map { it.clone() }.toTypedArray()
        for (posY in 0..4) {
            for (posX in 0..4) {
                block.shape[posY][posX] = oldShape[4 - posX][posY]
            }
        }
        return block
    }

    /**
     * Mirrors the given block horizontally
     * @param block the block, which shape will be flipped
     */
    fun flipBlock(block: Block): Block{
        val oldShape = block.shape.map { it.clone() }.toTypedArray()
        for (posY in 0..4) {
            for (posX in 0..4) {
                block.shape[posY][posX] = oldShape[posY][4 - posX]
            }
        }
        return block
    }

    /**
     * retruns a string, that visualizes the game board.
     * It also incorporates how many points the blue and red players are having (most relevant colors for AI development)
     *
     * @return visual representation of game board
     */
    override fun toString(): String {
        var boardString = ""
        gameBoard.forEach { row ->
            row.forEach { boardString += when(it) {
                PlayerColor.BLANK -> "-"
                PlayerColor.RED -> "R"
                PlayerColor.BLUE -> "B"
                PlayerColor.GREEN -> "G"
                PlayerColor.YELLOW -> "Y"
            } }
            boardString += "\n"
        }
        boardString += "Points RED: ${countPoints(PlayerColor.RED)}\n"
        boardString += "Points BLUE: ${countPoints(PlayerColor.BLUE)}"
        return boardString
    }
}