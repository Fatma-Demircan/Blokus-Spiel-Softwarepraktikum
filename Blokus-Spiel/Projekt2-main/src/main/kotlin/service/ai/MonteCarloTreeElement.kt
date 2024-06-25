package service.ai

import entity.Block
import entity.Player
import kotlin.math.floor
import kotlin.math.pow
import kotlin.random.Random

/**
 * Overarching class for the MonteCarloTree Elements
 */
abstract class MonteCarloTreeElement(open var aiService: AIService) {

    /**
     * Simulate the State
     */
    abstract fun simulate(players: List<Player>,currentPlayer: Player, gameState:AIGameState, noTurnCount: Int)

    /**
     * Propagates the Back
     */
    abstract fun backpropagation(winner:Player, players: List<Player>, currentPlayer: Player)

    /**
     * function that tries to find an unbiased random move as quickly as possible, first move found is returned
     */
    @Suppress("UNREACHABLE_CODE")
    fun quickRandomGen(gameState: AIGameState, player: Player): Move{

        if(player.blocks.size==0){
            return aiService.emptyMove //emptyMove
        }

        //initialize random and fixed elements of the random move search
        var cBlockIndex: Int = (0 until player.blocks.size).random()
        val startBlockIndex: Int = cBlockIndex
        var cBlock: Block = player.blocks[cBlockIndex]
        var foundMove = false
        var rMove: Move? = null
        val fieldIndex: Int = (gameState.gameBoard.size+2).toDouble().pow(2).toInt()
        var cSquare: Int = (0..fieldIndex).random()
        var direction: Boolean = Random.nextBoolean()

        while(!foundMove){

            //try every square on field in order of direction
            for(i in 0 .. fieldIndex) {

                    //randomize orientation
                    repeat((0..1).random()) {
                        repeat((0..3).random()) {
                            cBlock = gameState.rotateBlock(cBlock)
                        }
                        cBlock = gameState.flipBlock(cBlock)
                    }

                    val cX: Int = cSquare.mod(gameState.gameBoard.size + 2) - 2
                    val cY: Int = floor((cSquare / (gameState.gameBoard.size + 2)).toDouble()).toInt() - 2
                    if (gameState.placeBlockValid(cBlock, cX, cY)) {
                        rMove = Move(cBlock, cX, cY)
                        foundMove = true
                        break
                    }


                cSquare = if(direction) {
                    (cSquare + 1).mod(fieldIndex + 1)
                } else {
                    (cSquare - 1).mod(fieldIndex + 1)
                }
                if(foundMove){break}
            }

            //randomize direction and select next Block and Square
            direction = Random.nextBoolean()
            cBlockIndex = (cBlockIndex+1).mod(player.blocks.size)
            cBlock = player.blocks[cBlockIndex]
            if(cBlockIndex == startBlockIndex){
                return aiService.emptyMove //emptyMove
            }
        }
        return rMove!!
    }

    private fun IntRange.random() = Random.nextInt((endInclusive + 1) - start) + start

    /**
     * Evaluates the Winner
     */
    fun evaluate(players: List<Player>): Player {
        players.forEach {player ->
            player.score = if (player.blocks.isEmpty()) {
                15 + if (player.lastBlockEqualsOneOne) 5 else 0
            } else {
                player.blocks.foldRight(0) { block, acc -> acc - block.shape.foldRight(0) {
                        row, count -> count + row.count { it } } }
            }
        }
        var hPlayer: Player = players[0]
        for(i in players.indices){
            if(players[i].score>hPlayer.score){
                hPlayer = players[i]
            }
        }
        return hPlayer
    }


}