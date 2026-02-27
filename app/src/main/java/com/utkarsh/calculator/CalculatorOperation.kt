package com.utkarsh.calculator

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class CalculatorOperation(val symbol: String) : Parcelable {
    @Parcelize object Add : CalculatorOperation("+")
    @Parcelize object Subtract : CalculatorOperation("-")
    @Parcelize object Multiply : CalculatorOperation("×")
    @Parcelize object Divide : CalculatorOperation("÷")
    @Parcelize object Percentage : CalculatorOperation("%")
    @Parcelize object SquareRoot : CalculatorOperation("√")
    @Parcelize object Square : CalculatorOperation("x²")
    @Parcelize object Logarithm : CalculatorOperation("log")
    @Parcelize object Factorial : CalculatorOperation("!")
    @Parcelize object Sin : CalculatorOperation("sin")
    @Parcelize object Cos : CalculatorOperation("cos")
    @Parcelize object Tan : CalculatorOperation("tan")
    @Parcelize object Exp : CalculatorOperation("e")
    @Parcelize object Pi : CalculatorOperation("π")
    @Parcelize object Power : CalculatorOperation("^")
}
