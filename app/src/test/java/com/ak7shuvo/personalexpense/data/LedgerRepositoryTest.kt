package com.ak7shuvo.personalexpense.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ak7shuvo.personalexpense.domain.Ledger
import com.ak7shuvo.personalexpense.domain.Ledger.Problem
import com.ak7shuvo.personalexpense.domain.TxType
import com.ak7shuvo.personalexpense.domain.Txn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LedgerRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: LedgerRepository
    private val day = LocalDate.of(2026, 10, 1)

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        repo = LedgerRepository(db)
    }

    @After fun tearDown() { db.close() }

    private fun all() = runBlocking { repo.transactions.first() }
    private fun save(t: Txn) = runBlocking { repo.save(t) }
    private fun income(amt: Long, id: Long = 0) = Txn(id, TxType.INCOME, amt, "Salary", day)
    private fun lend(amt: Long, id: Long = 0) = Txn(id, TxType.LOAN, amt, "Rahim", day)
    private fun repay(loanId: Long, amt: Long, id: Long = 0) = Txn(id, TxType.REPAYMENT, amt, "", day, loanId = loanId)

    @Test fun insertThenReadBack() {
        assertEquals(SaveResult.Saved, save(income(100_050)))
        val rows = all()
        assertEquals(1, rows.size)
        assertEquals(100_050L, rows[0].amount)
        assertEquals(day, rows[0].date)
    }

    @Test fun editUpdatesRow() {
        save(income(100_000))
        val id = all().single().id
        assertEquals(SaveResult.Saved, save(income(250_000, id).copy(note = " bonus ")))
        val row = all().single()
        assertEquals(250_000L, row.amount)
        assertEquals("bonus", row.note)
    }

    @Test fun deleteRemovesRow() {
        save(income(100_000))
        runBlocking { repo.delete(all().single().id) }
        assertTrue(all().isEmpty())
    }

    @Test fun deletingLoanCascadesToRepayments() {
        save(income(500_000))
        save(lend(100_000))
        val loanId = all().first { it.type == TxType.LOAN }.id
        save(repay(loanId, 40_000))
        save(repay(loanId, 10_000))
        runBlocking { repo.delete(loanId) }
        assertEquals(listOf(TxType.INCOME), all().map { it.type })
    }

    @Test fun partialRepaymentsAndOverpaymentRejected() {
        save(lend(100_000))
        val loanId = all().single().id
        assertEquals(SaveResult.Saved, save(repay(loanId, 60_000)))
        assertEquals(SaveResult.Invalid(Problem.ExceedsOutstanding(40_000)), save(repay(loanId, 40_001)))
        assertEquals(SaveResult.Saved, save(repay(loanId, 40_000)))
        val s = Ledger.summarize(all())
        assertEquals(0L, s.outstanding)
        assertEquals(0L, s.balance)
    }

    @Test fun invalidEditLeavesDataUntouched() {
        save(lend(100_000))
        val loanId = all().single().id
        save(repay(loanId, 30_000))
        assertEquals(SaveResult.Invalid(Problem.BelowRepaid(30_000)), save(lend(20_000, loanId)))
        assertEquals(100_000L, all().first { it.type == TxType.LOAN }.amount)
    }

    @Test fun dataSurvivesReopeningTheDatabaseFile() {
        val name = "persist-test.db"
        context.deleteDatabase(name)
        fun open() = Room.databaseBuilder(context, AppDatabase::class.java, name).allowMainThreadQueries().build()

        val first = open()
        runBlocking { LedgerRepository(first).save(income(123_456)) }
        first.close()

        val second = open()
        try {
            val rows = runBlocking { LedgerRepository(second).transactions.first() }
            assertEquals(listOf(123_456L), rows.map { it.amount })
        } finally {
            second.close()
            context.deleteDatabase(name)
        }
    }
}
