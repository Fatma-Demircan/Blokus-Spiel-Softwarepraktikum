package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * Scene which is displayed while waiting for the host of a network game
 */
class WaitingForHostMenuScene(rootService: RootService) : MenuScene(1920, 1080), Refreshable  {

    private val mainButton = Button(
        posX = 80, posY = 900, width = 300, height = 100, text = "Disconnect",
        font = Font(
            family = "monospace", size = 40, fontWeight = Font.FontWeight.BOLD,
            color = Color(148, 98, 76)
        ), visual = ColorVisual.LIGHT_GRAY
    ).apply {
        onMouseClicked = {
            rootService.networkService.disconnect()
        }
    }

    private val waitLabel = Label(
        width = 920, height = 280, posX = 500, posY = 400,
        text = "Waiting for Host to start the game...",
        font = Font(size = 48, color = Color.WHITE)
    )

    init {
        addComponents(waitLabel, mainButton)
        background = ImageVisual("board2.jpg")
    }
}