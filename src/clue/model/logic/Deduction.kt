package clue.model.logic

import clue.model.game.Person
import clue.model.game.Room
import clue.model.game.Weapon

/**
 * Represents a clue deduction
 */
data class Deduction(val person : Person?, val room : Room?, val weapon : Weapon?)