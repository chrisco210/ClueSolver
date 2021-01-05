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
//    testGame()
    testGame2()
}

fun simpleGame() {
    val PEOPLE = listOf("person1", "person2")
    val WEAPONS = listOf("weapon1", "weapon2")
    val ROOMS = listOf("room1", "room2")
    val PLAYERS = listOf("player1", "player2")

    val deducer = ClueDeducer(PLAYERS, WEAPONS, PEOPLE, ROOMS)

    deducer.addHandInfo("player1", listOf(Weapon("weapon2")))
    // The deducer will be player1
    deducer.suggest("player1", Person("person2"), Weapon("weapon2"), Room("room2"), "player2", Person("person2"))
    deducer.suggest("player1", Person("person2"), Weapon("weapon2"), Room("room2"), "player2", Room("room2"))
    deducer.suggest("player1", Person("person1"), Weapon("weapon1"), Room("room2"), "player2", Room("room2"))

    println(deducer.getCurrentDeduction())
}

fun testGame2() {
    println("Test game 2:")

    val PEOPLE = listOf("ro", "mu", "gr", "sc", "pe", "pk", "wh", "br", "pl", "gy")
    val ROOMS = listOf("di", "ch", "li", "dw", "cy", "co", "sd", "bi", "ga", "ki", "fo", "tr")
    val WEAPONS = listOf("pi", "ro", "ho", "cs", "re", "wr", "kn", "po")

    val PLAYERS = listOf("elena", "chris")

    val deducer = ClueDeducer(PLAYERS, WEAPONS, PEOPLE, ROOMS)

    val HAND = listOf(
        Person("pl"),
        Person("gr"),
        Person("gy"),
        Person("br"),
        Person("pe"),
        Room("di"),
        Room("li"),
        Room("ki"),
        Room("bi"),
        Room("cy"),
        Room("tr"),
        Weapon("ro"),
        Weapon("wr")
    )

    deducer.addHandInfo("chris", HAND)

    deducer.suggest("elena", Person("mu"), Weapon("ro"), Room("ga"), "chris", Weapon("ro"))
    deducer.suggest("chris", Person("gr"), Weapon("kn"), Room("ga"), "elena", Room("ga"))
    deducer.suggest("elena", Person("pl"), Weapon("kn"), Room("dw"), "chris", Person("pl"))
    deducer.suggest("chris", Person("pk"), Weapon("kn"), Room("dw"), "elena", Room("dw"))
    deducer.suggest("elena", Person("gr"), Weapon("kn"), Room("dw"), "chris", Person("gr"))
    deducer.suggest("chris", Person("ro"), Weapon("pi"), Room("ch"), "elena", Person("ro"))
    deducer.suggest("elena", Person("ro"), Weapon("pi"), Room("dw"), null, null)
    deducer.suggest("chris", Person("pl"), Weapon("pi"), Room("tr"), null, null)
    deducer.suggest("elena", Person("wh"), Weapon("pi"), Room("dw"), null, null)
    deducer.suggest("chris", Person("wh"), Weapon("pi"), Room("di"), null, null)
    deducer.suggest("elena", Person("wh"), Weapon("pi"), Room("ki"), "chris", Room("ki"))
    deducer.suggest("chris", Person("wh"), Weapon("pi"), Room("bi"), null, null)
    deducer.suggest("elena", Person("wh"), Weapon("pi"), Room("bi"), "chris", Room("bi"))
    deducer.suggest("chris", Person("wh"), Weapon("pi"), Room("ch"), null, null)
//    deducer.suggest("elena", Person("wh"), Weapon("pi"), Room("ch"), null, null)


    // Should be: wh pi ch
    println(deducer.getCurrentDeduction())
}

fun testGame() {
    println("Test game:")

    val PEOPLE = listOf("p1", "p2", "p3", "p4", "p5", "p6")
    val WEAPONS = listOf("w1", "w2", "w3", "w4", "w5", "w6")
    val ROOMS = listOf("r1", "r2", "r3", "r4", "r5", "r6")

    val PLAYERS = listOf("p1", "p2", "p3")

    val cr = ClueDeducer(PLAYERS, WEAPONS, PEOPLE, ROOMS)

    cr.addHandInfo("p1", listOf(Person("p2"), Person("p3"), Person("p4"), Person("p5"), Person("p6")))

    cr.suggest("p1", Person("p2"), Weapon("w1"), Room("r1"), null, null)


    println("Deduction final ${cr.getCurrentDeduction()}")
    println("Correct: p1, w1, r1")
}
