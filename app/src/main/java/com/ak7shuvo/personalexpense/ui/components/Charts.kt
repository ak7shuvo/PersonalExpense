package com.ak7shuvo.personalexpense.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ak7shuvo.personalexpense.domain.CategoryShare
import com.ak7shuvo.personalexpense.domain.Money
import com.ak7shuvo.personalexpense.domain.MonthTotal
import com.ak7shuvo.personalexpense.ui.theme.AppTheme
import java.time.format.TextStyle
import java.util.Locale

/** Income vs expense per month. Bars grow in on first show and whenever the data changes. */
@Composable
fun MonthlyBars(months: List<MonthTotal>, modifier: Modifier = Modifier) {
    val finance = AppTheme.finance
    val grid = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val max = (months.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L)
    val grow = remember { Animatable(0f) }
    LaunchedEffect(months) {
        grow.snapTo(0f)
        grow.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${Money.SYMBOL}${Money.compact(max)}", style = MaterialTheme.typography.labelMedium, color = labelColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(finance.income); Spacer(Modifier.width(6.dp))
                Text("Income", style = MaterialTheme.typography.labelMedium, color = labelColor)
                Spacer(Modifier.width(14.dp))
                Dot(finance.expense); Spacer(Modifier.width(6.dp))
                Text("Expenses", style = MaterialTheme.typography.labelMedium, color = labelColor)
            }
        }
        Spacer(Modifier.height(10.dp))
        Canvas(Modifier.fillMaxWidth().height(132.dp)) {
            val h = size.height
            val slot = size.width / months.size
            val bar = (slot * 0.26f).coerceAtMost(18.dp.toPx())
            val gap = 4.dp.toPx()
            val radius = CornerRadius(bar / 2.2f)
            listOf(0f, 0.5f, 1f).forEach { f ->
                val y = h - h * f
                drawLine(grid, Offset(0f, y.coerceAtMost(h - 0.5f)), Offset(size.width, y.coerceAtMost(h - 0.5f)), strokeWidth = 1.dp.toPx())
            }
            months.forEachIndexed { i, m ->
                val cx = slot * i + slot / 2
                fun drawBar(value: Long, color: Color, left: Float) {
                    val bh = (h * (value.toDouble() / max).toFloat() * grow.value).coerceAtLeast(if (value > 0) 3.dp.toPx() else 0f)
                    if (bh > 0f) drawRoundRect(color, Offset(left, h - bh), Size(bar, bh), radius)
                }
                drawBar(m.income, finance.income, cx - bar - gap / 2)
                drawBar(m.expense, finance.expense, cx + gap / 2)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            months.forEach { m ->
                Text(
                    m.month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

/** Stacked share bar plus a legend row per category. */
@Composable
fun CategoryBreakdown(shares: List<CategoryShare>, modifier: Modifier = Modifier) {
    val palette = AppTheme.finance.chartPalette
    val total = shares.sumOf { it.amount }.coerceAtLeast(1L)
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(shares) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(12.dp)) {
            val gap = 3.dp.toPx()
            val usable = size.width - gap * (shares.size - 1).coerceAtLeast(0)
            var x = 0f
            shares.forEachIndexed { i, s ->
                val w = usable * (s.amount.toDouble() / total).toFloat() * reveal.value
                if (w > 0f) drawRoundRect(palette[i % palette.size], Offset(x, 0f), Size(w, size.height), CornerRadius(size.height / 2))
                x += w + gap * reveal.value
            }
        }
        Spacer(Modifier.height(14.dp))
        shares.forEachIndexed { i, s ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Dot(palette[i % palette.size])
                Text(s.label, Modifier.weight(1f).padding(start = 10.dp), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${(s.amount * 100 / total)}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Money.format(s.amount), Modifier.width(112.dp), style = MaterialTheme.typography.titleSmall, textAlign = androidx.compose.ui.text.style.TextAlign.End, maxLines = 1)
            }
        }
    }
}

/** Slim determinate progress track used for loan repayment. */
@Composable
fun ProgressTrack(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val anim = remember { Animatable(0f) }
    LaunchedEffect(progress) { anim.animateTo(progress, tween(700, easing = FastOutSlowInEasing)) }
    Canvas(modifier.fillMaxWidth().height(8.dp)) {
        drawRoundRect(track, Offset.Zero, size, CornerRadius(size.height / 2))
        if (anim.value > 0f) drawRoundRect(color, Offset.Zero, Size(size.width * anim.value, size.height), CornerRadius(size.height / 2))
    }
}
