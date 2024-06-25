package entity
import java.io.Serializable
/**
 * Entity class that represents a game state of "Blokus". It contains the player list ([players]), the [currentPlayer],
 * the game Board and the [previous] and possibly [next] game state. Comparable to a doubly linked list.
 *
 * @param [players] A list of the participating [players], including "dummy players".
 * @param [currentPlayer] The player who has to perform the next move.
 * @param [boardSize] The height and width of the [gameBoard]. Default = 20
 * @param [gameBoard] A 20 x 20 or 14 x 14 matrix of enum colors, which represents the game board, where fields,
 * which contain the "default color", are empty. Default: All fields are occupied with the "default color".
 * @param [previous] The previous game state. Default = null (for first game state).
 * @param [currentDummyController] Player who should perform the dummys next move.
 */

data class BlokusGameState (
    val players : List<Player>,
    var currentPlayer: Player,
    val boardSize : Int = 20,
    var gameBoard: Array<Array<PlayerColor>> = Array(boardSize){ Array(boardSize){ PlayerColor.BLANK } },
    var previous : BlokusGameState? = null,
    var next : BlokusGameState? = null,
    var currentDummyController: Player? = null,
    var currentDummyControllerIndex: Int = -1,
    ) : Serializable{

        companion object{
            private const val serialVersionUID: Long = 123
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
        boardString += "Points BLUE: ${countPoints(PlayerColor.BLUE)}\n"
        boardString += "Points GREEN: ${countPoints(PlayerColor.GREEN)}\n"
        boardString += "Points YELLOW: ${countPoints(PlayerColor.YELLOW)}\n"
        return boardString
    }

    /**
     * counts how many tiles a specified player has placed on the game board.
     *
     * @param playerColor the color after which will be searched on the board
     * @return the count of tiles from that player
     */
    private fun countPoints(playerColor: PlayerColor): Int {
        var count = 0
        gameBoard.forEach { row ->
            row.forEach {
                if (it == playerColor) count += 1
            } }
        return count
    }

    /**
     * returns a deep copy of the BlokusGameState object
     *
     * @return a new BlokusGameState object
     */
    fun copy(): BlokusGameState {
        val currentPlayerIndex = players.indexOf(currentPlayer)
        val newPlayers = players.map { it.copy() }.toList()
        val dummyControllerIndex = if(currentDummyController != null)  players.indexOf(currentDummyController) else -1
        val dummyControllerCopy = if(currentDummyController != null) newPlayers[dummyControllerIndex] else null
        return BlokusGameState(
            newPlayers,
            newPlayers[currentPlayerIndex],
            boardSize,
            gameBoard.map { row -> row.map { it }.toTypedArray() }.toTypedArray(),
            previous,
            next,
            dummyControllerCopy,
            this.currentDummyControllerIndex
        )
    }
}