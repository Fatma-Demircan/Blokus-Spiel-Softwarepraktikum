package service.messages

import entity.PlayerColor
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * [BlokusGameInitMessage], represents the Message, which is sent by the start of the Game
 * @param twoPlayerVariant, symbolized, if the Board should be the small Board
 * @param turnOrder, is the Order of the Players, by their entries
 */
@GameActionClass
data class BlokusGameInitMessage(
    val twoPlayerVariant: Boolean,
    val turnOrder: List<Pair<String, PlayerColor>>
    ): GameAction()
