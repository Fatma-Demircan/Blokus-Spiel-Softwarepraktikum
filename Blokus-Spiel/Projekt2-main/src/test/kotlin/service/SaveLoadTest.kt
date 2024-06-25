package service
import entity.*
import kotlin.test.*

/**
 * Test class for the Save and Load Functions
 */
class SaveLoadTest {
    private var rootService = RootService()

    @Test
    /**
    * Check if Saving and then Loading a Game State preserves Values
    **/
    fun testSaveLoadPlayer(){
        rootService.gameService.startGame(
            listOf("Robert", "Richard"),
            listOf(PlayerColor.GREEN, PlayerColor.RED),
            listOf(PlayerType.HUMAN, PlayerType.HUMAN),
            boardSize = 14
        )
        rootService.gameService.saveGame("save")
        rootService.currentGameState!!.players[0].name = "Michael"
        assert(rootService.currentGameState!!.players[0].name == "Michael")
        rootService.gameService.loadGame("save")

        assert(rootService.currentGameState!!.players[0].name == "Robert")
    }




}