package com.ak7shuvo.personalexpense.data

import androidx.room.withTransaction
import com.ak7shuvo.personalexpense.domain.Ledger
import com.ak7shuvo.personalexpense.domain.Txn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface SaveResult {
    data object Saved : SaveResult
    data class Invalid(val problem: Ledger.Problem) : SaveResult
}

class LedgerRepository(private val db: AppDatabase) {
    private val dao = db.transactions()

    /** All records, newest first. */
    val transactions: Flow<List<Txn>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    /** Validates against the stored ledger and inserts (id == 0) or updates, atomically. */
    suspend fun save(draft: Txn): SaveResult = db.withTransaction {
        val existing = dao.getAll().map { it.toDomain() }
        val problem = Ledger.validate(draft, existing)
        if (problem != null) return@withTransaction SaveResult.Invalid(problem)
        if (draft.id == 0L) dao.insert(draft.toEntity()) else dao.update(draft.toEntity())
        SaveResult.Saved
    }

    /** Deleting a loan also deletes its repayments (foreign-key cascade). */
    suspend fun delete(id: Long) = dao.delete(id)
}
