package com.example.expenses

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
// Animation imports
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
// General Compose imports
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expenses.ui.theme.ExpensesTheme
import com.patrykandpatryk.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.entryOf
import java.io.File
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

// --- Define Custom Color ---
val dollarBillGreen = Color(0xFFE5E4E2).copy(green = 0.95f)

// --- Enum for Mascot State ---
enum class MascotState {
    HAPPY, NERVOUS, SAD
}

// --- Default categories for first run ---
val defaultCategories = setOf("Food", "Groceries", "Transport", "Utilities")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpensesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = dollarBillGreen
                ) { innerPadding ->
                    ExpenseScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CsvTable(csvData: String, categoryBudgets: Map<String, Float>) {
    if (csvData.isBlank()) {
        Text(
            "A clean slate! Ready to log your first expense?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        return
    }
    val currentMonthName = YearMonth.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val previousMonthName = YearMonth.now().minusMonths(1).month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val visibleMonths = listOf("Category", currentMonthName, previousMonthName)

    val lines = csvData.trim().split('\n').filter { it.isNotBlank() }
    val originalHeader = lines.firstOrNull()?.split(',') ?: emptyList()
    val dataRows = if (lines.size > 1) lines.drop(1).map { it.split(',') } else emptyList()

    val headerIndicesToShow = originalHeader.mapIndexedNotNull { index, headerName ->
        if (headerName in visibleMonths) index else null
    }
    val filteredHeader = headerIndicesToShow.map { originalHeader[it] }
    val filteredDataRows = dataRows.map { row ->
        headerIndicesToShow.map { index -> row.getOrNull(index) ?: "" }
    }

    val columnTotals = MutableList(filteredHeader.size) { 0.0 }
    if (filteredDataRows.isNotEmpty()) {
        for (colIndex in 1 until filteredHeader.size) {
            var monthTotal = 0.0
            filteredDataRows.forEach { row ->
                monthTotal += row.getOrNull(colIndex)?.toDoubleOrNull() ?: 0.0
            }
            columnTotals[colIndex] = monthTotal
        }
    }

    val currentMonthDataIndex = filteredHeader.indexOf(currentMonthName)

    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(Modifier.padding(vertical = 8.dp)) {
            Text("Category", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
            Text("Budgeted", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            filteredHeader.drop(1).forEach { monthName ->
                val headerText = if (monthName == currentMonthName) "Spent" else monthName
                Text(headerText, modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }

        filteredDataRows.forEach { row ->
            val categoryName = row.firstOrNull() ?: ""
            val categoryBudget = categoryBudgets[categoryName] ?: 0f
            val currentMonthSpending = if (currentMonthDataIndex != -1) row.getOrNull(currentMonthDataIndex)?.toDoubleOrNull() ?: 0.0 else 0.0
            val progress = if (categoryBudget > 0) (currentMonthSpending / categoryBudget).toFloat() else 0f

            val progressBarColor = if (progress > 1.0f) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }

            val animatedProgress by animateFloatAsState(
                targetValue = progress.coerceIn(0f, 1f),
                label = "CategoryProgressAnimation"
            )

            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(categoryName, modifier = Modifier.weight(0.8f))
                Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (categoryBudget > 0) {
                        Text(String.format(Locale.US, "$%.0f", categoryBudget), fontSize = 12.sp)
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = progressBarColor,
                            trackColor = progressBarColor.copy(alpha = 0.3f)
                        )
                    }
                }
                row.drop(1).forEach { cell ->
                    Text(cell, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                }
            }
        }

        if (filteredDataRows.isNotEmpty()) {
            Row(Modifier.padding(vertical = 8.dp)) {
                Text("Total", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                columnTotals.drop(1).forEach { total ->
                    Text(text = String.format(Locale.US, "%.2f", total), modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseScreen(modifier: Modifier = Modifier) {
    var selectedExpense by remember { mutableStateOf<String?>(null) }
    var text by remember { mutableStateOf("") }
    var csvContent by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("ExpenseAppPrefs", Context.MODE_PRIVATE) }

    val categories = remember { mutableStateListOf<String>() }

    var isSetupComplete by remember { mutableStateOf(sharedPrefs.getBoolean("is_setup_complete", false)) }
    var showBudgetSettings by remember { mutableStateOf(!isSetupComplete) }
    var showHistory by remember { mutableStateOf(false) }
    val categoryBudgets = remember { mutableStateMapOf<String, Float>() }
    var tempCategoryBudgets by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var newCategoryText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        val savedCategories = sharedPrefs.getStringSet("user_categories", null)
        if (savedCategories.isNullOrEmpty()) {
            categories.addAll(defaultCategories)
        } else {
            categories.addAll(savedCategories)
        }

        categories.forEach { category ->
            categoryBudgets[category] = sharedPrefs.getFloat("budget_$category", 0f)
        }
        val file = File(context.filesDir, "expenses.csv")
        if (file.exists()) {
            csvContent = file.readText()
        }
    }

    LaunchedEffect(selectedExpense) {
        if (selectedExpense != null) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = modifier.padding(16.dp)) {
        when {
            showBudgetSettings -> {
                // --- UI #1: Full-Screen Budget Settings ---
                Column {
                    Text("Plan Your Budget", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (!isSetupComplete) {
                        Text("Set your budget to begin.", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(categories.sorted()) { category ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(category, modifier = Modifier.weight(1f))
                                TextField(
                                    value = tempCategoryBudgets[category] ?: "",
                                    onValueChange = { newValue -> tempCategoryBudgets = tempCategoryBudgets + (category to newValue) },
                                    label = { Text("Budget") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newCategoryText,
                            onValueChange = { newCategoryText = it },
                            label = { Text("Add New Expense Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newCategoryText.isNotBlank() && !categories.contains(newCategoryText)) {
                                    categories.add(newCategoryText)
                                    tempCategoryBudgets = tempCategoryBudgets + (newCategoryText to "0")
                                    newCategoryText = ""
                                    focusManager.clearFocus()
                                }
                            })
                        )
                        IconButton(onClick = {
                            if (newCategoryText.isNotBlank() && !categories.contains(newCategoryText)) {
                                categories.add(newCategoryText)
                                tempCategoryBudgets = tempCategoryBudgets + (newCategoryText to "0")
                                newCategoryText = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Category")
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            sharedPrefs.edit {
                                putStringSet("user_categories", categories.toSet())

                                tempCategoryBudgets.forEach { (category, budgetStr) ->
                                    val budgetFloat = budgetStr.toFloatOrNull() ?: 0f
                                    categoryBudgets[category] = budgetFloat
                                    putFloat("budget_$category", budgetFloat)
                                }

                                putBoolean("is_setup_complete", true)
                            }

                            isSetupComplete = true
                            showBudgetSettings = false
                            focusManager.clearFocus()
                        }
                    ) {
                        Text(if (isSetupComplete) "Update Plan" else "Start tracking expenses")
                    }
                }
            }
            showHistory -> {
                HistoryScreen(csvData = csvContent, onBack = { showHistory = false })
            }
            else -> {
                // --- UI #2: Full-Screen Main Dashboard ---
                Column {
                    val overallBudget = categoryBudgets.values.sum()
                    val monthlyTotal = remember(csvContent) {
                        val lines = csvContent.trim().split('\n').filter { it.isNotBlank() }
                        if (lines.size <= 1) return@remember 0.0

                        val header = lines.first().split(',')
                        val currentMonthName = YearMonth.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        val monthIndex = header.indexOf(currentMonthName)
                        if (monthIndex == -1) return@remember 0.0

                        lines.drop(1).sumOf { row ->
                            row.split(',').getOrNull(monthIndex)?.toDoubleOrNull() ?: 0.0
                        }
                    }

                    val rawProgress by remember(monthlyTotal, overallBudget) {
                        derivedStateOf {
                            if (overallBudget > 0) (monthlyTotal / overallBudget).toFloat() else 0f
                        }
                    }

                    val currentMascotState by remember(rawProgress) {
                        derivedStateOf {
                            when {
                                rawProgress > 1.0f -> MascotState.SAD
                                rawProgress > 0.9f -> MascotState.NERVOUS
                                else -> MascotState.HAPPY
                            }
                        }
                    }

                    val animatedOverallProgress by animateFloatAsState(
                        targetValue = rawProgress.coerceIn(0f, 1f),
                        label = "OverallProgressAnimation"
                    )

                    Text("${YearMonth.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())}'s Expenses:", style = MaterialTheme.typography.titleLarge)
                    CsvTable(csvData = csvContent, categoryBudgets = categoryBudgets)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Remaining budget:", fontWeight = FontWeight.Bold)
                        Text(text = String.format(Locale.US, "$%.2f", (overallBudget - monthlyTotal)), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { animatedOverallProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (rawProgress > 0.9) Color.Red else if (rawProgress > 0.75) Color.Yellow else Color.Green
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Crossfade(targetState = currentMascotState, label = "MascotCrossfade") { mascotState ->
                        val mascotRes = when (mascotState) {
                            MascotState.SAD -> R.drawable.mascot_sad
                            MascotState.NERVOUS -> R.drawable.mascot_nervous
                            MascotState.HAPPY -> R.drawable.mascot_happy
                        }
                        Image(
                            painter = painterResource(id = mascotRes),
                            contentDescription = "Budget Mascot",
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalArrangement = Arrangement.Center
                    ) {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            Button(onClick = { menuExpanded = true }) {
                                Text("Log Expense")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                categories.sorted().forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            selectedExpense = category
                                            text = ""
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedButton(onClick = {
                            tempCategoryBudgets = categoryBudgets.mapValues { it.value.roundToInt().toString() }
                            showBudgetSettings = true
                        }) {
                            Text(text = "Edit Budget")
                        }
                        IconButton(onClick = { showHistory = true }) {
                            Icon(Icons.Default.BarChart, contentDescription = "Spending History")
                        }
                    }

                    if (selectedExpense != null) {
                        val saveAction = {
                            if (text.isNotBlank()) {
                                val currentMonth = YearMonth.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                val newValue = text.toDoubleOrNull() ?: 0.0
                                val file = File(context.filesDir, "expenses.csv")

                                val lines = if (file.exists() && file.readText().isNotBlank()) file.readLines() else emptyList()
                                val header = if (lines.isNotEmpty()) lines[0].split(',').toMutableList() else mutableListOf("Category")
                                val dataMap = if (lines.size > 1) {
                                    lines.drop(1).mapNotNull { line ->
                                        val parts = line.split(',')
                                        if (parts.isNotEmpty()) parts[0] to parts.drop(1).toMutableList() else null
                                    }.toMap().toMutableMap()
                                } else {
                                    mutableMapOf()
                                }

                                if (!header.contains(currentMonth)) {
                                    header.add(currentMonth)
                                }
                                val monthIndex = header.indexOf(currentMonth) - 1

                                if (!dataMap.containsKey(selectedExpense!!)) {
                                    dataMap[selectedExpense!!] = mutableListOf()
                                }
                                val categoryData = dataMap[selectedExpense!!]!!
                                while (categoryData.size <= monthIndex) {
                                    categoryData.add("0.0")
                                }
                                val oldValue = categoryData[monthIndex].toDoubleOrNull() ?: 0.0
                                categoryData[monthIndex] = (oldValue + newValue).toString()

                                val updatedCsvContent = buildString {
                                    appendLine(header.joinToString(","))
                                    dataMap.keys.sorted().forEach { category ->
                                        val rowData = dataMap[category]!!
                                        while (rowData.size < header.size - 1) {
                                            rowData.add("0.0")
                                        }
                                        appendLine("$category,${rowData.joinToString(",")}")
                                    }
                                }.trim()

                                file.writeText(updatedCsvContent)
                                csvContent = updatedCsvContent

                                selectedExpense = null
                                text = ""
                                focusManager.clearFocus()
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = text,
                            onValueChange = { newText ->
                                if (newText.isEmpty() || newText.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    text = newText
                                 }
                            },
                            label = { Text("How much for $selectedExpense?") },
                            singleLine = true,
                            modifier = Modifier.focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { saveAction() })
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(csvData: String, onBack: () -> Unit) {
    val chartModelProducer = remember { ChartEntryModelProducer() }
    var entries by remember { mutableStateOf<List<ChartEntry>>(emptyList()) }

    LaunchedEffect(csvData) {
        val newEntries = if (csvData.isNotBlank()) {
            val lines = csvData.trim().split('\n').filter { it.isNotBlank() }
            if (lines.size > 1) {
                val header = lines.first().split(',').drop(1) // Drop "Category"
                val totals = MutableList(header.size) { 0.0 }

                lines.drop(1).forEach { line ->
                    val values = line.split(',').drop(1)
                    values.forEachIndexed { index, value ->
                        if (index < totals.size) {
                            totals[index] += value.toDoubleOrNull() ?: 0.0
                        }
                    }
                }

                totals.mapIndexed { index, total ->
                    entryOf(index.toFloat(), total.toFloat())
                }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
        entries = newEntries
        chartModelProducer.setEntries(newEntries)
    }

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val lines = csvData.trim().split('\n').filter { it.isNotBlank() }
        val header = if (lines.isNotEmpty()) lines.first().split(',').drop(1) else emptyList()
        header.getOrNull(value.toInt())?.take(3) ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Spending History", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(if (csvData.isBlank()) "No spending history yet." else "Processing data...")
            }
        } else {
            Chart(
                chart = columnChart(),
                chartModelProducer = chartModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to Dashboard")
        }
    }
}
