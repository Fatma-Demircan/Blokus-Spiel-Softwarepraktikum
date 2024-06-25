package view

import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * Network scene used to join a hosts session
 */
class JoinNetworkGameMenuScene(private val rootService: RootService, private val blokusApplication: BlokusApplication)
    : MenuScene(1920, 1080), Refreshable {

    companion object {
        const val NETWORK_SECRET = "blokus23a"
    }
    private val headline = Label(
        posX = 0, posY= 20, width = 1920, height = 130, text = "Join Game",
        font = Font(120, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)
    private val headline1 = Label(
        posX = 200, posY= 200, width = 600, height = 100, text = "Your Name",
        font = Font(40, Color(147, 107, 81), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)
    private val input1 = TextField(
        posX = 200, posY= 300, width = 600, height = 100, prompt = "Enter your name", font= Font(size=30))
    private val headline2 = Label(
        posX = 1120, posY= 200, width = 600, height = 100, text = "Toggle Player/AI",
        font = Font(40, Color(147, 107, 81), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)
    private val input2 = ComboBox(posX = 1120, posY= 300, width = 600, height = 100, prompt = "Player",
        font= Font(size=30),items = listOf(
        HUMAN,
        RANDOM_AI,
        HARD_AI
    ))
    private val box = Label(
        posX = 250, posY= 500, width = 1420, height = 300, text = "  ",
        font= Font(size=30, color = Color(195, 207, 203)))
    private val headline4 = Label(
        posX = 650, posY= 500, width = 600, height = 100, text = "Session ID",
        font = Font(40, Color(163, 144, 98), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)
    private val input4 = TextField(posX = 650, posY= 600, width = 600, height = 100, prompt = "Enter ID",
        font= Font(size=30))
    private val joinButton = Button(posX = 1500, posY= 900, width = 300, height = 100,text = "Join",
        font= Font(family = "monospace", size=40, fontWeight = Font.FontWeight.BOLD,
            color = Color(148, 98, 76)), visual = ColorVisual.LIGHT_GRAY).apply {
        onMouseClicked={
            rootService.networkService.joinGame(NETWORK_SECRET, input1.text, input4.text)
            rootService.networkService.client!!.playerType = getPlayerType()
        }
    }
    private val mainButton = Button(
        posX = 80, posY= 900, width = 300, height = 100,text = "Main Menu",
        font= Font(family = "monospace",size=40, fontWeight = Font.FontWeight.BOLD,
            color = Color(148, 98, 76)), visual = ColorVisual.LIGHT_GRAY).apply { onMouseClicked = {
        blokusApplication.showMainMenuScene()
    } }

    /**
     * converts the player type chosen in the combo box to its according enum
     *
     * @return the type
     */
    private fun getPlayerType() = when (input2.selectedItem) {
        HUMAN -> PlayerType.HUMAN
        HARD_AI -> PlayerType.HARD_AI
        else -> PlayerType.RANDOM_AI
    }

    init {
     addComponents(headline, headline1, input1, headline2, input2,box, headline4, input4, joinButton, mainButton)
        background = ImageVisual("board2.jpg")
    }


}