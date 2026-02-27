package com.utkarsh.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier,
    color: Color = Color.White,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier // Applying the passed-in modifier first (which contains the clip)
            .semantics { contentDescription = symbol }
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    ) {
        Text(
            text = symbol,
            style = textStyle,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
