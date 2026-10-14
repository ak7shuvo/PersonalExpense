package com.ak7shuvo.personalexpense.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ak7shuvo.personalexpense.domain.TxType
import com.ak7shuvo.personalexpense.domain.Txn
import java.time.LocalDate

/** Single ledger table. Repayments reference their loan; deleting a loan cascades to its repayments. */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("loanId"), Index("epochDay")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amountMinor: Long,
    val label: String,
    val epochDay: Long,
    val note: String,
    val loanId: Long?,
)

fun TransactionEntity.toDomain() = Txn(
    id = id,
    type = TxType.valueOf(type),
    amount = amountMinor,
    label = label,
    date = LocalDate.ofEpochDay(epochDay),
    note = note,
    loanId = loanId,
)

fun Txn.toEntity() = TransactionEntity(
    id = id,
    type = type.name,
    amountMinor = amount,
    label = label.trim(),
    epochDay = date.toEpochDay(),
    note = note.trim(),
    loanId = if (type == TxType.REPAYMENT) loanId else null,
)
