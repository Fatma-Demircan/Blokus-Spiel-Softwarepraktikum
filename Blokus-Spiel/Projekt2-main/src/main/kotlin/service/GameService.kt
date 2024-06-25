package service

import service.messages.BlokusSetPieceMessage
import entity.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Service layer class that provides the logic for actions not directly
 * related to a single player.
 *
 * @param rootService Provides access to other service classes and the [RootService.currentGameState].
 */
class GameService(private val rootService: RootService): AbstractRefreshingService() {

    /**
     *  Sets up a local game
     *
     * Players can be controlled by easy AI, hard AI or human players and there are 4 game modes:
     * 1) 4 player game (20x20 board)
     * 2) 3 player game where one color is being controlled by all players using a fourth dummy player (20x20 board)
     * 3) 2 player game (20x20 board)
     * 4) 2 player game where each player controls two colors using two dummy players (14x14 board)
     */
    fun startGame(
        playerNames: List<String>,
        playerColors: List<PlayerColor>,
        playerTypes: List<PlayerType>,
        boardSize: Int = 20
    ) {
        require(playerNames.distinct().size == playerNames.size)
        require(boardSize == 20 || boardSize == 14)
        require(playerColors.size in 2..4)
        require(playerColors.distinct().size == playerColors.size) {"player colors have to be distinct"}

        if(boardSize == 20) {
            require(playerNames.size in 2..4 && playerTypes.size in 2..4)
            if(playerTypes.any{it == PlayerType.DUMMY}) {
                require(playerColors == listOf(PlayerColor.BLUE, PlayerColor.YELLOW, PlayerColor.RED, PlayerColor.GREEN))
            }
        }

        val turnOrder = listOf(PlayerColor.BLUE, PlayerColor.YELLOW, PlayerColor.RED, PlayerColor.GREEN)

        rootService.aIService.aiList.removeAll { true }

        if(boardSize == 14) {
            require(playerNames.size == 2) {"A 14x14 game can only be played with two players!"}
            val players = (0..1).map { Player(playerNames[it], playerTypes[it], playerColors[it]) }
            val startGameState = BlokusGameState(players, currentPlayer = players[0], boardSize = 14)
            rootService.currentGameState = startGameState
        } else if (playerNames.size == 4){
            val players = (0..3).map { Player(playerNames[it], playerTypes[it], playerColors[it]) }
            val startGameState = BlokusGameState(players, currentPlayer = players[0], boardSize = 20)
            rootService.currentGameState = startGameState
        } else if (playerNames.size == 3) {
            val players = (0..2).map { Player(playerNames[it], playerTypes[it], turnOrder[it]) }.toMutableList()
            players.add(Player("Dummy", PlayerType.DUMMY, turnOrder[3]))
            val startGameState = BlokusGameState(players, currentPlayer = players[0], boardSize = 20,
                currentDummyController = players[0])
            rootService.currentGameState = startGameState
            if (players.any { it.type in rootService.aIService.aiValueList }) {
                rootService.aIService.createAI(PlayerType.HARD_AI, turnOrder[3])
            }
        } else {
            val player1 = Player(playerNames[0], playerTypes[0], turnOrder[0])
            val player2 = Player(playerNames[1], playerTypes[1], turnOrder[1])
            // create two new players with the same name and type for this 20x20 2 player version
            val dummy1 = Player(playerNames[0], playerTypes[0], turnOrder[2])
            val dummy2 = Player(playerNames[1], playerTypes[1], turnOrder[3])
            val players = listOf(player1, player2, dummy1, dummy2)
            val startGameState = BlokusGameState(players, currentPlayer = player1, boardSize = 20)
            rootService.currentGameState = startGameState
        }

        // create the AI objects for the AI players
        for (player in rootService.currentGameState!!.players) {
            if (player.type in rootService.aIService.aiValueList)
                rootService.aIService.createAI(player.type, player.color)
        }

        onAllRefreshables { refreshAfterStartGame() }
    }

    /**
     * Load current game state from a local file
     * @param path of the file containing game data
     */
    fun loadGame(path: String){

        val file = FileInputStream(path)
        val inputStream = ObjectInputStream(file)

        rootService.currentGameState = inputStream.readObject() as BlokusGameState

        inputStream.close()


        onAllRefreshables { refreshAfterStartGame() }

    }
    /**
     * Save current game state to a local file
     * @param path name for the file
     */
    fun saveGame(path: String){

        val file = FileOutputStream(path)
        val outputStream = ObjectOutputStream(file)

        outputStream.writeObject(rootService.currentGameState)

        outputStream.close()
        file.close()

    }

    /**
     * Sets the next player. It also calls the AI if the current player is an AI and can end the game.
     */
    fun nextPlayer(){
        val game = rootService.currentGameState!!
        if(game.players.all{!it.isPlayable}) { onAllRefreshables { refreshAfterGameEnd(game.players) }; return }

        for(i in 1 .. game.players.size){
            if(game.players[(game.players.indexOf(game.currentPlayer) + i) % game.players.size].isPlayable){
                game.currentPlayer =  game.players[(game.players.indexOf(game.currentPlayer) + i) % game.players.size]
                break
            }else{

                //Sends a PASSED message if in a network game
                if (rootService.networkService.connectionState != ConnectionState.DISCONNECTED) {
                    val message = BlokusSetPieceMessage(emptyList(),
                        rootService.currentGameState!!.gameBoard,
                        game.players[(game.players.indexOf(game.currentPlayer) + i) % game.players.size].color,
                        BlockID.PASSED)
                    rootService.networkService.client?.sendGameActionMessage(message)
                }
            }
        }

        // dummy handling
        if(game.currentPlayer.type == PlayerType.DUMMY){
            val currentPlayerIndex = game.players.indexOf(game.currentDummyController)
            if (game.players[currentPlayerIndex].type in rootService.aIService.aiValueList) {
                rootService.aIService.makeMove()
            }
        }

        //Case for network games
        if(rootService.networkService.connectionState != ConnectionState.DISCONNECTED) {
            if (game.currentPlayer.type != PlayerType.NET_PLAYER) {
                rootService.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
            } else {
                rootService.networkService.updateConnectionState(ConnectionState.PLAYING_WAITING_FOR_OPPONENT)
            }
        }


        onAllRefreshables { refreshAfterNextPlayer(game.currentPlayer) }

        if (game.currentPlayer.type in rootService.aIService.aiValueList) {
            rootService.aIService.makeMove()
        }
    }

    /**
     * Update the score of a player using the advanced scoring system.
     *
     * @param player the player which score will be updated
     */
    fun updateScore(player: Player){
        player.score = if (player.blocks.isEmpty()) {
            15 + if (player.lastBlockEqualsOneOne) 5 else 0
        } else {
            player.blocks.foldRight(0) { block, acc -> acc - block.shape
                .foldRight(0) { row, count -> count + row.count { it } } }
        }
    }
}