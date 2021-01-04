package clue.model.logic

import com.microsoft.z3.BoolSort
import com.microsoft.z3.Context
import com.microsoft.z3.Expr

interface Z3Convertible {
    fun convertToZ3(ctx : Context) : Expr<BoolSort>
}