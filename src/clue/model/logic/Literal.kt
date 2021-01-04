package clue.model.logic

import clue.model.game.Location
import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr

class Literal(private val number : Int, private val cardName : String, private val location : Location?) : Z3Convertible {

    constructor(number : Int) : this(number, "no name", null)

    fun negation() : Literal {
        return Literal(-number)
    }

    override fun convertToZ3(ctx: Context): Expr<BoolSort> =
        if(number < 0) {
            ctx.mkNot(ctx.mkConst((-number).toString(), ctx.boolSort))
        } else {
            ctx.mkConst(number.toString(), ctx.boolSort)
        }

    override fun toString(): String {
        return "${number.toString()} ($cardName, $location)"
    }
}