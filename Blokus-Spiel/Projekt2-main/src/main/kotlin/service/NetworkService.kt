package service

import service.messages.BlokusGameInitMessage
import service.messages.BlokusSetPieceMessage
import entity.BlockID
import entity.PlayerColor
import entity.PlayerType

/**
 * Service layer class that provides logic for sending and receiving messages in network games.
 */
class NetworkService(private val rootService: RootService): AbstractRefreshingService() {

    companion object {
        /** URL of the BGW net server*/
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Blokus"
    }

    /** Network client. Nullable for offline games */
    var client : NetworkClient? = null

    /** Current state of the connection in a network game */
    var connectionState = ConnectionState.DISCONNECTED


    /**
     * Connects to server and creates a new game session.
     *
     * @param secret secret needed to join
     * @param name name of the player
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(secret: String, name: String, sessionID: String?) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        if (sessionID.isNullOrBlank()) {
            client?.createGame(GAME_ID, "Gruppe06" ,"Welcome!")
        } else {
            client?.createGame(GAME_ID, sessionID, "Welcome!")
        }
        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * Connects to the server and joins a game session.
     *
     * @param secret secret needed to join
     * @param name name of the player
     * @param sessionID identifier of the session to be entered
     *
     * @throws IllegalStateException if the client is already connected to another game
     */
    fun joinGame(secret: String, name: String, sessionID: String) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID, "Hello!")

        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)

        onAllRefreshables {
            refreshAfterJoinedNetworkGame()
        }
    }

    /**
     * Sets up the game and sends the game init message.
     *
     * @param playerNames names of all players taking part
     * @param playerColors colors of all players taking part
     * @param playerTypes types of all players taking part
     * @param boardSize size of the board
     *
     * @throws IllegalStateException if the state doesn't allow for a game to be started
     */
    fun startNewHostedGame(playerNames: List<String>,
                           playerColors: List<PlayerColor>,
                           playerTypes: List<PlayerType>,
                           boardSize : Int) {

        check(connectionState == ConnectionState.WAITING_FOR_GUEST)
        { "not able to start a hosted game" }

        rootService.gameService.startGame(playerNames, playerColors, playerTypes, boardSize)

        //ConnectionState festlegen, je nachdem ob man startet oder nicht
        if (rootService.currentGameState?.currentPlayer?.name == client?.playerName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.PLAYING_WAITING_FOR_OPPONENT)
        }

        val twoPlayerVariant : Boolean = (boardSize == 14)
        val playerData = emptyList<Pair<String, PlayerColor>>().toMutableList()
        for (i in playerNames.indices){
            playerData.add(Pair(playerNames[i], playerColors[i]))
        }

        val message = BlokusGameInitMessage(twoPlayerVariant, playerData.toList())
        client?.sendGameActionMessage(message)
    }

    /**
     * Sets up the game by processing a [BlokusGameInitMessage]
     *
     * @param message the received message
     * @param localPlayer used to identify the player that belongs to this client
     * @param localPlayerType type of this client's player (HUMAN or AI)
     *
     * @throws IllegalStateException if no game init message is expected
     */
    fun startNewJoinedGame(message: BlokusGameInitMessage, localPlayer : String, localPlayerType : PlayerType){
        check(connectionState == ConnectionState.WAITING_FOR_INIT)
        { "not expecting a game init"}

        var boardSize = 20
        if (message.twoPlayerVariant) boardSize = 14

        val playerNames = emptyList<String>().toMutableList()
        val playerColors = emptyList<PlayerColor>().toMutableList()
        val playerTypes = emptyList<PlayerType>().toMutableList()

        for (player in message.turnOrder) {
            playerNames.add(player.first)
            playerColors.add(player.second)
            if (player.first == localPlayer) {
                playerTypes.add(localPlayerType)
            } else
                playerTypes.add(PlayerType.NET_PLAYER)
        }

        rootService.gameService.startGame(playerNames, playerColors, playerTypes, boardSize)

        //ConnectionState festlegen, je nachdem ob man startet oder nicht
        if (rootService.currentGameState?.currentPlayer?.name == client?.playerName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.PLAYING_WAITING_FOR_OPPONENT)
        }
    }


    /**
     * Processes a [BlokusSetPieceMessage]. Removes the piece contained in the message from the players hand
     * and places it on the board accordingly
     *
     * @throws IllegalStateException if a piece is not expected
     */
    fun receivePiece(message: BlokusSetPieceMessage){
        val blockToRemove = message.piece
        if (blockToRemove == BlockID.PASSED){
            return
        }

        check(connectionState == ConnectionState.PLAYING_WAITING_FOR_OPPONENT)
        { "not expecting an opponent's turn"}

        val coords = message.pieceCoords

        val nextGameState = rootService.currentGameState!!.copy()
        nextGameState.previous = rootService.currentGameState
        nextGameState.next = null
        rootService.currentGameState!!.next = nextGameState
        rootService.currentGameState = nextGameState
        val currentPlayer = rootService.currentGameState!!.currentPlayer

        currentPlayer.blocks.removeAll { it.blockID == blockToRemove }

        //Inserts colors into the board using given coordinates
        for (coord in coords){
            rootService.currentGameState!!.gameBoard[coord.second][coord.first] = currentPlayer.color
        }

        rootService.playerService.updateIsPlayable()
        rootService.gameService.updateScore(currentPlayer)

        onAllRefreshables { refreshAfterNetworkBlockPlaced(coords, blockToRemove) }
    }

    /**
     * Sends a [BlokusSetPieceMessage] to all other clients in the current network game.
     *
     *
     * @param pieceCoords board coordinates where the piece was set
     * @param piece ID of the set piece
     *
     * @throws IllegalStateException if it is not this clients turn or if there is no current game state
     */
    fun sendPiece(pieceCoords: List<Pair<Int, Int>>, piece : BlockID){
        check(connectionState == ConnectionState.PLAYING_MY_TURN)
        { "not this clients turn"}

        val gameState = rootService.currentGameState
        checkNotNull(gameState)

        val message = BlokusSetPieceMessage(pieceCoords, gameState.gameBoard, gameState.currentPlayer.color, piece)
        client?.sendGameActionMessage(message)
    }

    /**
     * Updates the [connectionState] to [newState] and notifies all refreshables
     *
     * @param newState state to be updated to
     */
    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState)
        }
    }

    /**
     * Disconnects the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active.
     */
    fun disconnect() {
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }


    /**
     * Connects to server, sets the [NetworkService.client] if successful and returns `true` on success.
     *
     * @param secret Network secret. Must not be blank (i.e. empty or only whitespaces)
     * @param name Player name. Must not be blank
     *
     * @throws IllegalArgumentException if secret or name is blank
     * @throws IllegalStateException if already connected to another game
     */
    private fun connect(secret: String, name: String): Boolean {
        require(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(secret.isNotBlank()) { "Server secret must be given" }
        require(name.isNotBlank()) { "Player name must be given" }

        val newClient =
            NetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = secret,
                networkService = this
            )

        return if (newClient.connect()) {
            this.client = newClient
            true
        } else {
            false
        }
    }

}