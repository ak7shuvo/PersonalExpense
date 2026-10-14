package com.ak7shuvo.personalexpense.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class InsightsTest {
    private fun tx(id: Long, type: TxType, amt: Long, label: String, y: Int, m: Int, day: Int = 5) =
        Txn(id, type, amt, label, LocalDate.of(y, m, day))

    @Test fun monthlyTotalsSpanSixMonthsAndIgnoreLoans() {
        val list = listOf(
            tx(1, TxType.INCOME, 1000, "Salary", 2026, 10),
            tx(2, TxType.EXPENSE, 400, "Food", 2026, 10),
            tx(3, TxType.EXPENSE, 100, "Food", 2026, 5),
            tx(4, TxType.LOAN, 9999, "Rahim", 2026, 10),
            tx(5, TxType.EXPENSE, 7, "Food", 2026, 4), // outside window
        )
        val r = Insights.monthlyTotals(list, YearMonth.of(2026, 10))
        assertEquals(6, r.size)
        assertEquals(YearMonth.of(2026, 5), r.first().month)
        assertEquals(100, r.first().expense)
        assertEquals(1000, r.last().income); assertEquals(400, r.last().expense)
    }

    @Test fun categoriesSortedAndFolded() {
        val list = (1..7).map { tx(it.toLong(), TxType.EXPENSE, it * 100L, "C$it", 2026, 10) } +
            tx(20, TxType.EXPENSE, 50, "C7", 2026, 9)
        val r = Insights.expenseByCategory(list, YearMonth.of(2026, 10))
        assertEquals(listOf("C7", "C6", "C5", "C4", "C3", "Other"), r.map { it.label })
        assertEquals(300, r.last().amount) // C2 + C1
    }
}
