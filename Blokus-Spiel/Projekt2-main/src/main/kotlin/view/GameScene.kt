package view

import entity.*
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.dialog.FileDialog
import tools.aqua.bgw.dialog.FileDialogMode
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import java.util.*

/**
 * This is the main scene for the Blokus game. It shows the whole game state at once.
 *
 * @param rootService rootService, to call player and game service methods.
 */
class GameScene(private val rootService: RootService, private val blokusApplication: BlokusApplication)
    : BoardGameScene(1920, 1080), Refreshable {
    private val unplacedBlockLength = 21
    private val placedBlockLength = 42

    private val xOffset = listOf(18, 1397, 1397, 18)
    private val yOffset = listOf(18, 18, 557, 557)

    private var selectedBlockIndex = -1
    private var currentPlayerIndex = 0

    private var isPaused = false
    private var simulationSpeed = 1000
    private var executeNextPlayer = mutableListOf<Boolean>()

    private val blockImageVisuals = listOf(
        ImageVisual(path = "green_block.png"),
        ImageVisual(path = "blue_block.png"),
        ImageVisual(path = "yellow_block.png"),
        ImageVisual(path = "red_block.png"),
        ImageVisual(path = "grey_block.png")
    )



    private val gameBoardBackground = Label(
        posX = 538,
        posY = 105,
        width = 844,
        height = 844,
        visual = ColorVisual.DARK_GRAY
    )

    private val separationLines = listOf(
        Label(posX = 0, posY = 539, width = 1920, height = 2, visual = ColorVisual.DARK_GRAY),
        Label(posX = 538, posY = 0, width = 2, height = 1920, visual = ColorVisual.DARK_GRAY),
        Label(posX = 1380, posY = 0, width = 2, height = 1920, visual = ColorVisual.DARK_GRAY)
    )

    private val cornerBackground = listOf(
        Label(posX = 0, posY = 0, height = 540, width = 540),
        Label(posX = 1380, posY = 0, height = 540, width = 540),
        Label(posX = 1380, posY = 540, height = 540, width = 540),
        Label(posX = 0, posY = 540, height = 540, width = 540)
    )



    private val gameBoardGridPane = GridPane<Label>(
        posX = 540,
        posY = 107,
        columns = 20,
        rows = 20,
        layoutFromCenter = false,
        visual = ColorVisual.DARK_GRAY
    )

    private val gameBoardDropAcceptor = Label(
        posX = 540,
        posY = 107,
        width = 840,
        height = 840,
        visual = ColorVisual.TRANSPARENT
    ).apply {
        dropAcceptor = {
            val block = rootService.currentGameState!!.currentPlayer.blocks[selectedBlockIndex]
            val coordY = (it.draggedComponent.actualPosY.toInt() - 107 + 24).floorDiv(42)
            val coordX = (it.draggedComponent.actualPosX.toInt() - 540 + 24).floorDiv(42)
            rootService.playerService.placeBlockValid(block, coordY, coordX)
        }
        onDragDropped = {
            val block = rootService.currentGameState!!.currentPlayer.blocks[selectedBlockIndex]
            val yCoord = (it.draggedComponent.actualPosY.toInt() - 107 + 24).floorDiv(42)
            val xCoord = (it.draggedComponent.actualPosX.toInt() - 540 + 24).floorDiv(42)
            rootService.playerService.placeBlock(block, yCoord, xCoord)
        }
    }



    private val playerBlocks = List<MutableList<Area<TokenView>>>(4){ mutableListOf() }

    private val playerNameLabels = List(4) {
        Label(
            width = 280,
            height = 70,
            font = Font(
                size = 40,
                color = Color.WHITE,
                fontWeight = Font.FontWeight.SEMI_BOLD))
    }

    private val cornerGreyForeground = listOf(
        Label(posX = 0, posY = 0, height = 540, width = 540, visual = ColorVisual(0,0,0, 80)),
        Label(posX = 1380, posY = 0, height = 540, width = 540, visual = ColorVisual(0,0,0, 80)),
        Label(posX = 1380, posY = 540, height = 540, width = 540, visual = ColorVisual(0,0,0, 80)),
        Label(posX = 0, posY = 540, height = 540, width = 540, visual = ColorVisual(0,0,0, 80))
    )



    private val playerScores = listOf(
        Label(posX = 555, posY = 15, height = 75, width = 75, font = Font(size = 35, color = Color.WHITE)),
        Label(posX = 645, posY = 15, height = 75, width = 75, font = Font(size = 35, color = Color.WHITE)),
        Label(posX = 735, posY = 15, height = 75, width = 75, font = Font(size = 35, color = Color.WHITE)),
        Label(posX = 825, posY = 15, height = 75, width = 75, font = Font(size = 35, color = Color.WHITE))
    )

    private val currentPlayerLabel = Label(
        posX = 1002,
        posY = 964,
        height = 101,
        width = 363,
        font = Font(size = 40, color = Color.WHITE, fontWeight = Font.FontWeight.SEMI_BOLD)
    )



    private val undoButton = Button(
        posX = 915,
        posY = 15,
        height = 75,
        width = 75,
        visual = ImageVisual(path = "arrow.png"),
    ).apply {
        rotation = 180.0
        onMouseClicked = {rootService.playerService.undo()}
    }

    private val redoButton = Button(
        posX = 1005,
        posY = 15,
        height = 75,
        width = 75,
        visual = ImageVisual(path = "arrow.png"),
    ).apply { onMouseClicked = {rootService.playerService.redo()} }

    /**
     * Save Button: opens file explorer
     */
    private val saveButton = Button(
        posX = 1095,
        posY = 15,
        height = 75,
        width = 128,
        text = "Save",
        font = Font(size = 34, color = Color.WHITE),
        visual = ColorVisual(Color(145,230,242))
    ).apply {
        onMouseClicked = {
            val names =
                rootService.currentGameState!!.players.map { "_${it.name.lowercase(Locale.getDefault())}" }.toList()
            var nameString = ""
            names.forEach { nameString += it }
            blokusApplication.showFileDialog(
                FileDialog(
                    mode = FileDialogMode.SAVE_FILE,
                    initialFileName = "blokus_game$nameString",
                    title = "Save file",
                )
            ).ifPresent{ rootService.gameService.saveGame("${it.first()}") }
        }
    }

    private val exitButton = Button(
        posX = 1238,
        posY = 15,
        height = 75,
        width = 127,
        text = "Exit",
        font = Font(size = 34, color = Color.WHITE),
        visual = ColorVisual(Color(221,136,136))
    ).apply { onMouseClicked = {
        blokusApplication.showMainMenuScene()
        resetScene()
    } }

    private val rotateButton = Button(
        posX = 555,
        posY = 964,
        height = 101,
        width = 150,
        text = "Rotate",
        font = Font(size = 34, color = Color.WHITE),
        visual = ColorVisual(Color(136, 221, 136))
    ).apply { onMouseClicked = {
        rootService.playerService.rotateBlock(rootService.currentGameState!!.currentPlayer.blocks[selectedBlockIndex])
    } }

    private val flipButton = Button(
        posX = 720,
        posY = 964,
        height = 101,
        width = 150,
        text = "Flip",
        font = Font(size = 34, color = Color.WHITE),
        visual = ColorVisual(Color(136, 221, 136))
    ).apply { onMouseClicked = {
        rootService.playerService.flipBlock(rootService.currentGameState!!.currentPlayer.blocks[selectedBlockIndex])
    } }

    private val pauseButton = Button(
        posX = 885,
        posY = 964,
        height = 101,
        width = 101,
        visual = ImageVisual(path = "pause.png")
    ).apply { onMouseClicked = {
        if(isPaused){
            isPaused = false
            visual = ImageVisual (path = "pause.png")
            saveButton.isDisabled = true

            val game = rootService.currentGameState!!
            var currentPlayer = game.currentPlayer
            var nextPlayer = rootService.currentGameState!!.currentPlayer
            for(i in 1 .. game.players.size){
                if(game.players[(game.players.indexOf(game.currentPlayer) + i) % game.players.size].isPlayable){
                    nextPlayer = rootService.currentGameState!!.players[(currentPlayerIndex + i)
                            % rootService.currentGameState!!.players.size]
                    break
                }
            }

            if(currentPlayer.type == PlayerType.DUMMY) currentPlayer = game.currentDummyController!!
            if(nextPlayer.type == PlayerType.DUMMY) nextPlayer = game.currentDummyController!!

            if(currentPlayer.blocks.size < nextPlayer.blocks.size ||
                (game.players.indexOf(currentPlayer) == game.players.size -1 &&
                        currentPlayer.blocks.size == nextPlayer.blocks.size)){
                if(nextPlayer.type in rootService.aIService.aiValueList){
                    isDisabled = true
                    playAnimation(DelayAnimation(duration = 0).apply {
                        onFinished = {
                            rootService.gameService.nextPlayer()
                        }
                    })
                }
                else{
                    rootService.gameService.nextPlayer()
                }
            }
            else if(currentPlayer.type in rootService.aIService.aiValueList){
                isDisabled = true
                playAnimation(DelayAnimation(duration = 0).apply {
                    onFinished = {
                        rootService.aIService.makeMove()
                    }
                })
            }
            if(rootService.currentGameState!!.players.all { !it.isPlayable })
                refreshAfterGameEnd(rootService.currentGameState!!.players)
        }
        else{
            visual = ImageVisual (path = "play.png")
            isPaused = true
            saveButton.isDisabled = false
            executeNextPlayer.replaceAll { false }
        }
    }}


    /**
     * refreshes and initializes the scene, after a game is started
     */
    override fun refreshAfterStartGame(simulationSpeed: Int) {
        if (rootService.currentGameState!!.boardSize != 20) return
        val players = rootService.currentGameState!!.players
        val currentPlayer = rootService.currentGameState!!.currentPlayer
        val gameBoard = rootService.currentGameState!!.gameBoard

        //scene state
        currentPlayerIndex = players.indexOf(currentPlayer)
        selectedBlockIndex = -1
        this.simulationSpeed = simulationSpeed

        if(currentPlayer.type in rootService.aIService.aiValueList){
            isPaused = true
            pauseButton.visual = ImageVisual("play.png")
            saveButton.isDisabled = false
        }
        else{
            saveButton.isDisabled = true
        }

        //initialize background beauty
        addComponents(gameBoardBackground)
        background = ColorVisual.WHITE
        for (i  in players.indices){
            cornerBackground[i].visual = toColorVisual(players[i].color, true)
            addComponents(cornerBackground[i])
        }
        separationLines.forEach { addComponents(it) }

        //initialize grid
        updateGameBoardGridPane(gameBoard)
        addComponents(gameBoardGridPane, gameBoardDropAcceptor)

        //initialize player blocks
        initializePlayerBlocks(listOf(0,1,2,3))
        playerBlocks.forEach { block -> block.forEach { addComponents(it) } }

        //initialize score and currentPlayerLabel
        updateScoreLabelsAndCurrentPlayerLabel()
        playerScores.forEach { addComponents(it) }
        addComponents(currentPlayerLabel)

        //initialize buttons
        addComponents(saveButton, undoButton, redoButton, exitButton, rotateButton, flipButton, pauseButton)

        //initialize playerLabels
        for(i in 0..3) {
            playerNameLabels[i].text = players[i].name
            playerNameLabels[i].visual = toColorVisual(players[i].color)
            playerNameLabels[i].posX = (xOffset[i] + 10).toDouble()
            playerNameLabels[i].posY = (yOffset[i] + 15).toDouble()
        }
        playerNameLabels.forEach { addComponents(it) }

        //toggle Activation
        toggleAccess()
    }

    /**
     * refreshes the scene after a block was rotated or flipped
     */
    override fun refreshAfterBlockManipulation(block: Block) {
        if (rootService.currentGameState!!.boardSize != 20) return
        updateBlock(currentPlayerIndex, rootService.currentGameState!!.currentPlayer.blocks.indexOf(block))
    }

    /**
     * refreshes the scene after an offline player or local ai placed a block
     */
    override fun refreshAfterBlockPlaced(gameBoard: Array<Array<PlayerColor>>,
                                         removedBlock: Block, coordY: Int, coordX: Int) {
        if (rootService.currentGameState!!.boardSize != 20) return
        //update grid
        for (i in 0..4){
            for(j in 0..4){
                val yCoord = coordY + i
                val xCoord = coordX + j
                if(yCoord in 0..19 && xCoord in 0..19 && gameBoard[yCoord][xCoord] != PlayerColor.BLANK){
                    gameBoardGridPane[xCoord, yCoord]!!.visual = toImageVisual(gameBoard[yCoord][xCoord])
                    gameBoardGridPane[xCoord, yCoord]!!.name = gameBoard[yCoord][xCoord].toString()
                }
            }
        }

        // human dummy handling
        if (rootService.currentGameState!!.currentPlayer.type == PlayerType.DUMMY) {
            val currentDummyPlayerIndex = rootService.currentGameState!!.players
                .indexOf(rootService.currentGameState!!.currentDummyController)
            rootService.currentGameState!!.currentDummyController =
                rootService.currentGameState!!.players[(currentDummyPlayerIndex+1)%3]
        }

        //update playerBlocks
        val removedBlockIndex = rootService.currentGameState!!.previous!!.currentPlayer.blocks.indexOfFirst {
            it.blockID == removedBlock.blockID
        }
        val indexOfPlayerWithRemovedBlocks = rootService.currentGameState!!.players
            .indexOf(rootService.currentGameState!!.currentPlayer)
        if (removedBlockIndex in playerBlocks[indexOfPlayerWithRemovedBlocks].indices) {
            removeComponents(playerBlocks[indexOfPlayerWithRemovedBlocks][removedBlockIndex])
            playerBlocks[indexOfPlayerWithRemovedBlocks].removeAt(removedBlockIndex)
            for (i in removedBlockIndex until playerBlocks[currentPlayerIndex].size) {
                playerBlocks[currentPlayerIndex][i].name = i.toString()
            }
        }

        //update scoreLabels and currentPlayerLabel
        var nextPlayer = rootService.currentGameState!!.currentPlayer
        val game = rootService.currentGameState!!
        for(i in 1 .. game.players.size){
            if(game.players[(game.players.indexOf(game.currentPlayer) + i) % game.players.size].isPlayable){
                nextPlayer = rootService.currentGameState!!.players[(currentPlayerIndex + i)
                        % rootService.currentGameState!!.players.size]
                updateScoreLabelsAndCurrentPlayerLabel((currentPlayerIndex + i)
                        % rootService.currentGameState!!.players.size)
                toggleAccess((currentPlayerIndex + i) % rootService.currentGameState!!.players.size)
                break
            }
        }

        if(nextPlayer.type == PlayerType.DUMMY) nextPlayer = game.currentDummyController!!

        pauseButton.isDisabled = false

        //next player (for ai with delay)
        executeNextPlayer.add(true)
        val index = executeNextPlayer.size - 1
        if(nextPlayer.type in rootService.aIService.aiValueList && !isPaused){
            playAnimation(DelayAnimation(duration = simulationSpeed).apply {
                onFinished = {
                    pauseButton.isDisabled = true
                    playAnimation(DelayAnimation(duration = 0).apply {
                        onFinished = {
                            if(executeNextPlayer[index]) { rootService.gameService.nextPlayer() }
                            else pauseButton.isDisabled = false
                        }
                    })
                }
            })
        }
        else if(!isPaused){
            rootService.gameService.nextPlayer()
        }
    }

    /**
     * refreshes the scene after an online player placed a block
     */
    override fun refreshAfterNetworkBlockPlaced(pieceCoords: List<Pair<Int, Int>>, block: BlockID) {
        if (rootService.currentGameState!!.boardSize != 20) return
        updateGameBoardGridPane(rootService.currentGameState!!.gameBoard)

        //update playerBlocks
        val removedBlockIndex = rootService.currentGameState!!.previous!!.currentPlayer.blocks
            .indexOfFirst { it.blockID == block }
        removeComponents(playerBlocks[currentPlayerIndex][removedBlockIndex])
        playerBlocks[currentPlayerIndex].removeAt(removedBlockIndex)
        for (i in removedBlockIndex until playerBlocks[currentPlayerIndex].size) {
            playerBlocks[currentPlayerIndex][i].name = i.toString()
        }

        //update scoreLabels and currentPlayerLabel
        var nextPlayer = rootService.currentGameState!!.currentPlayer
        val game = rootService.currentGameState!!
        for(i in 1 .. game.players.size){
            if(game.players[(game.players.indexOf(game.currentPlayer) + i) % game.players.size].isPlayable){
                nextPlayer = rootService.currentGameState!!.players[(currentPlayerIndex + i)
                        % rootService.currentGameState!!.players.size]
                updateScoreLabelsAndCurrentPlayerLabel((currentPlayerIndex + i)
                        % rootService.currentGameState!!.players.size)
                toggleAccess((currentPlayerIndex + i) % rootService.currentGameState!!.players.size)
                break
            }
        }

        if(nextPlayer.type == PlayerType.DUMMY) nextPlayer = game.currentDummyController!!

        pauseButton.isDisabled = false

        //next player (for ai with delay)
        executeNextPlayer.add(true)
        val index = executeNextPlayer.size - 1
        if(nextPlayer.type in rootService.aIService.aiValueList && !isPaused){
            playAnimation(DelayAnimation(duration = simulationSpeed).apply {
                onFinished = {
                    pauseButton.isDisabled = true
                    playAnimation(DelayAnimation(duration = 0).apply {
                        onFinished = {
                            if(executeNextPlayer[index]) { rootService.gameService.nextPlayer() }
                            else pauseButton.isDisabled = false
                        }
                    })
                }
            })
        }
        else if(!isPaused){
            rootService.gameService.nextPlayer()
        }
    }

    /**
     * refreshes the scene after the current player changed
     */
    override fun refreshAfterNextPlayer(newCurrentPlayer: Player) {
        if (rootService.currentGameState!!.boardSize != 20) return
        //updateCurrentPlayer and selectedBlock
        currentPlayerIndex = rootService.currentGameState!!.players.indexOf(newCurrentPlayer)
        selectedBlockIndex = -1

        // dummy handling
        val nextPlayer = rootService.currentGameState!!.currentPlayer
        if(nextPlayer.type == PlayerType.DUMMY) {
            //playerNameLabels[3].text = rootService.currentGameState!!.players[dummyControllerIndex].name
            //dummyControllerIndex = (dummyControllerIndex+1)%3
            playerNameLabels[3].text = rootService.currentGameState!!.currentDummyController!!.name
        } else if (rootService.currentGameState!!.players.any { it.type == PlayerType.DUMMY }){
            playerNameLabels[3].text = "Dummy"
        }

        //update scoreLabels and currentPlayerLabel
        updateScoreLabelsAndCurrentPlayerLabel()

        //update buttons
        toggleAccess()
    }

    /**
     * refreshes the scene after the game ended
     */
    override fun refreshAfterGameEnd(players: List<Player>) {
        if (rootService.currentGameState!!.boardSize != 20) return
        val currentGameState = rootService.currentGameState!!
        val playerNumber = currentGameState.players.indexOf(currentGameState.currentPlayer)
        if(!this.components.contains(cornerGreyForeground[playerNumber])){
            addComponents(cornerGreyForeground[playerNumber])
        }
        if(!currentGameState.players[playerNumber].isPlayable){
            cornerGreyForeground[playerNumber].visual = ColorVisual(0,0,0,140)
            cornerGreyForeground[playerNumber].text = "No possible moves left!"
            cornerGreyForeground[playerNumber].font = Font(
                fontWeight = Font.FontWeight.SEMI_BOLD, size = 40, color = Color.WHITE)
        }
        updateScoreLabelsAndCurrentPlayerLabel()



        playAnimation(DelayAnimation(duration = 2000).apply {
            onFinished = {
                resetScene()
                blokusApplication.showGameEndMenuScene()
            }
        })
    }

    /**
     * refreshes the scene after a move was undone ore redone
     */
    override fun refreshAfterUndoRedo(newGameState: BlokusGameState) {
        if (rootService.currentGameState!!.boardSize != 20) return
        //update currentPlayer and selected block
        currentPlayerIndex = newGameState.players.indexOf(newGameState.currentPlayer)
        selectedBlockIndex = -1

        //update PlayerBlocks
        val previousPlayerIndex = (newGameState.players.size +newGameState.players.size + currentPlayerIndex - 1) %
                newGameState.players.size

        playerBlocks[currentPlayerIndex].forEach { removeComponents(it) }
        playerBlocks[previousPlayerIndex].forEach { removeComponents(it) }
        playerBlocks[currentPlayerIndex].removeAll { true }
        playerBlocks[previousPlayerIndex].removeAll { true }
        initializePlayerBlocks(listOf(previousPlayerIndex,currentPlayerIndex))
        playerBlocks[currentPlayerIndex].forEach { addComponents(it) }
        playerBlocks[previousPlayerIndex].forEach { addComponents(it) }

        //initialize gameBoard
        updateGameBoardGridPane(newGameState.gameBoard)

        //update PlayerScores and currentPlayerLabel
        updateScoreLabelsAndCurrentPlayerLabel()

        //update buttons
        toggleAccess()
        if(rootService.currentGameState!!.currentPlayer.type in rootService.aIService.aiValueList){
            pauseButton.visual = ImageVisual("play.png")
            isPaused = true
            executeNextPlayer.replaceAll { false }
        }
        else if(rootService.currentGameState!!.currentPlayer.type == PlayerType.DUMMY
            && rootService.currentGameState!!.currentDummyController!!.type in rootService.aIService.aiValueList){
            isPaused = true
            pauseButton.visual = ImageVisual("play.png")
            executeNextPlayer.replaceAll { false }
        }
        else{
            pauseButton.visual = ImageVisual("pause.png")
            isPaused = false
        }
    }

    /**
     * maps player colors to color visuals
     */
    private fun toColorVisual(color : PlayerColor, weak: Boolean = false): ColorVisual{
        if(!weak){
            return when (color) {
                PlayerColor.GREEN -> ColorVisual(Color(84,130,53))
                PlayerColor.BLUE -> ColorVisual(Color(31,78,120))
                PlayerColor.YELLOW -> ColorVisual(Color(191 ,143,0))
                PlayerColor.RED -> ColorVisual(Color(198,89,17))
                else -> ColorVisual.TRANSPARENT
            }
        }
        else{
            return when (color) {
                PlayerColor.GREEN -> ColorVisual(Color(198,224,180))
                PlayerColor.BLUE -> ColorVisual(Color(189,215,238))
                PlayerColor.YELLOW -> ColorVisual(Color(255,230,153))
                PlayerColor.RED -> ColorVisual(Color(248,203,173))
                else -> ColorVisual.TRANSPARENT
            }
        }
    }

    /**
     * maps player colors to their block image visual
     */
    private fun toImageVisual(color : PlayerColor) : ImageVisual{
        return when (color) {
            PlayerColor.GREEN -> blockImageVisuals[0]
            PlayerColor.BLUE -> blockImageVisuals[1]
            PlayerColor.YELLOW -> blockImageVisuals[2]
            PlayerColor.RED -> blockImageVisuals[3]
            else -> blockImageVisuals[4]
        }
    }

    /**
     * updates the central 20 c 20 grid
     */
    private fun updateGameBoardGridPane(board : Array<Array<PlayerColor>>){
        for (posX in 0..19){
            for (posY in 0..19){
                if(gameBoardGridPane[posX, posY] == null){
                    gameBoardGridPane[posX, posY] = Label(
                        width = placedBlockLength,
                        height = placedBlockLength,
                    )
                    gameBoardGridPane[posX, posY]!!.visual = toImageVisual(board[posY][posX])
                    gameBoardGridPane[posX, posY]!!.name = board[posY][posX].toString()
                }
                else{
                    if(board[posY][posX].toString() != gameBoardGridPane[posX, posY]!!.name){
                        gameBoardGridPane[posX, posY]!!.visual = toImageVisual(board[posY][posX])
                        gameBoardGridPane[posX, posY]!!.name = board[posY][posX].toString()
                    }
                }
            }
        }
    }

    /**
     * initializes and refreshes the player blocks
     *
     * @param playerNumbers for the players whose blocks need to be initialized
     */
    private fun initializePlayerBlocks(playerNumbers : List<Int>){
        val players = rootService.currentGameState!!.players
        val blockTypes = BlockID.values()
        for(playerNumber in playerNumbers){
            val givenBlockTypes =  players[playerNumber].blocks.map { it.blockID }.toList()
            for((blockNumber, posNumber) in (3 ..23).withIndex()){
                if(givenBlockTypes.indexOfFirst { it == blockTypes[blockNumber] } >= 0){
                    playerBlocks[playerNumber].add(Area(
                        posX = xOffset[playerNumber] + (posNumber * 20 * 5) % (20 * 5 * 5),
                        posY = yOffset[playerNumber] + (posNumber / 5) * 20 * 5,
                        width = 5 * unplacedBlockLength,
                        height = 5 * unplacedBlockLength
                    ))
                    if(posNumber >= 20){
                        playerBlocks[playerNumber].last().posX = (xOffset[playerNumber] +
                                (posNumber * unplacedBlockLength * 6) % (unplacedBlockLength * 6 * 5)).toDouble()
                        playerBlocks[playerNumber].last().posY = (yOffset[playerNumber] +
                                (posNumber / 5) * 20 * 5).toDouble()
                    }
                    playerBlocks[playerNumber].last().name = (playerBlocks[playerNumber].size - 1).toString()
                    updateBlock(playerNumber, playerBlocks[playerNumber].size - 1)
                }
            }
        }
        playerBlocks.forEach { it.forEach { area -> area.onMousePressed =
            {updateComponentsAfterBlockSelected(area.name.toInt())} } }
        playerBlocks.forEach { it.forEach { area -> area.onDragGestureStarted =
            {updateComponentsAfterBlockSelected(area.name.toInt(), true)} } }
    }

    /**
     * updates a specific player block
     */
    private fun updateBlock(playerNumber : Int, blockNumber : Int){
        val block = playerBlocks[playerNumber][blockNumber]
        block.removeAll { true }
        val players = rootService.currentGameState!!.players
        for(i in 0..24){
            if(players[playerNumber].blocks[blockNumber].shape[i / 5][i % 5]){
                block.add(TokenView(
                    posX = (i * unplacedBlockLength) % (5 * unplacedBlockLength),
                    posY = (i / 5) * unplacedBlockLength,
                    width = unplacedBlockLength,
                    height = unplacedBlockLength,
                    visual = toImageVisual(players[playerNumber].color)
                ))
            }
        }
    }

    /**
     * updates rotate and flip button on block selection, also scales the coressponding blocks right
     *
     * @param blockNumber blockIndex of the selected block
     * @param onDrag indicates whether the block was selected through mouseclick or draggesture
     */
    private fun updateComponentsAfterBlockSelected(blockNumber: Int, onDrag: Boolean = false){
        currentPlayerIndex = rootService.currentGameState!!.players.indexOf(rootService.currentGameState!!.currentPlayer)
        if(selectedBlockIndex < 0){
            selectedBlockIndex = blockNumber
            playerBlocks[currentPlayerIndex][blockNumber].scale(placedBlockLength.toDouble()
                    / unplacedBlockLength.toDouble())
            rotateButton.isDisabled = false
            flipButton.isDisabled = false
        }
        else if (selectedBlockIndex == blockNumber && onDrag){
            return
        }
        else if (selectedBlockIndex == blockNumber){
            selectedBlockIndex = -1
            playerBlocks[currentPlayerIndex][blockNumber].scale(1)
            rotateButton.isDisabled = true
            flipButton.isDisabled = true
        }
        else{
            selectedBlockIndex = blockNumber
            playerBlocks[currentPlayerIndex].forEach { it.scale(1) }
            playerBlocks[currentPlayerIndex][blockNumber].scale(placedBlockLength.toDouble() /
                    unplacedBlockLength.toDouble())
        }
    }

    /**
     * toggles the access to the player blocks, undo and redo button and the corner foregrounds
     *
     * @param subCurrentPlayerIndex index used to mark another player than the real current player
     */
    private fun toggleAccess(subCurrentPlayerIndex: Int = rootService.currentGameState!!.players.indexOf(
        rootService.currentGameState!!.currentPlayer)
    ){
        val currentGameState = rootService.currentGameState!!
        if(PlayerType.NET_PLAYER in currentGameState.players.map { it.type }.toList()){
            undoButton.isDisabled = true
            redoButton.isDisabled = true
        }
        else{
            undoButton.isDisabled = currentGameState.previous == null
            redoButton.isDisabled = currentGameState.next == null
        }

        rotateButton.isDisabled = selectedBlockIndex < 0
        flipButton.isDisabled = selectedBlockIndex < 0

        for (playerNumber in 0..3){
            if(playerNumber == subCurrentPlayerIndex){
                playerBlocks[playerNumber].forEach { it.isDraggable = true; it.isDisabled = false }
                if (this.components.contains(cornerGreyForeground[playerNumber])){
                    removeComponents(cornerGreyForeground[playerNumber])
                }
                if(currentGameState.players[playerNumber].type in rootService.aIService.aiValueList
                    || currentGameState.players[playerNumber].type == PlayerType.NET_PLAYER){
                    playerBlocks[playerNumber].forEach { it.isDraggable = false; it.isDisabled = true }
                }
                else if(currentGameState.players[playerNumber].type == PlayerType.DUMMY &&
                    rootService.currentGameState!!.currentDummyController!!.type
                    in rootService.aIService.aiValueList){
                    playerBlocks[playerNumber].forEach { it.isDraggable = false; it.isDisabled = true }
                }
            }
            else{
                playerBlocks[playerNumber].forEach { it.isDraggable = false; it.isDisabled = true }
                if(!this.components.contains(cornerGreyForeground[playerNumber])){
                    addComponents(cornerGreyForeground[playerNumber])
                }
                if(!currentGameState.players[playerNumber].isPlayable){
                    cornerGreyForeground[playerNumber].visual = ColorVisual(0,0,0,140)
                    cornerGreyForeground[playerNumber].text = "No possible moves left!"
                    cornerGreyForeground[playerNumber].font =
                        Font(fontWeight = Font.FontWeight.SEMI_BOLD, size = 40, color = Color.WHITE)
                    cornerGreyForeground[playerNumber].isWrapText = true
                }
            }
        }
    }

    /**
     * updates the score labels and the currentPlayerLabel
     *
     * @param subCurrentPlayerIndex index used to mark another player than the real current player
     */
    private fun updateScoreLabelsAndCurrentPlayerLabel(
        subCurrentPlayerIndex: Int = rootService.currentGameState!!.players.indexOf(
            rootService.currentGameState!!.currentPlayer)
    ){
        val currentGameState = rootService.currentGameState!!
        for(i in 0..3){
            playerScores[i].text = currentGameState.players[i].score.toString()
            playerScores[i].visual = toColorVisual(currentGameState.players[i].color)
        }

        val currentPlayerType = if(currentGameState.players[subCurrentPlayerIndex].type != PlayerType.DUMMY) {
            currentGameState.players[subCurrentPlayerIndex].type
        } else if (currentGameState.currentPlayer.type == PlayerType.DUMMY) {
            PlayerType.DUMMY
        }
        else{
            currentGameState.currentDummyController!!.type
        }

        when (currentPlayerType) {
            PlayerType.HUMAN -> {
                currentPlayerLabel.text = "${currentGameState.players[subCurrentPlayerIndex].name}'s turn"
                currentPlayerLabel.visual = toColorVisual(currentGameState.players[subCurrentPlayerIndex].color)
                currentPlayerLabel.font = Font(size = 40, color = Color.WHITE, fontWeight = Font.FontWeight.SEMI_BOLD)
                currentPlayerLabel.isWrapText = false
            }
            PlayerType.DUMMY -> {
                currentPlayerLabel.text = "${currentGameState.currentDummyController!!.name}'s turn"
                currentPlayerLabel.visual = toColorVisual(currentGameState.currentDummyController!!.color)
                currentPlayerLabel.font = Font(size = 40, color = Color.WHITE, fontWeight = Font.FontWeight.SEMI_BOLD)
                currentPlayerLabel.isWrapText = false
            }
            in rootService.aIService.aiValueList -> {
                currentPlayerLabel.text = "Waiting for AI..."
                currentPlayerLabel.visual = toColorVisual(currentGameState.players[subCurrentPlayerIndex].color)
                currentPlayerLabel.font = Font(size = 30, color = Color.WHITE, fontWeight = Font.FontWeight.SEMI_BOLD)
            }
            PlayerType.NET_PLAYER -> {
                currentPlayerLabel.text = "Waiting for Online Player..."
                currentPlayerLabel.visual = toColorVisual(currentGameState.players[subCurrentPlayerIndex].color)
                currentPlayerLabel.font = Font(size = 26, color = Color.WHITE, fontWeight = Font.FontWeight.SEMI_BOLD)
            }

            else -> {}
        }
    }

    private fun resetScene() {
        //resetting the scene
        currentPlayerIndex = -1
        selectedBlockIndex = -1
        simulationSpeed = 1000
        isPaused = false
        pauseButton.visual = ImageVisual("pause.png")
        pauseButton.isDisabled = false
        components.forEach{removeComponents(it)}
        playerBlocks.forEach { it.removeAll { true } }
        cornerGreyForeground.forEach { it.text = "" }
    }
}