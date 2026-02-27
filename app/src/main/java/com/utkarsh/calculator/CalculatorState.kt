package com.utkarsh.calculator

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalculatorState(
    val number1: String = "",
    val number2: String = "",
    val operation: CalculatorOperation? = null,
    val history: List<String> = emptyList(),
    val realTimeResult: String = "",
    val isHistoryVisible: Boolean = false,
    val isScientificExpanded: Boolean = false
) : Parcelable
