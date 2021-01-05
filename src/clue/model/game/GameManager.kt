package clue.model.game

import clue.model.logic.Literal

/**
 * Represents a game of Clue, with methods to get literals of cards and locations
 */
class GameManager(val players : List<String>, val cards : List<Card>) {

    private fun <T>nullableIndexOf(list : List<T>, match : T) : Int? =
        when (val idx = list.indexOf(match)) {
            -1 -> null
            else -> idx
        }

    private fun numberOfPlayer(player : String) : Int? =
        when(val i = nullableIndexOf(players, player)) {
            null -> null
            else -> i + 1
        }

    private fun numberOfCard(card : Card) : Int? = nullableIndexOf(cards, card)

    val weapons : List<Weapon>
        get() = cards.filterIsInstance<Weapon>()
    val rooms : List<Room>
        get() = cards.filterIsInstance<Room>()
    val people : List<Person>
        get() = cards.filterIsInstance<Person>()

    fun isValidPlayer(player : String) : Boolean = players.contains(player)

    fun isValidCard(card : Card) : Boolean = cards.contains(card)

    fun locationOfPlayer(player : String) : Location? {
        val ret = if(isValidPlayer(player)) Location(player) else null

//        println("Generated location $ret for player $player")

        return ret
    }

    /**
     * Returns a literal representing a card being in a specific location
     */
    fun literalOfCard(card : Card, location : Location) : Literal? {
        val cardNum = numberOfCard(card)
        val playerNum : Int? = if (location.isCaseFile) {
            0
        } else {
            numberOfPlayer(location.player)
        }

        return if(cardNum == null || playerNum == null) {
            println("cardNum not found: $card")
            null
        } else {
            Literal((playerNum) * (cards.size) + cardNum + 1, card.toString(), location)
        }
    }
}