package com.ak7shuvo.personalexpense.domain

import com.ak7shuvo.personalexpense.domain.Ledger.Problem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LedgerTest {
    private val d = LocalDate.of(2026, 10, 1)
    private fun income(id: Long, amt: Long) = Txn(id, TxType.INCOME, amt, "Salary", d)
    private fun expense(id: Long, amt: Long, cat: String = "Food") = Txn(id, TxType.EXPENSE, amt, cat, d)
    private fun loan(id: Long, amt: Long, who: String = "Rahim") = Txn(id, TxType.LOAN, amt, who, d)
    private fun repay(id: Long, loanId: Long, amt: Long) = Txn(id, TxType.REPAYMENT, amt, "", d, loanId = loanId)

    @Test fun emptyLedgerIsZero() {
        val s = Ledger.summarize(emptyList())
        assertEquals(0, s.balance); assertEquals(0, s.outstanding)
    }

    @Test fun incomeAndExpenseDriveBalance() {
        val s = Ledger.summarize(listOf(income(1, 100_000), expense(2, 30_050)))
        assertEquals(100_000, s.income); assertEquals(30_050, s.expenses); assertEquals(69_950, s.balance)
    }

    @Test fun lendingIsNotAnExpense() {
        val s = Ledger.summarize(listOf(income(1, 100_000), loan(2, 40_000)))
        assertEquals(0, s.expenses)
        assertEquals(60_000, s.balance)
        assertEquals(40_000, s.outstanding)
    }

    @Test fun repaymentIsNotIncome() {
        val s = Ledger.summarize(listOf(income(1, 100_000), loan(2, 40_000), repay(3, 2, 15_000)))
        assertEquals(100_000, s.income)
        assertEquals(75_000, s.balance)
        assertEquals(25_000, s.outstanding)
        assertEquals(15_000, s.repaid)
    }

    @Test fun lendAndFullRepayLeavesWealthUnchanged() {
        val before = Ledger.summarize(listOf(income(1, 100_000))).balance
        val after = Ledger.summarize(listOf(income(1, 100_000), loan(2, 40_000), repay(3, 2, 15_000), repay(4, 2, 25_000)))
        assertEquals(before, after.balance)
        assertEquals(0, after.outstanding)
    }

    @Test fun wealthIsBalancePlusOutstandingAndIgnoresLending() {
        val s = Ledger.summarize(listOf(income(1, 100_000), expense(2, 10_000), loan(3, 40_000), repay(4, 3, 10_000)))
        assertEquals(s.income - s.expenses, s.balance + s.outstanding)
    }

    @Test fun partialRepaymentsTrackedPerLoan() {
        val txns = listOf(loan(1, 50_000), loan(2, 20_000, "Karim"), repay(3, 1, 10_000), repay(4, 1, 5_000))
        val st = Ledger.loanStatuses(txns).associateBy { it.loan.id }
        val a = st.getValue(1L); val b = st.getValue(2L)
        assertEquals(1L, a.loan.id); assertEquals(15_000, a.repaid); assertEquals(35_000, a.outstanding)
        assertEquals(2L, b.loan.id); assertEquals(20_000, b.outstanding)
        assertEquals(0.3f, a.progress, 0.0001f)
    }

    @Test fun settledLoansSortAfterOpenOnes() {
        val txns = listOf(loan(1, 10_000), repay(2, 1, 10_000), loan(3, 5_000, "Karim"))
        val st = Ledger.loanStatuses(txns)
        assertEquals(listOf(3L, 1L), st.map { it.loan.id })
        assertTrue(st[1].isSettled)
    }

    @Test fun editingTxnChangesTotals() {
        val list = listOf(income(1, 100_000), expense(2, 30_000))
        val edited = list.map { if (it.id == 2L) it.copy(amount = 45_000) else it }
        assertEquals(55_000, Ledger.summarize(edited).balance)
    }

    @Test fun deletingTxnChangesTotals() {
        val list = listOf(income(1, 100_000), expense(2, 30_000))
        assertEquals(100_000, Ledger.summarize(list.filterNot { it.id == 2L }).balance)
    }

    @Test fun deletingLoanRemovesItsRepaymentsFromTotals() {
        // Mirrors the DB cascade: removing a loan also removes its repayments.
        val list = listOf(income(1, 100_000), loan(2, 40_000), repay(3, 2, 10_000))
        val after = list.filterNot { it.id == 2L || it.loanId == 2L }
        assertEquals(100_000, Ledger.summarize(after).balance)
    }

    @Test fun rejectsZeroAndBlank() {
        assertEquals(Problem.NonPositiveAmount, Ledger.validate(expense(0, 0), emptyList()))
        assertEquals(Problem.BlankLabel, Ledger.validate(expense(0, 100, " "), emptyList()))
    }

    @Test fun repaymentCannotExceedOutstanding() {
        val existing = listOf(loan(1, 50_000), repay(2, 1, 30_000))
        assertEquals(Problem.ExceedsOutstanding(20_000), Ledger.validate(repay(0, 1, 20_001), existing))
        assertNull(Ledger.validate(repay(0, 1, 20_000), existing))
    }

    @Test fun editingRepaymentIgnoresItself() {
        val existing = listOf(loan(1, 50_000), repay(2, 1, 30_000))
        assertNull(Ledger.validate(repay(2, 1, 50_000), existing))
        assertEquals(Problem.ExceedsOutstanding(50_000), Ledger.validate(repay(2, 1, 50_001), existing))
    }

    @Test fun repaymentNeedsRealLoan() {
        assertEquals(Problem.LoanMissing, Ledger.validate(repay(0, 9, 100), listOf(income(1, 500))))
        assertEquals(Problem.LoanMissing, Ledger.validate(repay(0, 1, 100), listOf(income(1, 500))))
    }

    @Test fun loanCannotDropBelowRepaid() {
        val existing = listOf(loan(1, 50_000), repay(2, 1, 30_000))
        assertEquals(Problem.BelowRepaid(30_000), Ledger.validate(loan(1, 29_999), existing))
        assertNull(Ledger.validate(loan(1, 30_000), existing))
    }

    @Test fun editUnknownIdIsNotFound() {
        assertEquals(Problem.NotFound, Ledger.validate(expense(7, 100), emptyList()))
    }
}
