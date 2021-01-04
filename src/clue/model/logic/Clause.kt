package clue.model.logic

import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr

/**
 * Represents a CNF clause
 */
class Clause(private val literals : List<Literal>) : Z3Convertible {

    constructor(l : Literal) : this(listOf(l))

    override fun convertToZ3(ctx: Context): Expr<BoolSort> =
        literals.fold(
            ctx.mkFalse(),
            {acc, cur -> ctx.mkOr(acc, cur.convertToZ3(ctx))}
        )

    val length : Int
        get() = literals.size
}