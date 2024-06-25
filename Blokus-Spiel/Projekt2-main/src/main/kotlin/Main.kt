import service.RootService
import view.BlokusApplication

/**
 * Main method, which starts the game.
 */
fun main() {
    BlokusApplication(RootService()).show()
    println("Application ended. Goodbye")
}