package service.messages

import entity.BlockID
import entity.PlayerColor
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * [BlokusSetPieceMessage], represents the message for a move from a Player
 * @param pieceCoords, is the List of all Coordinates for the to be placed Block, Coordinates by the Board
 * @param gameBoard, is the Game-Board, to validate the B
 * @param color, is the Color of the Piece
 * @param piece, is the ID of the piece, which is getting placed
 */
@GameActionClass
data class BlokusSetPieceMessage(
    val pieceCoords: List<Pair<Int, Int>>,
    val gameBoard: Array<Array<PlayerColor>>,
    val color: PlayerColor,
    val piece: BlockID
):GameAction()
