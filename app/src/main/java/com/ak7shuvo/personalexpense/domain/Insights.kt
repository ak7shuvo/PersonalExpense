package com.ak7shuvo.personalexpense.domain

import java.time.YearMonth

data class MonthTotal(val month: YearMonth, val income: Long, val expense: Long)
data class CategoryShare(val label: String, val amount: Long)

object Insights {
    /** Income and expense per month for the [count] months ending at [end] (oldest first). Loans/repayments excluded. */
    fun monthlyTotals(txns: List<Txn>, end: YearMonth, count: Int = 6): List<MonthTotal> =
        (count - 1 downTo 0).map { back ->
            val m = end.minusMonths(back.toLong())
            val inMonth = txns.filter { YearMonth.from(it.date) == m }
            MonthTotal(
                m,
                inMonth.filter { it.type == TxType.INCOME }.sumOf { it.amount },
                inMonth.filter { it.type == TxType.EXPENSE }.sumOf { it.amount },
            )
        }

    /** Expenses of [month] per category, biggest first; categories past [top] fold into "Other". */
    fun expenseByCategory(txns: List<Txn>, month: YearMonth, top: Int = 5): List<CategoryShare> {
        val sorted = txns.filter { it.type == TxType.EXPENSE && YearMonth.from(it.date) == month }
            .groupBy { it.label.trim().ifEmpty { "Other" } }
            .map { (k, v) -> CategoryShare(k, v.sumOf { it.amount }) }
            .sortedByDescending { it.amount }
        if (sorted.size <= top) return sorted
        val rest = sorted.drop(top).sumOf { it.amount }
        return sorted.take(top) + CategoryShare("Other", rest)
    }
}
