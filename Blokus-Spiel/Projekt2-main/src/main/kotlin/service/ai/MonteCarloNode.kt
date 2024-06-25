package service.ai

import entity.BlockID
import entity.Player

/**
 * Nodes of the MonteCarlo Tree. each Node has an Associated Move and each node remembers, how many times it was
 * simulated and how many times its simulations have won
 */
class MonteCarloNode(override var aiService: AIService, val parent: MonteCarloTreeElement,
                     val move : Move) : MonteCarloTreeElement(aiService) {

    var wins: Int = 1
    var simulations: Int = 1

    private var children : MutableList<MonteCarloNode> = emptyList<MonteCarloNode>().toMutableList()

    /**
     * simulates a random child node. has a slight bias to choose child with the highest win ratio, since
     * those Nodes are the most promising
     */
    override fun simulate(players: List<Player>, currentPlayer: Player, gameState: AIGameState,
                          noTurnCount: Int) {
        gameState.currentPlayerColor = currentPlayer.color
        var newNoTurnCount: Int = noTurnCount
        var newGameState : AIGameState = gameState
        val playersCopy: List<Player> = players.map{it.copy()}
        // move execution
        if(move != aiService.emptyMove){
            newGameState = aiService.executeAICompMove(move,gameState)
            newGameState.firstTurn = false
            var blockRM: Int? = null
            for(i in players[(players.indexOf(currentPlayer)-1).mod(players.size)].blocks.indices){
                if(players[(players.indexOf(currentPlayer)-1).mod(players.size)].blocks[i].blockID ==
                    move.movedBlock.blockID){
                    blockRM = i
                }
            }

            playersCopy[(players.indexOf(currentPlayer)-1).mod(players.size)].blocks.removeAt(blockRM!!)
            newNoTurnCount = 0
            playersCopy[(players.indexOf(currentPlayer)-1).mod(players.size)].lastBlockEqualsOneOne =
                                                            move.movedBlock.blockID==BlockID.ONE_ONE
        }else{
            newNoTurnCount = noTurnCount + 1
        }

        if(noTurnCount >= players.size){
            val winner:Player = this.evaluate(players)
            parent.backpropagation(winner,players,players[(players.indexOf(currentPlayer)-1).mod(players.size)])
            return
        }

        if((1..10).random()<3){
            if(children.size>=1){
                var bestNode: MonteCarloNode = children[children.size-1]
                for(i in children.indices){
                    if(children[children.size-1-i].wins/ children[children.size-1-i].simulations >
                        bestNode.wins/bestNode.simulations){
                        bestNode = children[children.size-1-i]
                    }
                }
                children[children.indexOf(bestNode)].simulate(playersCopy,
                    currentPlayer=playersCopy[(players.indexOf(currentPlayer)+1).mod(players.size)],
                    newGameState.copy(), newNoTurnCount)
            }else{
                var simMove: Move = quickRandomGen(newGameState.copy(),currentPlayer)
                if(simMove == aiService.emptyMove){
                    val allMoves: List<Move> = gameState.allPosMoves(currentPlayer.blocks)
                    if(allMoves.isNotEmpty()){
                        simMove = allMoves[(allMoves.indices).random()]
                    }
                }
                children.add(MonteCarloNode(aiService,this,simMove))
                children[0].simulate(playersCopy,
                    currentPlayer=playersCopy[(players.indexOf(currentPlayer)+1).mod(players.size)],
                    newGameState.copy(), newNoTurnCount)
            }
        } else {
            var simMove: Move = quickRandomGen(newGameState.copy(),currentPlayer)
            if(simMove == aiService.emptyMove){
                val allMoves: List<Move> = gameState.allPosMoves(currentPlayer.blocks)
                if(allMoves.isNotEmpty()){
                    simMove = allMoves[(allMoves.indices).random()]
                }
            }
            var usedChild: Int? = null
            if(children.isNotEmpty()){
                for(i in children.indices){
                    if(children[i].move.movedBlock.shape == simMove.movedBlock.shape && children[i].move.posX ==
                        simMove.posX && children[i].move.posY == simMove.posY)
                        usedChild = i
                }
            }
            if (usedChild != null) {
                children[usedChild].simulate(playersCopy,
                    currentPlayer=playersCopy[(players.indexOf(currentPlayer)+1).mod(players.size)],
                    newGameState.copy(), newNoTurnCount)
            } else {
                children.add(MonteCarloNode(aiService,this,simMove))
                try {
                    children[children.lastIndex].simulate(playersCopy,
                        currentPlayer=playersCopy[(players.indexOf(currentPlayer)+1).mod(players.size)],
                        newGameState.copy(), newNoTurnCount)
                } catch (e: NullPointerException) {
                    println(e)
                }

            }


        }




    }

    /**
     * Backpropagation function to update self and parent with the result of a simulation
     */
    override fun backpropagation(winner: Player, players: List<Player>, currentPlayer: Player) {
        if(players[(players.indexOf(currentPlayer)-1).mod(players.size)].name==winner.name){
            wins++
            simulations++
        }else{
            simulations++
        }

        parent.backpropagation(winner,players,players[(players.indexOf(currentPlayer)-1).mod(players.size)])
    }


}