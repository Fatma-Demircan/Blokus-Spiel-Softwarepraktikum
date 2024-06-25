package view

import service.ConnectionState
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/**
 * Implementation of the BGW [BoardGameApplication] for the board game "Blokus"
 */
class BlokusApplication(private val rootService : RootService): BoardGameApplication("Blokus"), Refreshable {


    private val mainMenuScene = MainMenuScene(rootService, this)
    private val localGameMenuScene = LocalGameMenuScene(rootService, this)
    private val hostNetworkGameMenuScene = HostNetworkGameMenuScene(rootService, this)
    private var hostNetworkGameMenuScene2 = HostNetworkGameMenuScene2(rootService, this)
    private val joinNetworkGameMenuScene = JoinNetworkGameMenuScene(rootService, this)
    private val gameFinishedMenuScene = GameFinishedMenuScene(this)
    private val gameScene = GameScene(rootService, this)
    private val smallGameScene = SmallGameScene(rootService, this)
    private val waitingForHostMenuScene = WaitingForHostMenuScene(rootService)

    init {
        // all scenes and the application itself need to
        // react to changes done in the service layer
        rootService.addRefreshables(
            this,
            mainMenuScene,
            localGameMenuScene,
            hostNetworkGameMenuScene,
            hostNetworkGameMenuScene2,
            joinNetworkGameMenuScene,
            gameFinishedMenuScene,
            gameScene,
            smallGameScene,
            waitingForHostMenuScene,
            )

        // This is just done so that the blurred background when showing
        // the new game menu has content and looks nicer

        //this.showGameScene(gameScene)
        this.showMenuScene(mainMenuScene)
    }

    override fun refreshAfterStartGame(simulationSpeed: Int) {
        hideMenuScene()
        if (rootService.currentGameState!!.boardSize == 20) {
            showGameScene(gameScene)
        } else {
            showGameScene(smallGameScene)
        }
    }

    override fun refreshAfterJoinedNetworkGame() {
        showMenuScene(waitingForHostMenuScene)
    }

    override fun refreshConnectionState(state: ConnectionState) {
        if (state == ConnectionState.WAITING_FOR_HOST_CONFIRMATION){
            showMenuScene(hostNetworkGameMenuScene2)
        } else if (state == ConnectionState.DISCONNECTED) {
            showMenuScene(mainMenuScene)
        }
    }

    /**
     * shows the menu for joining an online game
     */
    fun showJoinNetworkGameMenuScene() {
        showMenuScene(joinNetworkGameMenuScene)
    }

    /**
     * shows the menu for hosting an online game
     */
    fun showHostOnlineGameMenuScene() {
        showMenuScene(hostNetworkGameMenuScene)
    }

    /**
     * shows the menu for configuring a local game
     */
    fun showLocalGameMenuScene() {
        showMenuScene(localGameMenuScene)
    }

    /**
     * shows the Main Menu scene
     */
    fun showMainMenuScene() {
        showMenuScene(mainMenuScene)
    }

    /**
     * shows the End Game Menu scene
     */
    fun showGameEndMenuScene() {
        showMenuScene(gameFinishedMenuScene)
    }

}