package service
import entity.PlayerColor
import entity.PlayerType
import kotlin.test.*

/**
 * Test class for functionalities related to [NetworkService]
 */
class NetworkServiceTest {

    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService

    companion object {
        const val NETWORK_SECRET = "blokus23a"
    }

    /**
     * Initialization of two connections to test network communication
     */
    private fun initClients(sessionID :String) {
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        rootServiceHost.networkService.hostGame(NETWORK_SECRET, "Host", sessionID)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)

        val session = rootServiceHost.networkService.client?.sessionID
        assertNotNull(session)

        rootServiceGuest.networkService.joinGame(NETWORK_SECRET, "Guest", sessionID)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)
    }

    /**
     * tests if the session has been entered by both host and guest
     */
    @Test
    fun testHostAndJoin(){
        initClients("Test01")

        assertEquals(rootServiceHost.networkService.connectionState, ConnectionState.WAITING_FOR_GUEST)
        assertEquals(rootServiceGuest.networkService.connectionState, ConnectionState.WAITING_FOR_INIT)
    }

    /**
     * tests the starting functionality for guests and hosts
     */
    @Test
    fun testStartNewGame(){
        initClients("Test02")
        val playerNames = listOf("Host", "Guest")
        val playerColors = listOf(PlayerColor.BLUE, PlayerColor.RED)
        val playerTypes = listOf(PlayerType.HUMAN, PlayerType.NET_PLAYER)

        rootServiceHost.networkService.startNewHostedGame(playerNames, playerColors, playerTypes, 14)
        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.PLAYING_WAITING_FOR_OPPONENT)

        val hostState = rootServiceHost.currentGameState
        val guestState = rootServiceGuest.currentGameState
        checkNotNull(hostState)
        checkNotNull(guestState)

        assertEquals(hostState.currentPlayer.name, guestState.currentPlayer.name)
        assertEquals(hostState.players[1].name, guestState.players[1].name)

        //Tests if player types are set correctly, since they aren't part of the init message
        assertEquals(guestState.currentPlayer.type, PlayerType.NET_PLAYER)
        assertEquals(hostState.currentPlayer.type, PlayerType.HUMAN)

        assertEquals(hostState.currentPlayer.color, guestState.currentPlayer.color)
        assertEquals(hostState.players[1].color, guestState.players[1].color)
    }

    /**
     * tests sending pieces when placing them and receiving pieces
     */
    @Test
    fun testSendingAndReceiving(){
        initClients("Test03")
        val playerNames = listOf("Host", "Guest")
        val playerColors = listOf(PlayerColor.BLUE, PlayerColor.RED)
        val playerTypes = listOf(PlayerType.HUMAN, PlayerType.NET_PLAYER)

        rootServiceHost.networkService.startNewHostedGame(playerNames, playerColors, playerTypes, 14)
        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.PLAYING_WAITING_FOR_OPPONENT)

        val block = rootServiceHost.currentGameState!!.currentPlayer.blocks[0]

        rootServiceHost.playerService.placeBlock(block, -2, -2)

        //Check if block has been placed
        assertEquals(rootServiceHost.currentGameState!!.gameBoard[0][0], PlayerColor.BLUE)
        waitForTime()
        assertEquals(rootServiceGuest.currentGameState!!.gameBoard[0][0], PlayerColor.BLUE)

        //Check if block has been removed
        assertTrue(!rootServiceHost.currentGameState!!.currentPlayer.blocks.contains(block))
        assertTrue(!rootServiceGuest.currentGameState!!.currentPlayer.blocks.contains(block))
    }

    /**
     * tests disconnecting from the server
     */
    @Test
    fun testDisconnect(){
        initClients("Test04")
        rootServiceGuest.networkService.disconnect()

        assertEquals(rootServiceGuest.networkService.connectionState, ConnectionState.DISCONNECTED)
        assertEquals(rootServiceGuest.networkService.client, null)
    }

    /**
     * waiting for the game represented by this [RootService] to reach the desired network state.
     *
     * @param state the desired state
     * @param timeout maximum milliseconds to wait
     *
     * @throws IllegalStateException if desired state is not reached
     */
    private fun RootService.waitForState(state: ConnectionState, timeout: Int = 10000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (networkService.connectionState == state)
                return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after waiting $timeout ms")
    }

    /**
     * waiting for a specified amount of time, needed when waiting for a network state is not possible
     *
     * @param time time spent waiting in milliseconds
     */
    private fun waitForTime(time: Int = 5000) {
        var timePassed = 0
        while (timePassed < time) {
            Thread.sleep(100)
            timePassed += 100
        }
    }
}