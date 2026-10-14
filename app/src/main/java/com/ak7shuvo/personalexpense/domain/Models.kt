package com.ak7shuvo.personalexpense.domain

import java.time.LocalDate

enum class TxType { INCOME, EXPENSE, LOAN, REPAYMENT }

/**
 * One ledger record. [amount] is in minor units (poisha, 1 ৳ = 100) so all arithmetic is exact.
 *
 * [label] is the income source, expense category or borrower name. A REPAYMENT points at its LOAN via
 * [loanId] and takes the borrower name from that loan.
 */
data class Txn(
    val id: Long,
    val type: TxType,
    val amount: Long,
    val label: String,
    val date: LocalDate,
    val note: String = "",
    val loanId: Long? = null,
)

data class Summary(
    val income: Long = 0,
    val expenses: Long = 0,
    val lent: Long = 0,
    val repaid: Long = 0,
) {
    /** Money still owed to me. */
    val outstanding: Long get() = lent - repaid

    /** Cash on hand: lending leaves the balance, repayments return to it. Neither is income/expense. */
    val balance: Long get() = income - expenses - lent + repaid
}

data class LoanStatus(
    val loan: Txn,
    val repayments: List<Txn>,
) {
    val repaid: Long get() = repayments.sumOf { it.amount }
    val outstanding: Long get() = loan.amount - repaid
    val isSettled: Boolean get() = outstanding <= 0
    val progress: Float get() = if (loan.amount <= 0) 0f else (repaid.toDouble() / loan.amount).toFloat().coerceIn(0f, 1f)
}
