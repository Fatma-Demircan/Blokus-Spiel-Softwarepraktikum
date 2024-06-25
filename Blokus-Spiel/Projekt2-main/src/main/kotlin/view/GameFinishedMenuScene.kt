package view

import entity.Player
import entity.PlayerColor
import entity.PlayerType
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import kotlin.system.exitProcess

/**
 * Game finished scene, which is displayed after the game ended. From here you can start a new game or leave the application.
 */
class GameFinishedMenuScene(blokusApplication: BlokusApplication) : MenuScene(1920, 1080), Refreshable  {
    //headline
    private val headline = Label(
        posX = 0, posY= 20, width = 1920, height = 130, text = "Game Ended",
        font = Font(120, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)

    //1st place
    private val place1Label = Label(
        width = 100, height = 100,
        posX = 500, posY = 250,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val player1Label = Label(
        width = 620, height = 100,
        posX = 600, posY = 250,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val points1Label = Label(
        width = 200, height = 100,
        posX = 1220, posY = 250,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )

    //2nd place
    private val place2Label = Label(
        width = 100, height = 100,
        posX = 500, posY = 400,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val player2Label = Label(
        width = 620, height = 100,
        posX = 600, posY = 400,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val points2Label = Label(
        width = 200, height = 100,
        posX = 1220, posY = 400,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )

    //3rd place
    private val place3Label = Label(
        width = 100, height = 100,
        posX = 500, posY = 550,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val player3Label = Label(
        width = 620, height = 100,
        posX = 600, posY = 550,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val points3Label = Label(
        width = 200, height = 100,
        posX = 1220, posY = 550,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )

    //4th place
    private val place4Label = Label(
        width = 100, height = 100,
        posX = 500, posY = 700,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val player4Label = Label(
        width = 620, height = 100,
        posX = 600, posY = 700,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val points4Label = Label(
        width = 200, height = 100,
        posX = 1220, posY = 700,
        font = Font(40, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY
    )

    //Button to leve the game
    private val quitButton = Button(
        width = 435, height = 100,
        posX = 985, posY = 850,
        text = "Exit",
        visual = ColorVisual(Color(133, 52, 54)),
        font= Font(family = "monospace", size=35, fontWeight = Font.FontWeight.BOLD, color = Color(163, 144, 98))
    ).apply { onMouseClicked = {
        exitProcess(0)
    } }


    //Button to start a new game with previous Names already entered
    private val newGameButton = Button(
        width = 435, height = 100,
        posX = 500, posY = 850,
        text = "Main Menu",
        visual = ColorVisual(Color(163, 144, 98)),
        font= Font(family = "monospace", size=35, fontWeight = Font.FontWeight.BOLD, color = Color(133, 52, 54))
    ).apply { onMouseClicked = {
        blokusApplication.showMainMenuScene()
    } }

    //Initialize scene
    init {
        background = ImageVisual(path = "board2.jpg")
        addComponents(
            headline,
            place1Label,
            player1Label,
            points1Label,
            place2Label,
            player2Label,
            points2Label,
            place3Label,
            player3Label,
            points3Label,
            place4Label,
            player4Label,
            points4Label,
            newGameButton,
            quitButton
        )
        place3Label.isVisible = false
        place4Label.isVisible = false
        player3Label.isVisible = false
        player4Label.isVisible = false
        points3Label.isVisible = false
        points4Label.isVisible = false
    }

    /**
     * Fills the corresponding lables with position, player name and corresponding points
     *
     * @param players list of unsorted players
     */
    override fun refreshAfterGameEnd(players : List<Player>) {
        val sortedPlayers = sortPlayers(players)

        //1st place
        place1Label.text = "1"
        player1Label.text = sortedPlayers[0].name
        points1Label.text = sortedPlayers[0].score.toString()

        place1Label.font = Font(40, toColor(sortedPlayers[0]), "monospace", fontWeight = Font.FontWeight.BOLD)
        player1Label.font = Font(40, toColor(sortedPlayers[0]), "monospace", fontWeight = Font.FontWeight.BOLD)
        points1Label.font = Font(40, toColor(sortedPlayers[0]), "monospace", fontWeight = Font.FontWeight.BOLD)

        //2nd place
        place2Label.text = if (sortedPlayers[1].score == sortedPlayers[0].score) "1" else "2"
        player2Label.text = sortedPlayers[1].name
        points2Label.text = sortedPlayers[1].score.toString()

        place2Label.font = Font(40, toColor(sortedPlayers[1]), "monospace", fontWeight = Font.FontWeight.BOLD)
        player2Label.font = Font(40, toColor(sortedPlayers[1]), "monospace", fontWeight = Font.FontWeight.BOLD)
        points2Label.font = Font(40, toColor(sortedPlayers[1]), "monospace", fontWeight = Font.FontWeight.BOLD)

        //3rd place
        computeThirdPlace(sortedPlayers)

        //4th place
        computeFourthPlace(sortedPlayers)
    }

    private fun toColor(player : Player): Color{
        return when (player.color) {
            PlayerColor.GREEN -> Color(84,130,53)
            PlayerColor.BLUE -> Color(31,78,120)
            PlayerColor.YELLOW -> Color(191 ,143,0)
            PlayerColor.RED -> Color(198,89,17)
            else -> Color.WHITE
        }
    }

    private fun sortPlayers(players: List<Player>): MutableList<Player>{
        val sortedPlayers = players.toMutableList()
        sortedPlayers.removeAll { it.type == PlayerType.DUMMY }
        if(sortedPlayers.size == 4){
            sortedPlayers.sortBy { it.name }
            if(sortedPlayers[0].name == sortedPlayers[1].name && sortedPlayers[2].name == sortedPlayers[3].name
                && sortedPlayers[0].type == sortedPlayers[1].type && sortedPlayers[2].type == sortedPlayers[3].type){
                sortedPlayers[0].score +=sortedPlayers[1].score
                sortedPlayers[2].score +=sortedPlayers[3].score
                sortedPlayers.removeAt(3)
                sortedPlayers.removeAt(1)
            }
        }
        sortedPlayers.sortByDescending { it.score }
        return sortedPlayers
    }

    private fun computeThirdPlace(sortedPlayers: MutableList<Player>){
        val size = sortedPlayers.size
        if(size >= 3){
            place3Label.isVisible = true; player3Label.isVisible = true; points3Label.isVisible = true
            place3Label.text = when (sortedPlayers[2].score) {
                sortedPlayers[0].score -> "1"
                sortedPlayers[1].score -> "2"
                else -> "3"
            }
            player3Label.text = sortedPlayers[2].name
            points3Label.text = sortedPlayers[2].score.toString()

            place3Label.font = Font(40, toColor(sortedPlayers[2]), "monospace", fontWeight = Font.FontWeight.BOLD)
            player3Label.font = Font(40, toColor(sortedPlayers[2]), "monospace", fontWeight = Font.FontWeight.BOLD)
            points3Label.font = Font(40, toColor(sortedPlayers[2]), "monospace", fontWeight = Font.FontWeight.BOLD)
        }
        else{
            quitButton.posY = 550.0
            newGameButton.posY = 550.0
            if (place3Label in components) removeComponents(place3Label)
            if (player3Label in components) removeComponents(player3Label)
            if (points3Label in components) removeComponents(points3Label)
        }
    }

    private fun computeFourthPlace(sortedPlayers: MutableList<Player>){
        val size = sortedPlayers.size
        if(size == 4){
            place4Label.isVisible = true; player4Label.isVisible = true; points4Label.isVisible = true
            place4Label.text = when (sortedPlayers[3].score) {
                sortedPlayers[0].score -> "1"
                sortedPlayers[1].score -> "2"
                sortedPlayers[2].score -> "3"
                else -> "4"
            }
            player4Label.text = sortedPlayers[3].name
            points4Label.text = sortedPlayers[3].score.toString()

            place4Label.font = Font(40, toColor(sortedPlayers[3]), "monospace", fontWeight = Font.FontWeight.BOLD)
            player4Label.font = Font(40, toColor(sortedPlayers[3]), "monospace", fontWeight = Font.FontWeight.BOLD)
            points4Label.font = Font(40, toColor(sortedPlayers[3]), "monospace", fontWeight = Font.FontWeight.BOLD)
        }
        else{
            quitButton.posY = 700.0
            newGameButton.posY = 700.0
            if (place4Label in components) removeComponents(place4Label)
            if (player4Label in components) removeComponents(player4Label)
            if (points4Label in components) removeComponents(points4Label)
        }
    }
}