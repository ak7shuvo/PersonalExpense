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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ak7shuvo.personalexpense.domain.TxType
import com.ak7shuvo.personalexpense.domain.Txn
import com.ak7shuvo.personalexpense.ui.LedgerUi
import com.ak7shuvo.personalexpense.ui.components.EmptyState
import com.ak7shuvo.personalexpense.ui.components.TransactionRow
import com.ak7shuvo.personalexpense.ui.relativeLabel

private val filters: List<Pair<String, TxType?>> = listOf(
    "All" to null,
    "Income" to TxType.INCOME,
    "Expenses" to TxType.EXPENSE,
    "Lent" to TxType.LOAN,
    "Repayments" to TxType.REPAYMENT,
)

@Composable
fun ActivityScreen(ui: LedgerUi, onOpen: (Txn) -> Unit, modifier: Modifier = Modifier) {
    var selected by rememberSaveable { mutableStateOf(-1) } // index into filters; -1 = All
    val type = filters.getOrNull(selected)?.second
    val shown = if (type == null) ui.txns else ui.txns.filter { it.type == type }
    val grouped = shown.groupBy { it.date } // already newest first

    LazyColumn(modifier.fillMaxWidth(), contentPadding = screenPadding()) {
        item {
            Column {
                Text("History", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${ui.txns.size} record${if (ui.txns.size == 1) "" else "s"} · tap one to edit or delete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEachIndexed { i, (label, t) ->
                        FilterChip(
                            selected = (t == type),
                            onClick = { selected = if (t == null) -1 else i },
                            label = { Text(label) },
                            shape = MaterialTheme.shapes.large,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        if (shown.isEmpty()) {
            item {
                EmptyState(
                    title = if (ui.txns.isEmpty()) "No records yet" else "Nothing here",
                    body = if (ui.txns.isEmpty()) "Everything you add shows up here, newest first." else "No records match this filter.",
                )
            }
        }
        grouped.forEach { (date, rows) ->
            item(key = "h-$date") {
                Text(
                    date.relativeLabel().uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 18.dp, bottom = 4.dp),
                )
            }
            items(rows, key = { it.id }) { t ->
                TransactionRow(t, ui.title(t), showDate = false, onClick = { onOpen(t) }, modifier = Modifier.animateItem())
            }
        }
    }
}
