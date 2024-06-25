package view

import entity.*
import service.ConnectionState

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 *
 * @see service.AbstractRefreshingService
 *
 */
interface Refreshable {
    /**
     * refreshes the necessary scenes after a game is started.
     *
     * @param [simulationSpeed] Simulation speed in ms.
     */
    fun refreshAfterStartGame(simulationSpeed: Int = 500){}

    /**
     * refreshes the necessary scenes after a game ended.
     *
     * @param [players] Non dummy players, including their final scores.
     */
    fun refreshAfterGameEnd(players : List<Player>){}

    /**
     * refreshes the game scene(s) after a block was rotated or flipped.
     *
     * @param [block] Manipulated block.
     */
    fun refreshAfterBlockManipulation(block: Block){}

    /**
     * refreshes the game scene(s) after a block was placed.
     *
     * @param [gameBoard] New game Board.
     * @param [removedBlock] Placed Block / Removed block from Inventory
     */
    fun refreshAfterBlockPlaced(gameBoard : Array<Array<PlayerColor>>, removedBlock: Block, coordY: Int, coordX: Int){}

    /**
     * refreshes after a block has been set by a network player
     *
     * @param pieceCoords coordinates of piece to be set
     */
    fun refreshAfterNetworkBlockPlaced(pieceCoords: List<Pair<Int, Int>>, block : BlockID){}

    /**
     * refreshes the game scene(s) after the player changed.
     *
     * @param [newCurrentPlayer] New current player.
     */
    fun refreshAfterNextPlayer(newCurrentPlayer: Player){}

    /**
     * refreshes the game scene(s) after a gameState was undone or redone.
     *
     * @param [newGameState] New game state.
     */
    fun refreshAfterUndoRedo(newGameState: BlokusGameState){}

    /**
     * refreshes the game scene(s) after joining a network game lobby.
     *
     */
    fun refreshAfterJoinedNetworkGame(){}

    /**
     * refreshes the game scene(s) after a player joined the network game lobby.
     *
     */
    fun refreshAfterPlayerJoined(name: String){}

    /**
     * refreshes the network connection status with the given information
     *
     * @param state the information to show
     */
    fun refreshConnectionState(state: ConnectionState) {}
    
}