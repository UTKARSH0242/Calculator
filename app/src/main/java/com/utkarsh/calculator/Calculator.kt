package com.utkarsh.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utkarsh.calculator.ui.theme.Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calculator(
    state: CalculatorState,
    buttonSpacing: Dp,
    modifier: Modifier = Modifier,
    onAction: (CalculatorActions) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val historyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator", style = MaterialTheme.typography.titleMedium) },
                actions = {
                    IconButton(
                        onClick = { onAction(CalculatorActions.ToggleHistory) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.History, 
                            contentDescription = "History",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues).fillMaxSize()) {
            if (state.isHistoryVisible) {
                HistorySection(state.history, historyListState, onAction, Modifier.fillMaxSize())
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    DisplaySection(state, isLandscape, context, Modifier.weight(1f))
                    KeyboardSection(state, isLandscape, buttonSpacing, onAction)
                }
            }
        }
    }
}

@Composable
fun HistorySection(
    history: List<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onAction: (CalculatorActions) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { onAction(CalculatorActions.ClearHistory) }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear History")
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { entry ->
                val parts = entry.split("=")
                val expression = parts.getOrNull(0)?.trim() ?: ""
                val result = parts.getOrNull(1)?.trim() ?: ""
                Column(
                    modifier = Modifier.fillMaxWidth().clickable { onAction(CalculatorActions.UseHistory(result)) },
                    horizontalAlignment = Alignment.End
                ) {
                    Text(expression, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(result, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DisplaySection(
    state: CalculatorState,
    isLandscape: Boolean,
    context: Context,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    LaunchedEffect(state.number1, state.operation, state.number2) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(state.number1, state.operation, state.number2) {
                    detectTapGestures(onLongPress = {
                        val displayText = state.number1 + (state.operation?.symbol ?: "") + state.number2
                        if (displayText.isNotBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Calc", displayText))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    })
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    modifier = Modifier.horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    val number1Color = if (state.operation == null) Orange else MaterialTheme.colorScheme.onBackground
                    val operationColor = MaterialTheme.colorScheme.primary
                    val number2Color = if (state.operation != null) Orange else MaterialTheme.colorScheme.onBackground

                    Text(
                        text = state.number1,
                        textAlign = TextAlign.End,
                        style = if (isLandscape) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayLarge,
                        color = number1Color,
                        maxLines = 1,
                        softWrap = false
                    )
                    state.operation?.let {
                        Text(
                            text = it.symbol,
                            textAlign = TextAlign.End,
                            style = if (isLandscape) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayLarge,
                            color = operationColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Text(
                        text = state.number2,
                        textAlign = TextAlign.End,
                        style = if (isLandscape) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayLarge,
                        color = number2Color,
                        maxLines = 1,
                        softWrap = false
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .height(if (isLandscape) 30.dp else 50.dp)
                            .width(3.dp)
                            .background(color = Orange.copy(alpha = cursorAlpha))
                    )
                }
                if (state.realTimeResult.isNotBlank()) {
                    Text(
                        text = state.realTimeResult,
                        style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KeyboardSection(
    state: CalculatorState,
    isLandscape: Boolean,
    buttonSpacing: Dp,
    onAction: (CalculatorActions) -> Unit
) {
    val finalSpacing = if (isLandscape) 4.dp else 8.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLandscape) 0.dp else 8.dp)
            .animateContentSize(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
    ) {
        if (!isLandscape) {
            ScientificExpandableRow(state.isScientificExpanded, finalSpacing, onAction)
            StandardPad(finalSpacing, onAction, isLandscape = false)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(finalSpacing)
            ) {
                LandscapeScientificPart(finalSpacing, onAction, Modifier.weight(1f))
                StandardPad(finalSpacing, onAction, isLandscape = true, modifier = Modifier.weight(1.2f))
            }
        }
    }
}

@Composable
fun ScientificExpandableRow(isExpanded: Boolean, buttonSpacing: Dp, onAction: (CalculatorActions) -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAction(CalculatorActions.ToggleScientific) }
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.rotate(rotation).size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                ScientificButton("sin", { onAction(CalculatorActions.Operation(CalculatorOperation.Sin)) }, Modifier.weight(1f))
                ScientificButton("cos", { onAction(CalculatorActions.Operation(CalculatorOperation.Cos)) }, Modifier.weight(1f))
                ScientificButton("tan", { onAction(CalculatorActions.Operation(CalculatorOperation.Tan)) }, Modifier.weight(1f))
                ScientificButton("log", { onAction(CalculatorActions.Operation(CalculatorOperation.Logarithm)) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LandscapeScientificPart(spacing: Dp, onAction: (CalculatorActions) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            ScientificButton("sin", { onAction(CalculatorActions.Operation(CalculatorOperation.Sin)) }, Modifier.weight(1f))
            ScientificButton("cos", { onAction(CalculatorActions.Operation(CalculatorOperation.Cos)) }, Modifier.weight(1f))
            ScientificButton("tan", { onAction(CalculatorActions.Operation(CalculatorOperation.Tan)) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            ScientificButton("√", { onAction(CalculatorActions.Operation(CalculatorOperation.SquareRoot)) }, Modifier.weight(1f))
            ScientificButton("x²", { onAction(CalculatorActions.Operation(CalculatorOperation.Square)) }, Modifier.weight(1f))
            ScientificButton("^", { onAction(CalculatorActions.Operation(CalculatorOperation.Power)) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            ScientificButton("log", { onAction(CalculatorActions.Operation(CalculatorOperation.Logarithm)) }, Modifier.weight(1f))
            ScientificButton("n!", { onAction(CalculatorActions.Operation(CalculatorOperation.Factorial)) }, Modifier.weight(1f))
            ScientificButton("π", { onAction(CalculatorActions.Operation(CalculatorOperation.Pi)) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            ScientificButton("e", { onAction(CalculatorActions.Operation(CalculatorOperation.Exp)) }, Modifier.weight(1f))
            ScientificButton("(", { }, Modifier.weight(1f))
            ScientificButton(")", { }, Modifier.weight(1f))
        }
    }
}

@Composable
fun StandardPad(
    buttonSpacing: Dp,
    onAction: (CalculatorActions) -> Unit,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    
    val shape = CircleShape
    val btnModifier = if (isLandscape) Modifier.height(34.dp) else Modifier.aspectRatio(1.1f)

    Column(modifier = modifier.padding(horizontal = if (isLandscape) 0.dp else 4.dp), verticalArrangement = Arrangement.spacedBy(buttonSpacing)) {
        val rowModifier = Modifier.fillMaxWidth()
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalcBtn("AC", Color.Red, secondaryContainer, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Clear) }
            CalcBtn("Del", primary, secondaryContainer, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Delete) }
            CalcBtn("%", primary, secondaryContainer, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Operation(CalculatorOperation.Percentage)) }
            CalcBtn("÷", primary, secondaryContainer, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Operation(CalculatorOperation.Divide)) }
        }
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalcBtn("7", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(7)) }
            CalcBtn("8", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(8)) }
            CalcBtn("9", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(9)) }
            CalcBtn("×", primary, secondaryContainer, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Operation(CalculatorOperation.Multiply)) }
        }
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalcBtn("4", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(4)) }
            CalcBtn("5", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(5)) }
            CalcBtn("6", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(6)) }
            CalcBtn("−", primary, secondaryContainer, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Operation(CalculatorOperation.Subtract)) }
        }
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalcBtn("1", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(1)) }
            CalcBtn("2", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(2)) }
            CalcBtn("3", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(3)) }
            CalcBtn("+", primary, secondaryContainer, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Operation(CalculatorOperation.Add)) }
        }
        Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalcBtn("0", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Number(0)) }
            CalcBtn(".", onSurface, surface, shape, btnModifier.weight(1f)) { onAction(CalculatorActions.Decimal) }
            val equalsShape = RoundedCornerShape(24.dp)
            val equalsBtnModifier = if (isLandscape) Modifier.height(34.dp) else Modifier.aspectRatio(2.2f)
            CalcBtn("=", Color.White, Orange, equalsShape, equalsBtnModifier.weight(2.2f)) { onAction(CalculatorActions.Calculate) }
        }
    }
}

@Composable
fun CalcBtn(symbol: String, textColor: Color, bgColor: Color, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier, onClick: () -> Unit) {
    CalculatorButton(symbol = symbol, color = textColor, modifier = modifier.clip(shape).background(bgColor), onClick = onClick)
}

@Composable
fun ScientificButton(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    CalculatorButton(
        symbol = symbol,
        onClick = onClick,
        color = Orange,
        textStyle = if (isLandscape) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleMedium,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .height(if (isLandscape) 34.dp else 48.dp)
    )
}
