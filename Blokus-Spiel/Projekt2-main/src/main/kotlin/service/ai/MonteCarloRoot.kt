package service.ai

import entity.Player
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Root element of the MonteCarloTree. manages which Moves to run simulations for and tries to find
 * best move for the current Position
 */
class MonteCarloRoot(override var aiService: AIService, private var simCount:Int) : MonteCarloTreeElement(aiService) {
    private var children: MutableList<MonteCarloNode>? = null

    /**
     * Initializes Children
     * @param currentPlayer current player
     * @param gameState current AIgameState
     */
    fun initRoot(currentPlayer: Player, gameState: AIGameState){
        initializeChildren(gameState.allPosMoves(currentPlayer.blocks))
    }

    /**
     * Simulates children 5*simCount times and chooses which child to simulate
     */
    override fun simulate(players: List<Player>, currentPlayer: Player, gameState: AIGameState, noTurnCount: Int) {



        if(children!!.size==0){return}

        var bestNode: MonteCarloNode = children!![children!!.size-1]

        for(i in 1 .. simCount){
            var sumSims = 0
            for(i in children!!.indices){
                sumSims += children!![i].simulations
            }
            for(i in children!!.indices){
                if((children!![children!!.size-1-i].wins/children!![children!!.size-1-i].simulations)+
                    sqrt(2.0) * sqrt(ln(sumSims.toDouble())/children!![children!!.size-1-i].simulations) >
                    (bestNode.wins/bestNode.simulations) +
                    sqrt(2.0) * sqrt(ln(sumSims.toDouble())/bestNode.simulations)){
                    bestNode = children!![children!!.size-1-i]
                }
            }

            val newPlayersCopy: List<Player> = players.map{it.copy()}
            children!![children!!.indexOf(bestNode)].simulate(
                newPlayersCopy,
                currentPlayer=newPlayersCopy[(players.indexOf(currentPlayer)+1).mod(newPlayersCopy.size)],
                gameState.copy(),
                noTurnCount)
        }
    }

    override fun backpropagation(winner: Player, players: List<Player>, currentPlayer: Player) {}


    /**
     * returns best found move from the children
     */
    fun bestMove():Move{

        if(children!!.size == 0){return aiService.emptyMove}

        var bestNode: MonteCarloNode = children!![0]
        for(i in children!!.indices){
            if(children!![i].wins.toDouble()/children!![i].simulations.toDouble() >
                bestNode.wins.toDouble()/bestNode.simulations.toDouble()){
                bestNode = children!![i]
            }
        }

        return bestNode.move
    }


    private fun initializeChildren(moves:List<Move>){
        children = emptyList<MonteCarloNode>().toMutableList()
        moves.forEach{children!!.add(MonteCarloNode(parent = this, move = it, aiService = aiService))}
    }



}