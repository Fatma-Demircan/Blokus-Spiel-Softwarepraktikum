package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.dialog.FileDialog
import tools.aqua.bgw.dialog.FileDialogMode
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * Main menu scene where a player selects if he wants to
 * - play a local game
 * - load a game
 * - host a network game
 * - join a network game
 */
class MainMenuScene(private val rootService: RootService, private val blokusApplication: BlokusApplication)
    : MenuScene(1920, 1080, background= ColorVisual.WHITE), Refreshable{
    private val headline = Label(
        posX = 0, posY= 20, width = 1920, height = 130, text = "Main Menu",
        font = Font(120, Color(133, 52, 54), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY)
    private val playButton = Button(
        posX = 500, posY= 300, width = 920, height = 100,text = "Play Local Game",
        font= Font(40, Color(147, 107, 81), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual = ColorVisual.LIGHT_GRAY).apply { onMouseClicked = {
        blokusApplication.showLocalGameMenuScene()
    } }
    private val loadButton = Button(
        posX = 500, posY= 450, width = 920, height = 100, text = "Load Game",
        font= Font(40, Color(163, 144, 98), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual= ColorVisual.LIGHT_GRAY).apply {
        onMouseClicked = {
            try{
                blokusApplication.showFileDialog(
                    FileDialog(
                        mode = FileDialogMode.OPEN_FILE,
                        title = "Save file",
                    )
                ).ifPresent{ rootService.gameService.loadGame("${it.first()}") }
            }
            catch (e : Exception){
                println("Could not load file!")
            }
        }
    }
    private val hostButton = Button(
        posX = 500, posY= 600, width = 920, height = 100, text = "Host Online Game",
        font= Font(40, Color(163, 144, 98), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual= ColorVisual.LIGHT_GRAY).apply { onMouseClicked = {
        blokusApplication.showHostOnlineGameMenuScene()
    } }
    private val joinButton = Button(
        posX = 500, posY= 750, width = 920, height = 100, text = "Join Online Game",
        font=  Font(40, Color(163, 144, 98), "monospace", fontWeight = Font.FontWeight.BOLD),
        visual= ColorVisual.LIGHT_GRAY).apply { onMouseClicked = {
            blokusApplication.showJoinNetworkGameMenuScene()
    } }


    init {
        addComponents(headline, playButton, loadButton, hostButton, joinButton)
        background = ImageVisual("board2.jpg")
    }
}