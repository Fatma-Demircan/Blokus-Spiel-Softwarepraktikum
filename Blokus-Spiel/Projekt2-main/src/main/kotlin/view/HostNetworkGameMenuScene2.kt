package view

import entity.PlayerColor
import entity.PlayerType
import service.ConnectionState
import service.RootService
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * Network scene where the host waits for players, picks their colors and can call for a game start
 */
class HostNetworkGameMenuScene2(private val rootService: RootService, private val blokusApplication: BlokusApplication)
    : MenuScene(1920, 1080), Refreshable {

    private var playersAmount = -1
    private var names: MutableList<String> = mutableListOf()
    private val types = mutableListOf(HUMAN, HUMAN, HUMAN, HUMAN)
    var boardSize = 0
    private var listColor = listOf<ColorVisual>()
    private var playerColors = mutableListOf<ColorVisual>()
    private var listOfPlayerColor = mutableListOf<PlayerColor>()
    private var listOfTypes = mutableListOf<PlayerType>()

    private val headline = Label(posX = 0, posY= 20, width = 1920, height = 160,
        text = "Online Game", font= Font( family = "monospace", size=110,
            fontWeight = Font.FontWeight.BOLD,
            color = Color(133, 52, 54)
        ),
        visual = ColorVisual.LIGHT_GRAY)
    private val headline1 = Label(posX = 0, posY= 190, width = 1920, height = 100,
        text = "Waiting for players...",
        Font(size=40,fontWeight= Font.FontWeight.BOLD, family = "monospace",
            color = Color(141, 75, 65)
        ),
        visual = ColorVisual(Color(162, 159, 150))
    )
    private val mainButton = Button(posX = 80, posY= 900, width = 300, height = 100,
        text = "Main Menu", font= Font(family = "monospace",size=35,
            fontWeight = Font.FontWeight.BOLD, color = Color(188, 173, 138)
        ),
        visual = ColorVisual(Color(141, 75, 65))
    ).apply { onMouseClicked = {
        blokusApplication.showMainMenuScene()
    } }
    private val button14 = Button(posX = 1540, posY= 780, width = 300, height = 100,
        text = "14×14",font= Font(family = "monospace", size=35,
            fontWeight = Font.FontWeight.BOLD, color = Color(133, 52, 54)
        ),
        visual = ColorVisual(Color(156, 121, 87))
    ).apply {
        onMousePressed={
            boardSize = 14
            createListColors(playerColors)
            rootService.networkService.startNewHostedGame(
                playerNames = names.subList(0, playersAmount),
                playerColors = listOfPlayerColor.subList(0, playersAmount),
                playerTypes = createListPlayerTypes(),
                boardSize= boardSize)
        }
    }
    private val normalButton = Button(posX = 1540, posY= 900, width = 300, height = 100,
        text = "Normal Game",font= Font(family = "monospace", size=35,
            fontWeight = Font.FontWeight.BOLD,
            color = Color(133, 52, 54)
        ),
        visual = ColorVisual(Color(163, 144, 98))
    ).apply {
        onMousePressed={
            boardSize = 20
            createListColors(playerColors)
            rootService.networkService.startNewHostedGame(
                playerNames = names.subList(0, playersAmount),
                playerColors = listOfPlayerColor.subList(0, playersAmount),
                playerTypes = createListPlayerTypes(),
                boardSize= boardSize)
        }
    }

    private fun createListColors(playerColors: MutableList<ColorVisual>){
        listOfPlayerColor= mutableListOf()
        for(i in 0 until playerColors.size){
            if(playerColors[i] == ColorVisual.YELLOW){
                listOfPlayerColor.add(PlayerColor.YELLOW)
            }else if(playerColors[i] == ColorVisual.GREEN){
                listOfPlayerColor.add(PlayerColor.GREEN)
            }else if(playerColors[i] == ColorVisual.RED){
                listOfPlayerColor.add(PlayerColor.RED)
            }else{
                listOfPlayerColor.add(PlayerColor.BLUE)
            }
        }
    }

    /**
     * Creates a list of all player types needed to start
     *
     * @return list of [PlayerType]
     */
    private fun createListPlayerTypes(): MutableList<PlayerType>{
        listOfTypes= mutableListOf()
        if(types[0]== HUMAN){
            listOfTypes.add(PlayerType.HUMAN)
        }else if(types[0] == HARD_AI){
            listOfTypes.add(PlayerType.HARD_AI)
        }else{
            listOfTypes.add(PlayerType.RANDOM_AI)
        }
        repeat(playersAmount - 1) { listOfTypes.add(PlayerType.NET_PLAYER)}
        return listOfTypes
    }


    private val mainGrid = GridPane<StaticComponentView<UIComponent>>(
        columns = 3, rows = 5, posX = this.width*0.5, posY = this.height*0.5, spacing=30,
        visual = ColorVisual(Color(188, 173, 138))
    ).apply {
        this.setCenterMode(Alignment.CENTER)
        this[0,0] = Label(
            text = "Name",
            font = Font(35, Color(141, 75, 65), "monospace", fontWeight = Font.FontWeight.BOLD)
        )
        this[1,0]= Label(
            text = "Type",
            font = Font(35, Color(141, 75, 65), "monospace", fontWeight = Font.FontWeight.BOLD)
        )
    }

    private val playersAmountGrid = GridPane<StaticComponentView<UIComponent>>(
        columns = 3, rows=1, posX = this.width*0.5, posY = this.height*0.75,
        visual = ColorVisual(Color(188, 173, 138))
    )

    private val playersAmountLabel = Label(
        text = "$playersAmount", width = 100, height = 70, visual = ColorVisual(Color(135, 145, 162)),
        font = Font(size = 30, fontWeight = Font.FontWeight.BOLD)
    )


    private fun refreshAfterChangeAmountPlayer(){
        playersAmountLabel.text = "$playersAmount"
        names = mutableListOf(rootService.networkService.client!!.playerName)
        rootService.networkService.client!!.otherPlayerNames.forEach { names.add(it) }
        for(i in 1..DEFAULT_PLAYERS_AMOUNT){
            mainGrid[0,i] = null
            mainGrid[1,i] = null
            mainGrid[2,i] = null
        }
        listColor = when(playersAmount) {
            2 -> listOf(ColorVisual.BLUE, ColorVisual.RED)
            3 -> listOf(ColorVisual.BLUE, ColorVisual.YELLOW, ColorVisual.RED)
            else -> listOf(ColorVisual.BLUE, ColorVisual.YELLOW, ColorVisual.RED, ColorVisual.GREEN)
        }
        playerColors = listColor.toMutableList()
        var a = 0
        for(i in 1..playersAmount){
            mainGrid[0,i] = TextField(
                prompt = names[i - 1], width = 500, height = 70,
                font = Font(size = 24, color = Color(156, 121, 87))
            ).apply {
                onKeyTyped = {names[i-1] = this.text}
            }
            mainGrid[1,i] = if (i == 1) {
                ComboBox(
                    width = 200, height = 70, font = Font(size = 20), items = listOf(HUMAN, HARD_AI, RANDOM_AI)).apply {
                    selectedItemProperty.addListener{_, type -> types[0] = type!!}
                }
            } else { Label(width = 200, height = 70, font = Font(size = 20), text = "Network Player") }

            mainGrid[2,i] = Button(
                posX = 1350,
                posY= 383,
                width = 60,
                height = 60,
                text = "",
                font = Font(
                    family = "monospace", size=35,
                    fontWeight = Font.FontWeight.BOLD,
                    color = Color(133, 52, 54)
                ),
                visual = listColor[i-1]
            ).apply{
                onMouseClicked={
                    a++
                    if(a==playersAmount)
                        a = 0
                    this.visual = listColor[a]
                }
            }
            mainGrid[0,i]?.isDisabled = true
        }
    }

    override fun refreshAfterPlayerJoined(name: String) {
        if (playersAmount < 4) {
            playersAmount += 1
            names.add(name)
        } else {
            println("Error: lobby is already full")
        }
        refreshAfterChangeAmountPlayer()
    }

    override fun refreshConnectionState(state: ConnectionState) {
        if (state == ConnectionState.WAITING_FOR_GUEST)
            refreshAfterChangeAmountPlayer()
    }

    init {
        playersAmount = 1

        addComponents(headline, headline1, mainButton, button14, normalButton)
        addComponents(mainGrid, playersAmountGrid)
        if(playersAmount > 2) {
            button14.isVisible = false
        }
        playersAmountGrid[1,0] = playersAmountLabel
        background = ImageVisual("board2.jpg")
    }

}