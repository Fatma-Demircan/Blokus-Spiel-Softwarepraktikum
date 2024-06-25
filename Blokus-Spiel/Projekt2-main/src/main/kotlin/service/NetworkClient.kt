package service


import service.messages.BlokusGameInitMessage
import service.messages.BlokusSetPieceMessage
import entity.PlayerType
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.*

/**
 * [BoardGameClient] implementation for network communication.
 *
 * @param networkService the [NetworkService] to potentially forward received messages to.
 * @param playerName name of the player this client uses
 * @param host name of the host
 * @param secret secret needed for connection
 */
class NetworkClient(playerName: String,
                    host: String,
                    secret: String,
                    private val networkService: NetworkService) : BoardGameClient(playerName,
                                                                    host, secret, NetworkLogging.VERBOSE)  {

    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null

    /** list of other players taking part */
    var otherPlayerNames: MutableList<String> = mutableListOf()

    var playerType = PlayerType.HUMAN

    /**
     * Handle a [CreateGameResponse] sent by the server. Will await the guest player when its
     * status is [CreateGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @param response received response from the server
     * @throws IllegalStateException if status != success or currently not waiting for a game creation response.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
            { "unexpected CreateGameResponse" }

            when (response.status) {
                CreateGameResponseStatus.SUCCESS -> {
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
                    sessionID = response.sessionID
                }
                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * Disconnects in case something goes wrong
     *
     * @throws IllegalStateException
     */
    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        error(message)
    }

    /**
     * Handle a [JoinGameResponse] sent by the server. Will await the init message when its
     * status is [JoinGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @param response received response from the server
     * @throws IllegalStateException if status != success or currently not waiting for a join game response.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
            { "unexpected JoinGameResponse" }

            when (response.status) {
                JoinGameResponseStatus.SUCCESS -> {
                    otherPlayerNames = response.opponents.toMutableList()
                    sessionID = response.sessionID
                    networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
                }
                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * Handle a [PlayerJoinedNotification] sent by the server. Adds the joining player to the list of other players
     *
     * @throws IllegalStateException if not currently expecting any guests to join.
     * @param notification Notification received
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_GUEST )
            { "not awaiting any guests."}

            otherPlayerNames.add(notification.sender)
            networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
            networkService.onAllRefreshables { refreshAfterPlayerJoined(notification.sender) }
        }
    }

    /**
     * Handle a [GameActionResponse] sent by the server. Does nothing when its
     * status is [GameActionResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @throws IllegalStateException if the response's status is not successful
     * @param response response received
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
                    networkService.connectionState == ConnectionState.PLAYING_WAITING_FOR_OPPONENT)
            { "not currently playing in a network game."}

            when (response.status) {
                GameActionResponseStatus.SUCCESS -> {} // do nothing in this case
                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * handle a [BlokusGameInitMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message : BlokusGameInitMessage, sender : String){
        BoardGameApplication.runOnGUIThread {
            networkService.startNewJoinedGame(message, playerName, playerType)
        }
    }

    /**
     * handle a [BlokusSetPieceMessage] sent by the server
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onPieceReceived(message: BlokusSetPieceMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receivePiece(message)
        }
    }
}