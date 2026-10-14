package com.ak7shuvo.personalexpense.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ak7shuvo.personalexpense.data.SaveResult
import com.ak7shuvo.personalexpense.domain.Ledger
import com.ak7shuvo.personalexpense.domain.LoanStatus
import com.ak7shuvo.personalexpense.domain.Money
import com.ak7shuvo.personalexpense.domain.TxType
import com.ak7shuvo.personalexpense.domain.Txn
import com.ak7shuvo.personalexpense.ui.ExpenseCategories
import com.ak7shuvo.personalexpense.ui.IncomeSources
import com.ak7shuvo.personalexpense.ui.LedgerUi
import com.ak7shuvo.personalexpense.ui.accent
import com.ak7shuvo.personalexpense.ui.components.SectionHeader
import com.ak7shuvo.personalexpense.ui.message
import com.ak7shuvo.personalexpense.ui.relativeLabel
import com.ak7shuvo.personalexpense.ui.theme.AppTheme
import java.time.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

private val entryTypes = listOf(TxType.INCOME to "Income", TxType.EXPENSE to "Expense", TxType.LOAN to "Lend", TxType.REPAYMENT to "Repay")

/**
 * Add or edit any record. [editId] == 0 means new. For a new repayment, [presetLoanId] pre-selects the loan.
 */
