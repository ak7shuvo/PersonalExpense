package com.ak7shuvo.personalexpense.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ak7shuvo.personalexpense.domain.Ledger.Problem
import com.ak7shuvo.personalexpense.domain.Money
import com.ak7shuvo.personalexpense.domain.TxType
import com.ak7shuvo.personalexpense.ui.theme.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val shortFormat = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)

fun LocalDate.relativeLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> format(dayFormat)
}

fun LocalDate.shortLabel(): String = format(shortFormat)

val TxType.title: String
    get() = when (this) {
        TxType.INCOME -> "Income"
        TxType.EXPENSE -> "Expense"
        TxType.LOAN -> "Lent"
        TxType.REPAYMENT -> "Repayment"
    }

val TxType.glyph: String
    get() = when (this) {
        TxType.INCOME -> "↓"
        TxType.EXPENSE -> "↑"
        TxType.LOAN -> "→"
        TxType.REPAYMENT -> "←"
    }

/** Sign shown before the amount: effect on cash balance. */
val TxType.sign: String
    get() = when (this) {
        TxType.INCOME, TxType.REPAYMENT -> "+"
        TxType.EXPENSE, TxType.LOAN -> "−"
    }

@Composable
fun TxType.accent(): Color = when (this) {
    TxType.INCOME -> AppTheme.finance.income
    TxType.EXPENSE -> AppTheme.finance.expense
    TxType.LOAN, TxType.REPAYMENT -> AppTheme.finance.loan
}

fun signedMoney(type: TxType, amount: Long) = type.sign + Money.format(amount)

fun Problem.message(): String = when (this) {
    Problem.NonPositiveAmount -> "Enter an amount greater than zero."
    Problem.BlankLabel -> "This field can't be empty."
    Problem.NotFound -> "This record no longer exists."
    Problem.LoanMissing -> "Choose the loan being repaid."
    is Problem.ExceedsOutstanding -> "Only ${Money.format(outstanding)} is still owed on this loan."
    is Problem.BelowRepaid -> "${Money.format(repaid)} has already been repaid, so the loan can't be smaller."
}

val ExpenseCategories = listOf("Food", "Transport", "Bills", "Rent", "Shopping", "Health", "Education", "Other")
val IncomeSources = listOf("Salary", "Business", "Freelance", "Gift", "Other")
