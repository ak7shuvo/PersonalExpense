package com.ak7shuvo.personalexpense.domain

/** Pure financial rules. No Android, no database, no UI. */
object Ledger {

    fun summarize(txns: List<Txn>): Summary {
        var income = 0L; var expenses = 0L; var lent = 0L; var repaid = 0L
        for (t in txns) when (t.type) {
            TxType.INCOME -> income += t.amount
            TxType.EXPENSE -> expenses += t.amount
            TxType.LOAN -> lent += t.amount
            TxType.REPAYMENT -> repaid += t.amount
        }
        return Summary(income, expenses, lent, repaid)
    }

    /** Open loans first (newest first), then settled ones. */
    fun loanStatuses(txns: List<Txn>): List<LoanStatus> {
        val repayments = txns.filter { it.type == TxType.REPAYMENT }.groupBy { it.loanId }
        return txns.filter { it.type == TxType.LOAN }
            .map { LoanStatus(it, (repayments[it.id] ?: emptyList()).sortedByDescending { r -> r.date }) }
            .sortedWith(compareBy<LoanStatus> { it.isSettled }.thenByDescending { it.loan.date }.thenByDescending { it.loan.id })
    }

    sealed interface Problem {
        data object NonPositiveAmount : Problem
        data object BlankLabel : Problem
        data object NotFound : Problem
        data object LoanMissing : Problem
        data class ExceedsOutstanding(val outstanding: Long) : Problem
        data class BelowRepaid(val repaid: Long) : Problem
    }

    /**
     * Checks [draft] against the current [existing] records. For edits, [draft.id] must already exist and
     * is excluded from the loan arithmetic so a repayment can be re-saved at its own amount.
     */
    fun validate(draft: Txn, existing: List<Txn>): Problem? {
        if (draft.amount <= 0) return Problem.NonPositiveAmount
        if (draft.type != TxType.REPAYMENT && draft.label.isBlank()) return Problem.BlankLabel

        val old = if (draft.id != 0L) existing.firstOrNull { it.id == draft.id } else null
        if (draft.id != 0L && (old == null || old.type != draft.type)) return Problem.NotFound

        when (draft.type) {
            TxType.REPAYMENT -> {
                val loan = existing.firstOrNull { it.id == draft.loanId && it.type == TxType.LOAN }
                    ?: return Problem.LoanMissing
                val others = existing.filter { it.type == TxType.REPAYMENT && it.loanId == loan.id && it.id != draft.id }
                val outstanding = loan.amount - others.sumOf { it.amount }
                if (draft.amount > outstanding) return Problem.ExceedsOutstanding(outstanding)
            }
            TxType.LOAN -> if (old != null) {
                val repaid = existing.filter { it.type == TxType.REPAYMENT && it.loanId == old.id }.sumOf { it.amount }
                if (draft.amount < repaid) return Problem.BelowRepaid(repaid)
            }
            else -> Unit
        }
        return null
    }
}