@Composable
fun EntryScreen(
    ui: LedgerUi,
    initialType: TxType,
    editId: Long,
    presetLoanId: Long,
    onSave: (Txn, (SaveResult) -> Unit) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val existing = if (editId != 0L) ui.txn(editId) else null
    val missing = editId != 0L && ui.loaded && existing == null
    LaunchedEffect(missing) { if (missing) onBack() } // deleted (or never existed): leave once
    if (editId != 0L && existing == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }
    EntryForm(ui, initialType, existing, presetLoanId, onSave, onDelete, onBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EntryForm(
    ui: LedgerUi,
    initialType: TxType,
    existing: Txn?,
    presetLoanId: Long,
    onSave: (Txn, (SaveResult) -> Unit) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val editing = existing != null
    var typeName by rememberSaveable { mutableStateOf((existing?.type ?: initialType).name) }
    var amountText by rememberSaveable { mutableStateOf(existing?.let { Money.toInput(it.amount) } ?: "") }
    var label by rememberSaveable { mutableStateOf(existing?.takeIf { it.type != TxType.REPAYMENT }?.label ?: "") }
    var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var dateDay by rememberSaveable { mutableStateOf(existing?.date?.toEpochDay() ?: LocalDate.now().toEpochDay()) }
    var loanChoice by rememberSaveable { mutableStateOf(existing?.loanId ?: presetLoanId) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val type = TxType.valueOf(typeName)
    val accent = type.accent()
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (!editing) runCatching { focus.requestFocus() } }

    // Loans a repayment can target, with how much each can still absorb (this repayment's own amount is released when editing).
    val repayOptions: List<Pair<LoanStatus, Long>> = ui.loans.mapNotNull { s ->
        val own = if (existing != null && existing.loanId == s.loan.id) existing.amount else 0L
        val room = s.outstanding + own
        if (room > 0 || s.loan.id == existing?.loanId) s to room else null
    }
    val selectedLoanId: Long? = loanChoice.takeIf { id -> repayOptions.any { it.first.loan.id == id } }
        ?: repayOptions.singleOrNull()?.first?.loan?.id
    val selectedRoom = repayOptions.firstOrNull { it.first.loan.id == selectedLoanId }?.second

    fun submit() {
        val minor = Money.parse(amountText)
        if (minor == null || minor <= 0L) { error = "Enter a valid amount, up to two decimals."; return }
        if (type == TxType.REPAYMENT) {
            if (selectedLoanId == null) { error = Ledger.Problem.LoanMissing.message(); return }
        } else if (label.isBlank()) {
            error = when (type) {
                TxType.INCOME -> "Add where this income came from."
                TxType.EXPENSE -> "Choose or type a category."
                else -> "Add who you lent to."
            }
            return
        }
        val loanLabel = ui.loans.firstOrNull { it.loan.id == selectedLoanId }?.loan?.label.orEmpty()
        val draft = Txn(
            id = existing?.id ?: 0L,
            type = type,
            amount = minor,
            label = if (type == TxType.REPAYMENT) loanLabel else label.trim(),
            date = LocalDate.ofEpochDay(dateDay),
            note = note.trim(),
            loanId = if (type == TxType.REPAYMENT) selectedLoanId else null,
        )
        saving = true
        onSave(draft) { result ->
            saving = false
            when (result) {
                SaveResult.Saved -> onBack()
                is SaveResult.Invalid -> error = result.problem.message()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().imePadding()) {
        // Header
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text(
                when (type) {
                    TxType.INCOME -> if (editing) "Edit income" else "New income"
                    TxType.EXPENSE -> if (editing) "Edit expense" else "New expense"
                    TxType.LOAN -> if (editing) "Edit loan" else "Lend money"
                    TxType.REPAYMENT -> if (editing) "Edit repayment" else "Record repayment"
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            if (editing) {
                IconButton(onClick = { showDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            if (!editing) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    entryTypes.forEachIndexed { i, (t, name) ->
                        SegmentedButton(
                            selected = t == type,
                            onClick = { if (t != type) { typeName = t.name; label = ""; error = null } },
                            shape = SegmentedButtonDefaults.itemShape(i, entryTypes.size),
                            icon = {},
                            label = { Text(name, maxLines = 1) },
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Amount
            Column(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large).padding(20.dp),
            ) {
                SectionHeader("Amount")
                val amountStyle = MaterialTheme.typography.displayMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Money.SYMBOL, style = amountStyle, color = accent, modifier = Modifier.padding(end = 8.dp))
                    BasicTextField(
                        value = amountText,
                        onValueChange = { amountText = Money.sanitizeInput(it); error = null },
                        textStyle = amountStyle,
                        singleLine = true,
                        cursorBrush = SolidColor(accent),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox = { inner ->
                            Box {
                                if (amountText.isEmpty()) Text("0", style = amountStyle, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                                inner()
                            }
                        },
                        modifier = Modifier.weight(1f).focusRequester(focus),
                    )
                }
                if (type == TxType.REPAYMENT && selectedRoom != null) {
                    TextButton(onClick = { amountText = Money.toInput(selectedRoom); error = null }) {
                        Text("Pay in full · ${Money.format(selectedRoom)}")
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            if (type == TxType.REPAYMENT) {
                SectionHeader("Repaying")
                Spacer(Modifier.height(8.dp))
                if (repayOptions.isEmpty()) {
                    Text(
                        "There are no open loans. Record a loan first, then come back to log repayments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                repayOptions.forEach { (status, room) ->
                    val chosen = status.loan.id == selectedLoanId
                    val lockedToThis = editing
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(MaterialTheme.shapes.medium)
                            .border(BorderStroke(if (chosen) 1.5.dp else 1.dp, if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant), MaterialTheme.shapes.medium)
                            .background(if (chosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface)
                            .clickable(enabled = !lockedToThis) { loanChoice = status.loan.id; error = null }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = chosen, onClick = null, enabled = !lockedToThis || chosen)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(status.loan.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Lent ${status.loan.date.relativeLabel()} · ${Money.format(status.loan.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text("${Money.format(room)} left", style = MaterialTheme.typography.labelLarge, color = AppTheme.finance.loan)
                    }
                }
            } else {
                SectionHeader(
                    when (type) {
                        TxType.INCOME -> "Source"
                        TxType.EXPENSE -> "Category"
                        else -> "Lent to"
                    },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it.take(40); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    placeholder = {
                        Text(
                            when (type) {
                                TxType.INCOME -> "e.g. Salary"
                                TxType.EXPENSE -> "e.g. Groceries"
                                else -> "Person's name"
                            },
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                val suggestions = when (type) {
                    TxType.INCOME -> IncomeSources
                    TxType.EXPENSE -> ExpenseCategories
                    else -> ui.loans.map { it.loan.label }.distinct().take(8)
                }
                if (suggestions.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.forEach { s ->
                            FilterChip(
                                selected = label.trim().equals(s, ignoreCase = true),
                                onClick = { label = s; error = null },
                                label = { Text(s) },
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            // Date
            SectionHeader("Date")
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), MaterialTheme.shapes.medium)
                    .clickable { showDate = true }.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(LocalDate.ofEpochDay(dateDay).relativeLabel(), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Filled.DateRange, contentDescription = "Pick date", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(20.dp))

            SectionHeader("Note (optional)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("Add a detail you'll want later") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            Spacer(Modifier.height(24.dp))
        }

        // Save bar
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            Button(
                onClick = ::submit,
                enabled = !saving && !(type == TxType.REPAYMENT && repayOptions.isEmpty()),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    if (editing) "Save changes" else when (type) {
                        TxType.INCOME -> "Add income"
                        TxType.EXPENSE -> "Add expense"
                        TxType.LOAN -> "Record loan"
                        TxType.REPAYMENT -> "Record repayment"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateDay * MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dateDay = Math.floorDiv(it, MILLIS_PER_DAY) }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }

    if (showDelete && existing != null) {
        val cascade = if (existing.type == TxType.LOAN) ui.loans.firstOrNull { it.loan.id == existing.id }?.repayments?.size ?: 0 else 0
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete this record?") },
            text = {
                Text(
                    buildString {
                        append("${existing.type.name.lowercase().replaceFirstChar { it.uppercase() }} of ${Money.format(existing.amount)} will be removed and your totals recalculated.")
                        if (cascade > 0) append(" This also deletes its $cascade repayment${if (cascade == 1) "" else "s"}.")
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { showDelete = false; onDelete(existing.id) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Keep") } },
        )
    }
}
