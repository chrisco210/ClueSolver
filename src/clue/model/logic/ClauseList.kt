package clue.model.logic

import com.microsoft.z3.*

class ClauseList() : Z3Convertible {
    var clauses : MutableList<Clause> = mutableListOf()

    fun addClause(c : Clause) {
        clauses.add(c)
    }

    override fun convertToZ3(ctx: Context): Expr<BoolSort> =
        clauses.fold(
            ctx.mkTrue(),
            {acc, cur -> ctx.mkAnd(acc, cur.convertToZ3(ctx))}
        )
}