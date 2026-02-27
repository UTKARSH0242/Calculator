package com.utkarsh.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import java.util.Locale
import kotlin.math.*

class CalculatorViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    @OptIn(SavedStateHandleSaveableApi::class)
    var state by mutableStateOf(
        savedStateHandle.saveable(key = "calculator_state") {
            CalculatorState()
        }
    )
        private set

    fun onAction(action: CalculatorActions) {
        when(action) {
            is CalculatorActions.Number -> enterNumber(action.number)
            is CalculatorActions.Decimal -> enterDecimal()
            is CalculatorActions.Clear -> state = state.copy(
                number1 = "",
                number2 = "",
                operation = null,
                realTimeResult = ""
            )
            is CalculatorActions.Operation -> enterOperation(action.operation)
            is CalculatorActions.Calculate -> performCalculation()
            is CalculatorActions.Delete -> performDeletion()
            is CalculatorActions.UseHistory -> {
                state = state.copy(
                    number1 = action.value,
                    number2 = "",
                    operation = null,
                    realTimeResult = "",
                    isHistoryVisible = false
                )
            }
            is CalculatorActions.ClearHistory -> {
                state = state.copy(history = emptyList())
            }
            is CalculatorActions.ToggleHistory -> {
                state = state.copy(isHistoryVisible = !state.isHistoryVisible)
            }
            is CalculatorActions.ToggleScientific -> {
                state = state.copy(isScientificExpanded = !state.isScientificExpanded)
            }
        }
        updateRealTimeResult()
    }

    private fun performDeletion() {
        when {
            state.number2.isNotBlank() -> state = state.copy(
                number2 = state.number2.dropLast(1)
            )
            state.operation != null -> state = state.copy(
                operation = null
            )
            state.number1.isNotBlank() -> state = state.copy(
                number1 = state.number1.dropLast(1)
            )
        }
    }

    private fun performCalculation() {
        val number1 = state.number1.toDoubleOrNull()
        val number2 = state.number2.toDoubleOrNull()
        if(number1 != null && number2 != null && state.operation != null) {
            val result = calculateResult(number1, number2, state.operation!!)
            
            val formattedResult = formatResult(result)
            val historyEntry = "${state.number1} ${state.operation!!.symbol} ${state.number2} = $formattedResult"
            
            if (formattedResult != "Error") {
                addToHistory(historyEntry)
                state = state.copy(
                    number1 = formattedResult,
                    number2 = "",
                    operation = null,
                    realTimeResult = ""
                )
            } else {
                state = state.copy(number1 = "Error", number2 = "", operation = null)
            }
        }
    }

    private fun calculateResult(n1: Double, n2: Double, op: CalculatorOperation): Double {
        return when(op) {
            is CalculatorOperation.Add -> n1 + n2
            is CalculatorOperation.Subtract -> n1 - n2
            is CalculatorOperation.Multiply -> n1 * n2
            is CalculatorOperation.Divide -> if (n2 != 0.0) n1 / n2 else Double.NaN
            is CalculatorOperation.Percentage -> (n1 * n2) / 100.0
            is CalculatorOperation.Power -> n1.pow(n2)
            else -> Double.NaN
        }
    }

    private fun updateRealTimeResult() {
        val number1 = state.number1.toDoubleOrNull()
        val number2 = state.number2.toDoubleOrNull()
        if (number1 != null && number2 != null && state.operation != null) {
            val result = calculateResult(number1, number2, state.operation!!)
            if (!result.isNaN() && result.isFinite()) {
                state = state.copy(realTimeResult = "= " + formatResult(result))
            } else {
                state = state.copy(realTimeResult = "")
            }
        } else {
            state = state.copy(realTimeResult = "")
        }
    }

    private fun enterOperation(operation: CalculatorOperation) {
        if(state.number1.isNotBlank() && state.number1 != "Error") {
            when(operation) {
                is CalculatorOperation.SquareRoot,
                is CalculatorOperation.Square,
                is CalculatorOperation.Logarithm,
                is CalculatorOperation.Factorial,
                is CalculatorOperation.Sin,
                is CalculatorOperation.Cos,
                is CalculatorOperation.Tan -> performUnaryOperation(operation)
                is CalculatorOperation.Exp,
                is CalculatorOperation.Pi -> {
                   val value = if (operation is CalculatorOperation.Pi) PI else E
                   if (state.operation == null) {
                       state = state.copy(number1 = formatResult(value))
                   } else {
                       state = state.copy(number2 = formatResult(value))
                   }
                }
                else -> {
                    if (state.number2.isNotBlank()) performCalculation()
                    state = state.copy(operation = operation)
                }
            }
        } else if (operation is CalculatorOperation.Pi || operation is CalculatorOperation.Exp) {
             val value = if (operation is CalculatorOperation.Pi) PI else E
             state = state.copy(number1 = formatResult(value))
        }
    }

    private fun performUnaryOperation(operation: CalculatorOperation) {
        val number = state.number1.toDoubleOrNull() ?: return
        val result = when(operation) {
            is CalculatorOperation.SquareRoot -> if (number >= 0) sqrt(number) else Double.NaN
            is CalculatorOperation.Square -> number * number
            is CalculatorOperation.Logarithm -> if (number > 0) log10(number) else Double.NaN
            is CalculatorOperation.Factorial -> factorial(number)
            is CalculatorOperation.Sin -> sin(Math.toRadians(number))
            is CalculatorOperation.Cos -> cos(Math.toRadians(number))
            is CalculatorOperation.Tan -> tan(Math.toRadians(number))
            else -> return
        }
        
        val formattedResult = formatResult(result)
        if (formattedResult != "Error") {
            val historyEntry = "${operation.symbol}($number) = $formattedResult"
            addToHistory(historyEntry)
            state = state.copy(
                number1 = formattedResult,
                number2 = "",
                operation = null
            )
        } else {
            state = state.copy(number1 = "Error", number2 = "", operation = null)
        }
    }
    
    private fun addToHistory(entry: String) {
        val newHistory = listOf(entry) + state.history
        state = state.copy(history = newHistory.take(20))
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n > 170 || n % 1 != 0.0) return Double.NaN
        if (n == 0.0) return 1.0
        var res = 1.0
        for (i in 1..n.toInt()) res *= i
        return res
    }

    private fun formatResult(result: Double): String {
        if (result.isNaN() || !result.isFinite()) return "Error"
        return if (result % 1 == 0.0) {
            if (abs(result) < 1e15) result.toLong().toString() else "%.4e".format(Locale.US, result)
        } else {
            val s = "%.4f".format(Locale.US, result).trimEnd('0').trimEnd('.')
            if (s.length > 15) "%.4e".format(Locale.US, result) else s
        }
    }

    private fun enterDecimal() {
        if(state.operation == null) {
            if (!state.number1.contains(".") && state.number1 != "Error") {
                state = state.copy(number1 = (if(state.number1.isEmpty()) "0" else state.number1) + ".")
            }
        } else {
            if (!state.number2.contains(".")) {
                state = state.copy(number2 = (if(state.number2.isEmpty()) "0" else state.number2) + ".")
            }
        }
    }

    private fun enterNumber(number: Int) {
        if (state.number1 == "Error") state = state.copy(number1 = "")
        
        if(state.operation == null) {
            if(state.number1.length >= 12) return
            state = state.copy(number1 = state.number1 + number)
        } else {
            if(state.number2.length >= 12) return
            state = state.copy(number2 = state.number2 + number)
        }
    }
}
