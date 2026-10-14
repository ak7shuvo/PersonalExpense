package com.ak7shuvo.personalexpense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ak7shuvo.personalexpense.domain.LoanStatus
import com.ak7shuvo.personalexpense.domain.Money
import com.ak7shuvo.personalexpense.domain.Txn
import com.ak7shuvo.personalexpense.ui.LedgerUi
import com.ak7shuvo.personalexpense.ui.components.EmptyState
import com.ak7shuvo.personalexpense.ui.components.MoneyText
import com.ak7shuvo.personalexpense.ui.components.ProgressTrack
import com.ak7shuvo.personalexpense.ui.components.SectionHeader
import com.ak7shuvo.personalexpense.ui.components.SoftCard
import com.ak7shuvo.personalexpense.ui.relativeLabel
import com.ak7shuvo.personalexpense.ui.theme.AppTheme

@Composable
fun LoansScreen(
    ui: LedgerUi,
    onLend: () -> Unit,
    onEditLoan: (Txn) -> Unit,
    onRepay: (LoanStatus) -> Unit,
    onEditRepayment: (Txn) -> Unit,
    modifier: Modifier = Modifier,
) {
    val open = ui.loans.filter { !it.isSettled }
    val settled = ui.loans.filter { it.isSettled }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = screenPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Loans", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Money you lent, and what's come back.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onLend) { Text("Lend") }
            }
        }
        item {
            SoftCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    SectionHeader("Still owed to you")
                    Spacer(Modifier.height(8.dp))
                    MoneyText(ui.summary.outstanding, MaterialTheme.typography.displayMedium, AppTheme.finance.loan, animate = true)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${open.size} open · ${Money.format(ui.summary.repaid)} repaid of ${Money.format(ui.summary.lent)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (ui.loans.isEmpty()) {
            item {
                EmptyState(
                    title = "No loans",
                    body = "When you lend money to someone, record it here. Partial repayments are tracked until the balance reaches zero.",
                    action = "Record a loan",
                    onAction = onLend,
                )
            }
        }
        if (open.isNotEmpty()) {
            item { SectionHeader("Open") }
            items(open, key = { it.loan.id }) { LoanCard(it, onEditLoan, onRepay, onEditRepayment, Modifier.animateItem()) }
        }
        if (settled.isNotEmpty()) {
            item { SectionHeader("Settled") }
            items(settled, key = { it.loan.id }) { LoanCard(it, onEditLoan, onRepay, onEditRepayment, Modifier.animateItem()) }
        }
    }
}

@Composable
private fun LoanCard(
    status: LoanStatus,
    onEditLoan: (Txn) -> Unit,
    onRepay: (LoanStatus) -> Unit,
    onEditRepayment: (Txn) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loan = status.loan
    SoftCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { onEditLoan(loan) }, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(loan.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Lent ${loan.date.relativeLabel()}" + if (loan.note.isNotBlank()) " · ${loan.note}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(Money.format(loan.amount), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
            ProgressTrack(status.progress, if (status.isSettled) AppTheme.finance.income else AppTheme.finance.loan)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Repaid ${Money.format(status.repaid)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (status.isSettled) "Settled" else "${Money.format(status.outstanding)} left",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (status.isSettled) AppTheme.finance.income else AppTheme.finance.loan,
                )
            }
            if (status.repayments.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                status.repayments.forEach { r ->
                    Row(
                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { onEditRepayment(r) }.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(r.date.relativeLabel(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${Money.format(r.amount)}", style = MaterialTheme.typography.titleSmall, color = AppTheme.finance.loan)
                    }
                }
            }
            if (!status.isSettled) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onRepay(status) }, Modifier.fillMaxWidth()) { Text("Record repayment") }
            }
        }
    }
}
