package com.ak7shuvo.personalexpense.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.ak7shuvo.personalexpense.domain.Money

/** Amount with a muted symbol and smaller fraction; counts smoothly to new values. */
@Composable
fun MoneyText(
    minor: Long,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    animate: Boolean = false,
    fractionScale: Float = 0.62f,
    showFraction: Boolean = true,
) {
    var from by remember { mutableLongStateOf(minor) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(minor) {
        if (animate && from != minor) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        from = minor
    }
    val shown = if (animate && progress.value < 1f) from + ((minor - from) * progress.value.toDouble()).toLong() else minor

    val small: TextUnit = style.fontSize * fractionScale
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = (if (shown < 0) "-" else "") + Money.SYMBOL,
            style = style.copy(fontSize = small),
            color = color.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 2.dp, bottom = 2.dp),
        )
        Text(Money.groupWhole(shown), style = style, color = color, maxLines = 1, overflow = TextOverflow.Clip)
        if (showFraction) {
            Text(
                Money.fraction(shown),
                style = style.copy(fontSize = small),
                color = color.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
    }
}

