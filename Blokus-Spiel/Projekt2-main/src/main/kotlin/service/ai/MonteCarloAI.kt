package service.ai

import entity.BlockID
import entity.BlokusGameState
import entity.PlayerColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import service.PlayerService
import service.RootService

/**
 * MonteCarlo Managing class
 */
class MonteCarloAI(rootService: RootService, private val color: PlayerColor,
                   private val opponentColor: PlayerColor): AIType {

    private val aiService: AIService = rootService.aIService
    private val playerService: PlayerService = rootService.playerService

    override fun getMove(currentGameState: BlokusGameState): Move {
        val corners = listOf(Pair(0, 0), Pair(0, 13), Pair(13, 13), Pair(13, 0))
        val turnCount = currentGameState.currentPlayer.blocks.size
        var hardCodedMove = aiService.emptyMove
        if (turnCount >= 19) {
            var choosenCornerIndex = 0
            for (cornerIndex in corners.indices) {
                if (currentGameState.gameBoard[corners[cornerIndex].first][corners[cornerIndex].second] == color) {
                    choosenCornerIndex = cornerIndex
                }
            }
            when (turnCount) {
                21 -> {
                    for (cornerIndex in corners.indices) {
                        if (currentGameState.gameBoard[corners[cornerIndex].first][corners[cornerIndex].second]
                            != PlayerColor.BLANK) {
                            choosenCornerIndex = (cornerIndex + 3) % 4
                            break
                        }
                    }
                    val block = currentGameState.currentPlayer.blocks.first { it.blockID == BlockID.FIVE_TEN }
                    when (choosenCornerIndex) {
                        0 -> {
                            playerService.flipBlock(block)
                            playerService.rotateBlock(block)
                            playerService.rotateBlock(block)
                        }
                        1 -> {
                            playerService.rotateBlock(block)
                            playerService.flipBlock(block)
                        }
                        2 -> {
                            playerService.flipBlock(block)
                        }
                        3 -> {
                            playerService.flipBlock(block)
                            playerService.rotateBlock(block)
                        }
                    }
                    val posY = if (choosenCornerIndex in 0..1) -1 else 10
                    val posX = if (choosenCornerIndex in listOf(0, 3)) -1 else 10
                    hardCodedMove = Move(block, posY, posX)
                }
                20 -> {
                    val block = currentGameState.currentPlayer.blocks.first { it.blockID == BlockID.FIVE_ELEVEN }
                    val posY = if (choosenCornerIndex in 0..1) 1 else 8
                    val posX = if (choosenCornerIndex in listOf(0, 3)) 1 else 8
                    hardCodedMove = Move(block, posY, posX)
                }
                19 -> {
                    val block = currentGameState.currentPlayer.blocks.first { it.blockID == BlockID.FIVE_SEVEN }
                    when (choosenCornerIndex) {
                        0 -> {}
                        1 -> playerService.rotateBlock(block)
                        2 -> {
                            playerService.flipBlock(block)
                            playerService.rotateBlock(block)
                        }
                        3 -> repeat(3) {playerService.rotateBlock(block)}
                    }
                    val posY = if (choosenCornerIndex in 0..1) 3 else 6
                    val posX = if (choosenCornerIndex in listOf(0, 3)) 3 else 6
                    hardCodedMove = Move(block, posY, posX)
                }
            }
        }
        if (playerService.placeBlockValid(hardCodedMove.movedBlock, hardCodedMove.posY, hardCodedMove.posX))
            return hardCodedMove



        if(currentGameState.currentPlayer.blocks.size>21){
            val mMAI = AlphaBetaPruningAI(aiService.rootService, color,opponentColor)
            return mMAI.getMove(currentGameState)
        }
        println("MonteCarlo AI Move")
        val state = AIGameState(true, currentGameState.gameBoard, currentGameState.currentPlayer.color,
            currentGameState.currentPlayer.blocks.size == 21,
            21 - currentGameState.currentPlayer.blocks.size)


        val monteCarloR = MonteCarloRoot(aiService,1000)
        monteCarloR.initRoot(currentGameState.currentPlayer, state)
        runBlocking {
            val waiter1 =GlobalScope.async {
                monteCarloR.simulate(currentGameState.players,currentGameState.currentPlayer, state, 0)
                println("t1 Complete")
            }

            val waiter2 =GlobalScope.async {
                monteCarloR.simulate(currentGameState.players,currentGameState.currentPlayer, state, 0)
                println("t2 Complete")
            }
            val waiter3 =GlobalScope.async {
                monteCarloR.simulate(currentGameState.players,currentGameState.currentPlayer, state, 0)
                println("t3 Complete")
            }
            val waiter4 = GlobalScope.async {
                monteCarloR.simulate(currentGameState.players,currentGameState.currentPlayer, state, 0)
                println("t4 Complete")
            }


            //waiter1.await()
            //waiter2.await()
            //waiter3.await()
            //waiter4.await()

            delay(9500)
        }

        val bMove: Move = monteCarloR.bestMove()
        if(bMove == aiService.emptyMove){
            val mMAI = AlphaBetaPruningAI(aiService.rootService, color,opponentColor)
            return mMAI.getMove(currentGameState)
        }

        return(monteCarloR.bestMove())
    }

    override fun getPlayerColor(): PlayerColor {
        return color
    }

    override fun syncAfterUndoRedo() {
        TODO("Not yet implemented")
    }

}