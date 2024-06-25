package service.ai

import entity.*
import service.AbstractRefreshingService
import service.RootService

/**
 * Entry point into the AI layer.
 */
class AIService(val rootService: RootService): AbstractRefreshingService() {

    val emptyMove: Move = Move(Block(BlockID.ONE_ONE), -100, -100)
    val aiValueList: List<PlayerType> = listOf(
        PlayerType.RANDOM_AI,
        PlayerType.HARD_AI,
        PlayerType.MINI_MAX_AI,
        PlayerType.ALPHA_BETA_AI,
        PlayerType.MONTE_CARLO_AI
    )
    val allBlockOrientations: MutableList<Block> = compAllBlockOrientations()

    val aiList: MutableList<AIType> = mutableListOf()

    /**
     * creates an AI object for every AI player. This is necessary because the AI objects are storing which blocks
     * they still have left in every orientation.
     *
     * @param aiType the type of AI to be created
     * @param playerColor the color of the player the AI is playing
     */
    fun createAI(aiType: PlayerType, playerColor: PlayerColor) {
        require(aiType in aiValueList)
        if (rootService.currentGameState!!.players.size == 2 || aiType == PlayerType.RANDOM_AI ) {
            when (aiType) {
                PlayerType.RANDOM_AI -> aiList.add(MonteCarloAI(rootService, playerColor,
                    if (playerColor == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED))
                PlayerType.HARD_AI -> aiList.add(AlphaBetaPruningAI(rootService, playerColor,
                            if (playerColor == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED))
                PlayerType.MINI_MAX_AI -> aiList.add(MiniMaxAI(rootService, playerColor,
                    if (playerColor == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED))
                PlayerType.ALPHA_BETA_AI -> aiList.add(AlphaBetaPruningAI(rootService, playerColor,
                    if (playerColor == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED))
                PlayerType.MONTE_CARLO_AI -> aiList.add(MonteCarloAI(rootService, playerColor,
                    if (playerColor == PlayerColor.RED) PlayerColor.BLUE else PlayerColor.RED))
                else -> {}
            }
        } else {
            aiList.add(NotDuoAI(rootService, playerColor))
        }

    }

    /**
     * called when an AI is at turn. This method retrieves the responsible AI object and gets the move from it.
     * Then the move is executed.
     *
     * @return a Boolean value, true if the AI can make a move, false if not
     */
    fun makeMove(): Boolean {
        checkNotNull(rootService.currentGameState)
        val currentPlayerColor = rootService.currentGameState!!.currentPlayer.color
        check(currentPlayerColor in aiList.map { it.getPlayerColor() })
        val choosenMove: Move = aiList.first { it.getPlayerColor() == currentPlayerColor }
            .getMove(rootService.currentGameState!!)

        if (choosenMove != emptyMove) {
            check(rootService.playerService.placeBlock(choosenMove.movedBlock, choosenMove.posY, choosenMove.posX))
            return true
        }
        println("Illegal move or no move possible!")
        return false
    }


    /**
     * only called when initializing the AIService object, because of performance reasons. The result of the operations
     * will be stored in [AIService.allBlockOrientations] which then can be used by other classes if they copy
     * the elements from it.
     */
    private fun compAllBlockOrientations(): MutableList<Block> {
        val allBlockAlignments: MutableList<Block> = mutableListOf()
        // only one orientation
        listOf(BlockID.ONE_ONE, BlockID.FOUR_TWO, BlockID.FIVE_ELEVEN).forEach { allBlockAlignments.add(Block(it)) }
        // rotated two ways
        listOf(BlockID.TWO_ONE, BlockID.THREE_TWO, BlockID.FOUR_FIVE, BlockID.FIVE_FOUR).forEach {
            val block = Block(it)
            allBlockAlignments.add(block.copy())
            rootService.playerService.rotateBlock(block)
            allBlockAlignments.add(block.copy())
        }
        // rotated by 2 ways and flipped by two ways
        listOf(BlockID.FIVE_SIX).forEach {
            val block = Block(it)
            allBlockAlignments.add(block.copy())
            rootService.playerService.rotateBlock(block)
            allBlockAlignments.add(block.copy())
            rootService.playerService.rotateBlock(block)
            rootService.playerService.flipBlock(block)
            allBlockAlignments.add(block.copy())
            rootService.playerService.rotateBlock(block)
            allBlockAlignments.add(block.copy())
        }
        // rotated by 4 ways
        listOf(BlockID.THREE_ONE, BlockID.FOUR_THREE, BlockID.FIVE_NINE, BlockID.FIVE_FIVE, BlockID.FIVE_EIGHT,
            BlockID.FIVE_SEVEN).forEach {
            val block = Block(it)
            repeat(4) {
                allBlockAlignments.add(block.copy())
                rootService.playerService.rotateBlock(block)
            }
        }
        // can be orientated in 8 ways
        listOf(BlockID.FOUR_ONE, BlockID.FOUR_FOUR, BlockID.FIVE_TEN, BlockID.FIVE_THREE, BlockID.FIVE_TWO,
            BlockID.FIVE_ONE, BlockID.FIVE_TWELVE).forEach {
            val block = Block(it)
            repeat(2) {
                repeat(4) {
                    allBlockAlignments.add(block.copy())
                    rootService.playerService.rotateBlock(block)
                }
                rootService.playerService.flipBlock(block)
            }
        }

        return allBlockAlignments
    }

    /**
     * places a block and returns a new AIGameState
     *
     * @param move the choosen move of the AI
     * @param lastGameState the last game state
     * @return a new [AIGameState] object that can be used for further calculations
     */
    fun executeAICompMove(move: Move, lastGameState: AIGameState): AIGameState {
        val newState = lastGameState.copy()
        for (posY in 0..4) {
            for (posX in 0..4) {
                if (move.movedBlock.shape[posY][posX]) {
                    newState.gameBoard[move.posY + posY][move.posX + posX] = move.movedBlock.color
                }
            }
        }
        newState.ownTurn = !newState.ownTurn
        newState.currentPlayerColor =
            if (newState.currentPlayerColor == PlayerColor.BLUE) PlayerColor.BLUE else PlayerColor.RED
        newState.turn += 1

        return newState
    }

    /**
     * counts on how many fields a player (specified by [playerColor]) is able to place blocks.
     *
     * @param gameBoard a 2d boolean array representing the game board
     * @param playerColor the color which is searched for
     * @return the amount of corners
     */
    fun countCorners(gameBoard: Array<Array<PlayerColor>>, playerColor: PlayerColor): Int {
        var countCorners = 0
        for (posY in gameBoard.indices) {
            for (posX in gameBoard.indices) {
                if (playerColor in rootService.playerService.getAllDiagonalAdjacentTileColors(gameBoard, posY, posX))
                    countCorners += 1
            }
        }

        return countCorners
    }

    /**
     * should be called after a user performed a undo or redo operation. Then this method calls the responsible
     * method of each AI
     */
    fun syncAfterUndoRedo() {
        aiList.forEach { it.syncAfterUndoRedo() }
    }

}