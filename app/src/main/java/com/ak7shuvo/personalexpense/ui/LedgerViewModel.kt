package com.ak7shuvo.personalexpense.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ak7shuvo.personalexpense.PersonalExpenseApp
import com.ak7shuvo.personalexpense.data.LedgerRepository
import com.ak7shuvo.personalexpense.data.SaveResult
import com.ak7shuvo.personalexpense.domain.CategoryShare
import com.ak7shuvo.personalexpense.domain.Insights
import com.ak7shuvo.personalexpense.domain.Ledger
import com.ak7shuvo.personalexpense.domain.LoanStatus
import com.ak7shuvo.personalexpense.domain.MonthTotal
import com.ak7shuvo.personalexpense.domain.Summary
import com.ak7shuvo.personalexpense.domain.Txn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class LedgerUi(
    val loaded: Boolean = false,
    val txns: List<Txn> = emptyList(),
    val summary: Summary = Summary(),
    val loans: List<LoanStatus> = emptyList(),
    val months: List<MonthTotal> = emptyList(),
    val categories: List<CategoryShare> = emptyList(),
) {
    fun txn(id: Long): Txn? = txns.firstOrNull { it.id == id }

    /** Borrower for a repayment (taken from its loan), else the record's own label. */
    fun title(t: Txn): String =
        if (t.loanId != null) loans.firstOrNull { it.loan.id == t.loanId }?.loan?.label ?: "Loan" else t.label
}

class LedgerViewModel(private val repo: LedgerRepository) : ViewModel() {

    val state: StateFlow<LedgerUi> = repo.transactions
        .map { txns ->
            val now = YearMonth.from(LocalDate.now())
            LedgerUi(
                loaded = true,
                txns = txns,
                summary = Ledger.summarize(txns),
                loans = Ledger.loanStatuses(txns),
                months = Insights.monthlyTotals(txns, now),
                categories = Insights.expenseByCategory(txns, now),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LedgerUi())

    fun save(draft: Txn, onResult: (SaveResult) -> Unit) {
        viewModelScope.launch { onResult(repo.save(draft)) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LedgerViewModel((this[APPLICATION_KEY] as PersonalExpenseApp).repository) }
        }
    }
}
