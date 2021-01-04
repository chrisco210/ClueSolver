package clue.model.game

/**
 * Represents a location where a card can be, either in the player represented by the string or in the case file
 * represented by a null value
 */
class Location(val player : String) {
    private var caseFile = false

    constructor() : this("") {
        caseFile = true
    }

    val isCaseFile
        get() = caseFile


    override fun equals(other: Any?): Boolean {
        return when(other) {
            is Location ->
                if(this.caseFile || other.caseFile)
                    (other.caseFile && this.caseFile)
                else
                    (player == other.player)

            else -> false
        }
    }

    override fun toString(): String =
        if(caseFile) {
            "Case file"
        } else {
            player
        }
}