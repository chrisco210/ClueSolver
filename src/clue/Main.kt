package clue

import clue.model.ClueDeducer
import clue.model.game.Location
import clue.model.game.Person
import clue.model.game.Room
import clue.model.game.Weapon
import com.microsoft.z3.*



// suggest(suggester, card1, card2, card3, refuter, cardShown)
//

val PLAYERS : List<String> = listOf()
fun main() {
//    simpleGame()
    testGame()
}

fun simpleGame() {
    val PEOPLE = listOf("person1", "person2")
    val WEAPONS = listOf("weapon1", "weapon2")
    val ROOMS = listOf("room1", "room2")
    val PLAYERS = listOf("player1", "player2")

    val deducer = ClueDeducer(PLAYERS, WEAPONS, PEOPLE, ROOMS)

    // The deducer will be player1
    val handInfo = deducer.addHandInfo("player1", listOf(Person("person1"), Weapon("weapon1"), Room("room1")))

    if(!handInfo) println("Hand info was false")

    println(deducer.getCurrentDeduction())
}

fun testGame() {
    println("Test game:")

    val PEOPLE = listOf("mu", "pl", "gr", "pe", "sc", "wh")
    val WEAPONS = listOf("kn", "ca", "re", "ro", "pi", "wr")
    val ROOMS = listOf("ha", "lo", "di", "ki", "ba", "co", "bi", "li", "st")

    val cr = ClueDeducer(PEOPLE, WEAPONS, PEOPLE, ROOMS)

    val us = "sc"

    cr.addHandInfo(us, listOf(Person("wh"), Room("li"), Room("st")))

    cr.suggest("sc", Person("sc"), Room("ro"), Weapon("lo"), "mu", Person("sc"))
    cr.suggest("mu", Person("pe"), Room("pi"), Weapon("di"), "pe", null)
    cr.suggest("wh", Person("mu"), Room("re"), Weapon("ba"), "pe", null)
    cr.suggest("gr", Person("wh"), Room("kn"), Weapon("ba"), "pl", null)
    cr.suggest("pe", Person("gr"), Room("ca"), Weapon("di"), "wh", null)
    cr.suggest("pl", Person("wh"), Room("wr"), Weapon("st"), "sc", Person("wh"))
    cr.suggest("sc", Person("pl"), Room("ro"), Weapon("co"), "mu", Person("pl"))
    cr.suggest("mu", Person("pe"), Room("ro"), Weapon("ba"), "wh", null)
    cr.suggest("wh", Person("mu"), Room("ca"), Weapon("st"), "gr", null)
    cr.suggest("gr", Person("pe"), Room("kn"), Weapon("di"), "pe", null)
    cr.suggest("pe", Person("mu"), Room("pi"), Weapon("di"), "pl", null)
    cr.suggest("pl", Person("gr"), Room("kn"), Weapon("co"), "wh", null)
    cr.suggest("sc", Person("pe"), Room("kn"), Weapon("lo"), "mu", Room("lo"))
    cr.suggest("mu", Person("pe"), Room("kn"), Weapon("di"), "wh", null)
    cr.suggest("wh", Person("pe"), Room("wr"), Weapon("ha"), "gr", null)
    cr.suggest("gr", Person("wh"), Room("pi"), Weapon("co"), "pl", null)
    cr.suggest("pe", Person("sc"), Room("pi"), Weapon("ha"), "mu", null)
    cr.suggest("pl", Person("pe"), Room("pi"), Weapon("ba"), null, null)
    cr.suggest("sc", Person("wh"), Room("pi"), Weapon("ha"), "pe", Room("ha"))
    cr.suggest("wh", Person("pe"), Room("pi"), Weapon("ha"), "pe", null)
    cr.suggest("pe", Person("pe"), Room("pi"), Weapon("ha"), null, null)
    cr.suggest("sc", Person("gr"), Room("pi"), Weapon("st"), "wh", Person("gr"))
    cr.suggest("mu", Person("pe"), Room("pi"), Weapon("ba"), "pl", null)
    cr.suggest("wh", Person("pe"), Room("pi"), Weapon("st"), "sc", Room("st"))
    cr.suggest("gr", Person("wh"), Room("pi"), Weapon("st"), "sc", Person("wh"))
    cr.suggest("pe", Person("wh"), Room("pi"), Weapon("st"), "sc", Person("wh"))
    cr.suggest("pl", Person("pe"), Room("pi"), Weapon("ki"), "gr", null)

    println("Current Deduction:")
    println(cr.getCurrentDeduction())
    println("Correct Deduction:")
    println("pe, pi, bi")
}
