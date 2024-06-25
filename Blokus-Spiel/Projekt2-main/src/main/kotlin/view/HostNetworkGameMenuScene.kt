package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * Menu scene for hosting a network game
 */
class HostNetworkGameMenuScene(private val rootService: RootService, private val blokusApplication: BlokusApplication) :
    MenuScene(1920, 1080), Refreshable {

    companion object {
        const val NETWORK_SECRET = "blokus23a"
    }

    private val headline = Label(
        posX = 0, posY = 20, width = 1920, height = 170, text = "Online Game",
        font = Font(family = "monospace", size = 120, fontWeight = Font.FontWeight.BOLD, color = Color(133, 52, 54)),
        visual = ColorVisual.LIGHT_GRAY
    )
    private val headline1 = Label(
        posX = 0,
        posY = 110,
        width = 1920,
        height = 100,
        text = "Send the session ID to your friends to play with them",
        Font(size = 30, fontWeight = Font.FontWeight.BOLD, family = "monospace", color = Color(141, 75, 65))
    )
   /** val box = Label(
        posX = 610, posY = 230, width = 700, height = 150, text = "  ",
        font = Font(size = 30), visual = ColorVisual(Color(188, 173, 138))
    )
    val headline2 = Label(
        posX = 500, posY = 230, width = 920, height = 100, text = "Session ID:",
        font = Font(family = "monospace", size = 40, fontWeight = Font.FontWeight.BOLD, color = Color(141, 75, 65))
    )
    val id = Label(posX = 500, posY = 280, width = 920, height = 100, text = "ID", font = Font(size = 30))
    val player1 = Label(
        posX = 0, posY = 400, width = 1900, height = 70, text = "Host",
        font = Font(40, Color(141, 75, 65), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(Color(162, 159, 150))
    )
    val input1 = TextField(
        posX = 500, posY = 480, width = 920, height = 70, prompt = "Enter your name", font = Font(size = 30)
    )
    val player2 = Label(
        posX = 0, posY = 560, width = 1900, height = 70, text = "Guest 1",
        font = Font(40, Color(141, 75, 65), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(Color(162, 159, 150))
    )
    val input2 = TextField(posX = 500, posY = 640, width = 920, height = 70, prompt = "name", font = Font(size = 30))
    val player3 = Label(
        posX = 0, posY = 720, width = 1900, height = 70, text = "Guest 2",
        font = Font(40, Color(141, 75, 65), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(Color(162, 159, 150))
    )
    val input3 = TextField(posX = 500, posY = 800, width = 920, height = 70, prompt = "name", font = Font(size = 30))
    val player4 = Label(
        posX = 0, posY = 880, width = 1900, height = 70, text = "Guest 3",
        font = Font(40, Color(141, 75, 65), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual(Color(162, 159, 150))
    )
    val input4 = TextField(posX = 500, posY = 960, width = 920, height = 70, prompt = "name", font = Font(size = 30))
*/
   private val mainButton = Button(
        posX = 80, posY = 900, width = 300, height = 100, text = "Main Menu",
        font = Font(
            family = "monospace", size = 40, fontWeight = Font.FontWeight.BOLD,
            color = Color(148, 98, 76)
        ), visual = ColorVisual.LIGHT_GRAY
    ).apply {
        onMouseClicked = {
            blokusApplication.showMainMenuScene()
        }
    }
    private val headline5 = Label(
        posX = 650, posY= 200, width = 600, height = 100, text = "Your Name",
        font = Font(40, Color(147, 107, 81), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)
    private val input5 = TextField(
        posX = 650, posY= 300, width = 600, height = 100, prompt = "Enter your name", font= Font(size=30))


    private val headlineSessionID = Label(
        posX = 650, posY= 500, width = 600, height = 100, text = "Session ID",
        font = Font(40, Color(163, 144, 98), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)
    private val inputID = TextField(posX = 650, posY= 600, width = 600, height = 100,
        prompt = "Enter ID", font= Font(size=30))

    private val hostButton = Button(
        posX = 800, posY= 800, width = 300, height = 100,text = "Host",
        font= Font(family = "monospace",size=40, fontWeight = Font.FontWeight.BOLD,
            color = Color(148, 98, 76)), visual = ColorVisual.LIGHT_GRAY).apply { onMouseClicked = {

            rootService.networkService.hostGame(secret = NETWORK_SECRET, name = input5.text, sessionID = inputID.text)
    } }



        init {
            addComponents(
                headline,
                headline1,
               /** box,
                headline2,
                id,
                player1,
                player2,
                player3,
                player4,
                input1,
                input2,
                input3,
                input4,
               */
                headline5,
                input5,
                headlineSessionID,
                inputID,
                hostButton, mainButton

            )
            background = ImageVisual("board2.jpg")
        }

    }
