package com.ak7shuvo.personalexpense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ak7shuvo.personalexpense.domain.Summary
import com.ak7shuvo.personalexpense.domain.TxType
import com.ak7shuvo.personalexpense.domain.Txn
import com.ak7shuvo.personalexpense.ui.LedgerUi
import com.ak7shuvo.personalexpense.ui.components.CategoryBreakdown
import com.ak7shuvo.personalexpense.ui.components.EmptyState
import com.ak7shuvo.personalexpense.ui.components.MoneyText
import com.ak7shuvo.personalexpense.ui.components.MonthlyBars
import com.ak7shuvo.personalexpense.ui.components.ProgressTrack
import com.ak7shuvo.personalexpense.ui.components.SectionHeader
import com.ak7shuvo.personalexpense.ui.components.SoftCard
import com.ak7shuvo.personalexpense.ui.components.TransactionRow
import com.ak7shuvo.personalexpense.ui.components.TypeBadge
import com.ak7shuvo.personalexpense.ui.theme.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Top padding that clears the status bar (the tab screens draw edge to edge). */
@Composable
fun screenPadding(bottom: androidx.compose.ui.unit.Dp = 0.dp): PaddingValues =
    PaddingValues(
        start = 20.dp,
        end = 20.dp,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
        bottom = bottom + 32.dp,
    )

private val dateHeader = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)

@Composable
fun OverviewScreen(
    ui: LedgerUi,
    onAdd: (TxType) -> Unit,
    onOpen: (Txn) -> Unit,
    onSeeActivity: () -> Unit,
    onSeeLoans: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxWidth(), contentPadding = screenPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column {
                Text(LocalDate.now().format(dateHeader).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("Overview", style = MaterialTheme.typography.headlineMedium)
            }
        }
        item { HeroCard(ui.summary) }
        item { QuickActions(onAdd) }
        item { LoansCard(ui.summary, onSeeLoans) }

        if (ui.months.any { it.income > 0 || it.expense > 0 }) {
            item {
                SoftCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        SectionHeader("Last 6 months")
                        Spacer(Modifier.height(12.dp))
                        MonthlyBars(ui.months)
                    }
                }
            }
        }
        if (ui.categories.isNotEmpty()) {
            item {
                SoftCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        SectionHeader("Spending this month")
                        Spacer(Modifier.height(14.dp))
                        CategoryBreakdown(ui.categories)
                    }
                }
            }
        }

        item { SectionHeader("Recent activity", action = if (ui.txns.isEmpty()) null else "See all", onAction = onSeeActivity) }
        if (ui.txns.isEmpty()) {
            item {
                SoftCard(Modifier.fillMaxWidth()) {
                    EmptyState(
                        title = "A clean slate",
                        body = "Record your first income or expense and your balance will appear here.",
                        action = "Add income",
                        onAction = { onAdd(TxType.INCOME) },
                    )
                }
            }
        } else {
            item {
                SoftCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        ui.txns.take(5).forEachIndexed { i, t ->
                            if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 60.dp))
                            TransactionRow(t, ui.title(t), showDate = true, onClick = { onOpen(t) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(summary: Summary) {
    val f = AppTheme.finance
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).background(f.hero).padding(24.dp),
    ) {
        Text("CURRENT BALANCE", style = MaterialTheme.typography.labelSmall, color = f.onHeroMuted)
        Spacer(Modifier.height(10.dp))
        MoneyText(summary.balance, MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp, lineHeight = 46.sp), f.onHero, animate = true)
        Spacer(Modifier.height(22.dp))
        HorizontalDivider(color = f.onHero.copy(alpha = 0.12f))
        Spacer(Modifier.height(16.dp))
        Row {
            HeroStat("Income", summary.income, f.heroIncome, "↓", Modifier.weight(1f))
            HeroStat("Expenses", summary.expenses, f.heroExpense, "↑", Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroStat(label: String, amount: Long, accent: androidx.compose.ui.graphics.Color, arrow: String, modifier: Modifier) {
    val f = AppTheme.finance
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(arrow, color = accent, style = MaterialTheme.typography.labelLarge)
            Text("  $label", color = f.onHeroMuted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(4.dp))
        MoneyText(amount, MaterialTheme.typography.titleMedium, f.onHero, fractionScale = 0.8f)
    }
}

@Composable
private fun QuickActions(onAdd: (TxType) -> Unit) {
    val actions = listOf(TxType.INCOME to "Income", TxType.EXPENSE to "Expense", TxType.LOAN to "Lend", TxType.REPAYMENT to "Repay")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        actions.forEach { (type, label) ->
            Column(
                Modifier.weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                    .clickable { onAdd(type) }
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TypeBadge(type)
                Spacer(Modifier.height(8.dp))
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LoansCard(summary: Summary, onClick: () -> Unit) {
    val loanColor = AppTheme.finance.loan
    SoftCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(20.dp)) {
            SectionHeader("Money lent")
            Spacer(Modifier.height(14.dp))
            if (summary.lent == 0L) {
                Text(
                    "Nothing lent out. Lending is tracked here and never counted as an expense.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row {
                    LoanStat("Lent", summary.lent, Modifier.weight(1f))
                    LoanStat("Repaid", summary.repaid, Modifier.weight(1f))
                    LoanStat("Outstanding", summary.outstanding, Modifier.weight(1f), emphasise = true)
                }
                Spacer(Modifier.height(16.dp))
                ProgressTrack((summary.repaid.toDouble() / summary.lent).toFloat(), loanColor)
            }
        }
    }
}

@Composable
private fun LoanStat(label: String, amount: Long, modifier: Modifier, emphasise: Boolean = false) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        MoneyText(
            amount,
            MaterialTheme.typography.titleMedium,
            if (emphasise) AppTheme.finance.loan else MaterialTheme.colorScheme.onSurface,
            showFraction = false,
            fractionScale = 0.8f,
        )
    }
}

