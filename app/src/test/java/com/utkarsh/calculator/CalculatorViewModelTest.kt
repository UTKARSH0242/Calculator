package com.utkarsh.calculator

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        viewModel = CalculatorViewModel(SavedStateHandle())
    }

    @Test
    fun `test addition`() {
        viewModel.onAction(CalculatorActions.Number(5))
        viewModel.onAction(CalculatorActions.Operation(CalculatorOperation.Add))
        viewModel.onAction(CalculatorActions.Number(3))
        viewModel.onAction(CalculatorActions.Calculate)
        
        assertEquals("8", viewModel.state.number1)
    }

    @Test
    fun `test subtraction`() {
        viewModel.onAction(CalculatorActions.Number(10))
        viewModel.onAction(CalculatorActions.Operation(CalculatorOperation.Subtract))
        viewModel.onAction(CalculatorActions.Number(4))
        viewModel.onAction(CalculatorActions.Calculate)
        
        assertEquals("6", viewModel.state.number1)
    }

    @Test
    fun `test multiplication`() {
        viewModel.onAction(CalculatorActions.Number(7))
        viewModel.onAction(CalculatorActions.Operation(CalculatorOperation.Multiply))
        viewModel.onAction(CalculatorActions.Number(6))
        viewModel.onAction(CalculatorActions.Calculate)
        
        assertEquals("42", viewModel.state.number1)
    }

    @Test
    fun `test division`() {
        viewModel.onAction(CalculatorActions.Number(15))
        viewModel.onAction(CalculatorActions.Operation(CalculatorOperation.Divide))
        viewModel.onAction(CalculatorActions.Number(3))
        viewModel.onAction(CalculatorActions.Calculate)
        
        assertEquals("5", viewModel.state.number1)
    }

    @Test
    fun `test clear`() {
        viewModel.onAction(CalculatorActions.Number(5))
        viewModel.onAction(CalculatorActions.Clear)
        
        assertEquals("", viewModel.state.number1)
        assertEquals("", viewModel.state.number2)
        assertEquals(null, viewModel.state.operation)
    }

    @Test
    fun `test history limit`() {
        repeat(25) {
            viewModel.onAction(CalculatorActions.Number(1))
            viewModel.onAction(CalculatorActions.Operation(CalculatorOperation.Add))
            viewModel.onAction(CalculatorActions.Number(1))
            viewModel.onAction(CalculatorActions.Calculate)
        }
        
        assertEquals(20, viewModel.state.history.size)
    }
}
