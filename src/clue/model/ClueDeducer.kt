package clue.model

import clue.model.game.*
import clue.model.logic.Clause
import clue.model.logic.ClauseList
import clue.model.logic.Deduction
import clue.model.logic.Literal
import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr
import com.microsoft.z3.Status

/**
 * Represents the "model" in the MVC pattern
 */
class ClueDeducer(
    players : List<String>,
    weapons : List<String>,
    people : List<String>,
    rooms : List<String>) {
    private val clauseList : ClauseList = ClauseList()

    private val gameManager : GameManager =
        GameManager(
            players,
            listOf(
                (people.map {str -> Person(str)}),
                (rooms.map {str -> Room(str) }),
                (weapons.map {str -> Weapon(str)})
            ).flatten()
        )

    init {
        val playersAndCF : MutableList<Location> = mutableListOf(Location())
        playersAndCF.addAll(players.map {p : String -> gameManager.locationOfPlayer(p)!!})

        // Cards are in at least one place
        for (card in gameManager.cards) {
            clauseList.addClause(
                Clause(
                    playersAndCF.map {loc -> gameManager.literalOfCard(card, loc)!!}
                )
            )
        }

        // If a card is in one position, it cannot be in another position
        for (card in gameManager.cards) {
            for (loc in playersAndCF) {
                for (otherLoc in playersAndCF.filter {o -> loc != o}) {
                    clauseList.addClause(
                        Clause(
                            listOf(
                                gameManager.literalOfCard(card, loc)!!.negation(),
                                gameManager.literalOfCard(card, otherLoc)!!.negation()
                            )
                        )
                    )
                }
            }
        }

        // At least one card of each category must be in the case file
        clauseList.addClause(
            Clause(
                gameManager.people.map { card -> gameManager.literalOfCard(card, Location())!!}
            )
        )
        clauseList.addClause(
            Clause(
                gameManager.weapons.map { card -> gameManager.literalOfCard(card, Location())!!}
            )
        )
        clauseList.addClause(
            Clause(
                gameManager.rooms.map { card -> gameManager.literalOfCard(card, Location())!!}
            )
        )

        // No two cards of the same category can be in the case file
        for(lst in listOf(gameManager.people, gameManager.weapons, gameManager.rooms)) {
            for(p in lst) {
                for (other in lst.filter {it != p}) {
                    clauseList.addClause(Clause(
                        listOf(
                            gameManager.literalOfCard(p, Location())!!.negation(),
                            gameManager.literalOfCard(other, Location())!!.negation()
                        )
                    ))
                }
            }
        }
    }

    /**
     * Determines which card is implied by the knowledge base, if any
     */
    private fun <T : Card> findImplies(
        cards : List<T>,
        knowledge : Expr<BoolSort>,
        ctx : Context
        ) : T? {
        val caseFile = Location()

        for(card in cards) {
            val cardCfLiteral = gameManager.literalOfCard(card, caseFile)!!

//            println("Determining if $cardCfLiteral is implied by the knowledge")

//            println("Knowledge: $knowledge")

            val literal = cardCfLiteral.convertToZ3(ctx)

            val resolution = ctx.mkAnd(knowledge, ctx.mkNot(literal))

//            println("Resolution: $resolution")
            val solver = ctx.mkSolver()

            solver.add(resolution)

            if(solver.check() == Status.UNSATISFIABLE) {
                return card
//                println("Satisfiable, invalid")
            }
        }

        return null
    }

    /**
     * Returns the current deduction for the case file
     */
    fun getCurrentDeduction() : Deduction {
        val ctx : Context = Context()
        val know = clauseList.convertToZ3(ctx)

        // Our expression should not be necessarily false
        val testSolver = ctx.mkSolver()
        testSolver.add(know)
        assert(testSolver.check() == Status.SATISFIABLE)

        val weapon = findImplies(gameManager.weapons, know, ctx)
        val person = findImplies(gameManager.people, know, ctx)
        val room = findImplies(gameManager.rooms, know, ctx)

        return Deduction(person, room, weapon)
    }

    fun addHandInfo(player : String, hand : List<Card>) : Boolean {
        val loc = gameManager.locationOfPlayer(player) ?: return false
        val literals : List<Literal?> = hand.map {c -> gameManager.literalOfCard(c, loc)}
        val notInHand = gameManager.cards.filterNot {hand.contains(it)} .map {c -> gameManager.literalOfCard(c, loc)}
        println("Adding hand info.")
        println(literals)

        if(literals.contains(null)) {
            println("WARNING: HAND INFO CONTAINS NULLS")
            return false
        } else {
            // This is safe because we check if the list contains null above
            (literals as List<Literal>).forEach {clauseList.addClause(Clause(it))}
            (notInHand as List<Literal>).forEach {clauseList.addClause(Clause(it.negation()))}

            return true
        }
    }

    /**
     * Add the results of a Clue: Master Detective hand inspection
     */
    fun addInspection(target : String, card : Card) : Boolean {
        val loc = gameManager.locationOfPlayer(target) ?: return false
        val lit = gameManager.literalOfCard(card, loc) ?: return false

        clauseList.addClause(Clause(lit))

        return true
    }

    /**
     * This function is for using the ruleset of multiple people being able to refute a suggestion
     */
    fun multiSuggest(
        suggester : String,
        person : Person,
        room : Room,
        weapon : Weapon,
        refuters : Map<String, Card?>
    ) : Boolean {
        val loc = gameManager.locationOfPlayer(suggester) ?: return false

        // ensure precondition of valid cards
        if(!gameManager.isValidCard(person)
            || !gameManager.isValidCard(weapon)
            || !gameManager.isValidCard(room)
        ) {
            return false
        }

        val doesNotHave = gameManager.players.filterNot {player -> refuters.keys.contains(player)}

        addDoNotHave(
            (doesNotHave.map {gameManager.locationOfPlayer(it)}).filterNotNull(),
            person,
            room,
            weapon
        )

        for((player, card) in refuters) {
            val locOfPlayer = gameManager.locationOfPlayer(player) ?: continue
            when(card) {
                null -> addCardShownUnknown(locOfPlayer, room, weapon, person)
                else -> addCardShown(locOfPlayer, card)
            }
        }

        return true
    }

    /**
     * Add a suggestion of the form "[suggester] thinks that it was [person] in [room] with the [weapon]"
     * If [refuter] is non-null, then refuter showed [cardShown] to [suggester] to refute the suggestion.
     * If [cardShown] is null when [refuter] is non-null, the observer could not see the card.
     */
    fun suggest(
        suggester : String,
        person : Person,
        weapon : Weapon,
        room : Room,
        refuter : String? = null,
        cardShown : Card? = null
    ) : Boolean {
        if(!gameManager.isValidPlayer(suggester)) {
            println("Invalid suggester")
            return false
        }

        // ensure precondition of valid cards
        if(!gameManager.isValidCard(person)
            || !gameManager.isValidCard(weapon)
            || !gameManager.isValidCard(room)
        ) {
            println("Suggestion $person $weapon $room failed.")
            println("Cards: ${gameManager.cards}")
            return false
        }

        val doesNotHave =
            if (refuter == null)
                gameManager.players.filter { it != suggester }
            else {
                val indexOfRefuter = gameManager.players.indexOf(refuter)

                assert (indexOfRefuter != -1)

                gameManager.players.subList(0, indexOfRefuter).filter {it != suggester}
            }

        assert (!(doesNotHave.map {gameManager.locationOfPlayer(it)}).contains(null))

        addDoNotHave(
            (doesNotHave.map {gameManager.locationOfPlayer(it)}).filterNotNull(),
            person,
            room,
            weapon
        )

        if(refuter != null) {
            assert (gameManager.isValidPlayer(refuter))
            val refuterLoc = gameManager.locationOfPlayer(refuter) ?: return false

            if(cardShown == null) {
                addCardShownUnknown(refuterLoc, room, weapon, person)
            } else {
                addCardShown(refuterLoc, cardShown)
            }
        }

        return true
    }

    // Precondition: the card shown is a valid card
    private fun addCardShown(refuter : Location, cardShown : Card)  {
        assert(gameManager.isValidCard(cardShown))

        val lit = gameManager.literalOfCard(cardShown, refuter) ?: return

        clauseList.addClause(Clause(lit))
    }

    // Precondition: weapon, person, and room are valid cards
    private fun addCardShownUnknown(refuter : Location, room : Room, weapon : Weapon, person : Person)  {
        assert(gameManager.isValidCard(weapon))
        assert(gameManager.isValidCard(person))
        assert(gameManager.isValidCard(room))

        val roomLit = gameManager.literalOfCard(room, refuter) ?: return
        val weaponLit = gameManager.literalOfCard(weapon, refuter) ?: return
        val personLit = gameManager.literalOfCard(room, refuter) ?: return

        clauseList.addClause(Clause(listOf(roomLit, weaponLit, personLit)))
    }

    /**
     * Add clauses for the people in the list not having the selected cards
     * Precondition: person, room, and weapon are valid cards
     */
    private fun addDoNotHave(
        doesNotHave : List<Location>,
        person : Person,
        room : Room,
        weapon : Weapon
    )  {
        assert(gameManager.isValidCard(weapon))
        assert(gameManager.isValidCard(person))
        assert(gameManager.isValidCard(room))

        println("Do not have: $doesNotHave")

        for(player in doesNotHave) {
            clauseList.addClause(Clause(gameManager.literalOfCard(person, player)!!.negation()))
            clauseList.addClause(Clause(gameManager.literalOfCard(room, player)!!.negation()))
            clauseList.addClause(Clause(gameManager.literalOfCard(weapon, player)!!.negation()))
        }
    }
}