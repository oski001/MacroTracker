package com.example.macrotrainingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import com.example.macrotrainingtracker.ui.model.LogsUiState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.example.macrotrainingtracker.ui.theme.Success
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val DAYS_OF_WEEK = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

@Composable
fun FoodLogScreen(
    state: LogsUiState,
    products: List<FoodProductEntity>,
    onSelectDate: (String?) -> Unit,
    onDeleteLog: (Long) -> Unit,
    onUpdateLog: (FoodLogEntity) -> Unit,
    onQuickAdd: (String, String, Int, Int, Int) -> Unit,
    onAddFromProduct: (FoodProductEntity, Double, String) -> Unit
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedDate) { onSelectDate(selectedDate) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text("HISTORIA", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
        Text("Dziennik żywienia", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Poprzedni miesiąc", tint = MaterialTheme.colorScheme.onSurface)
            }
            val monthName = displayedMonth.month.getDisplayName(TextStyle.FULL, Locale("pl", "PL"))
                .replaceFirstChar { it.uppercase() }
            Text("$monthName ${displayedMonth.year}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            IconButton(
                onClick = { if (displayedMonth < YearMonth.now()) displayedMonth = displayedMonth.plusMonths(1) },
                enabled = displayedMonth < YearMonth.now()
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Następny miesiąc", tint = if (displayedMonth < YearMonth.now()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)
            }
        }

        // Day of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            DAYS_OF_WEEK.forEach { day ->
                Text(
                    day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Medium
                )
            }
        }

        // Calendar grid
        CalendarGrid(
            yearMonth = displayedMonth,
            loggedDates = state.allLoggedDates,
            selectedDate = selectedDate,
            onDayClick = { date ->
                selectedDate = if (selectedDate == date) null else date
            }
        )

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(Success, "Zalogowane kalorie")
            LegendItem(MaterialTheme.colorScheme.outline, "Brak wpisów")
        }
    }

    // Day detail dialog
    if (selectedDate != null && state.selectedDateLogs.isNotEmpty() || selectedDate != null) {
        if (selectedDate != null) {
            DayDetailSheet(
                date = selectedDate!!,
                logs = state.selectedDateLogs,
                products = products,
                onDismiss = { selectedDate = null; onSelectDate(null) },
                onDeleteLog = onDeleteLog,
                onUpdateLog = onUpdateLog,
                onQuickAdd = { name, p, f, c -> onQuickAdd(selectedDate!!, name, p, f, c) },
                onAddFromProduct = { product, grams -> onAddFromProduct(product, grams, selectedDate!!) }
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    loggedDates: Set<String>,
    selectedDate: String?,
    onDayClick: (String) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // ISO day of week: Monday=1, Sunday=7 → offset for Monday-start grid
    val startOffset = firstDay.dayOfWeek.value - 1
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7
    val today = LocalDate.now().toString()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1
                    val modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)

                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Box(modifier = modifier)
                    } else {
                        val date = yearMonth.atDay(dayNumber).toString()
                        val hasLogs = date in loggedDates
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val isFuture = date > today

                        Box(
                            modifier = modifier
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        hasLogs -> Success.copy(alpha = 0.2f)
                                        isToday -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> Color.Transparent
                                    },
                                    CircleShape
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.dp else 0.dp,
                                    color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable(enabled = !isFuture) { onDayClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    dayNumber.toString(),
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        hasLogs -> Success
                                        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                if (hasLogs && !isSelected) {
                                    Box(modifier = Modifier.size(4.dp).background(Success, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
    }
}

@Composable
private fun DayDetailSheet(
    date: String,
    logs: List<FoodLogEntity>,
    products: List<FoodProductEntity>,
    onDismiss: () -> Unit,
    onDeleteLog: (Long) -> Unit,
    onUpdateLog: (FoodLogEntity) -> Unit,
    onQuickAdd: (String, Int, Int, Int) -> Unit,
    onAddFromProduct: (FoodProductEntity, Double) -> Unit
) {
    var showQuickAdd by remember { mutableStateOf(false) }
    var showLibrary by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<FoodLogEntity?>(null) }

    val parsed = LocalDate.parse(date)
    val totalCalories = logs.sumOf { it.calories }
    val totalProtein = logs.sumOf { it.protein }
    val totalFats = logs.sumOf { it.fats }
    val totalCarbs = logs.sumOf { it.carbs }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Date header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            "${parsed.dayOfMonth} ${parsed.month.getDisplayName(TextStyle.FULL, Locale("pl", "PL")).replaceFirstChar { it.uppercase() }} ${parsed.year}",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                        )
                        if (logs.isNotEmpty()) Text("$totalCalories kcal • B:${totalProtein}g T:${totalFats}g W:${totalCarbs}g", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onDismiss) { Text("Zamknij", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }

                // Add buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showQuickAdd = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)
                    ) { Text("+ QUICK ADD", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { showLibrary = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) { Text("+ Z BIBLIOTEKI", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Logs list
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Brak wpisów", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Dodaj posiłek z biblioteki lub Quick Add", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(logs, key = { it.id }) { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.sourceName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("B:${log.protein}g  T:${log.fats}g  W:${log.carbs}g${if (log.grams > 0) "  • ${log.grams.roundToInt()}g" else ""}", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${log.calories}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = AccentPrimary)
                                    Text(" kcal", fontSize = 10.sp, color = Color(0xFF616161))
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(onClick = { editingLog = log }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = Color(0xFF9E9E9E), modifier = Modifier.size(15.dp))
                                    }
                                    IconButton(onClick = { onDeleteLog(log.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color(0xFF616161), modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick add for this date
    if (showQuickAdd) {
        QuickAddDialog(date = date, onDismiss = { showQuickAdd = false }, onAdd = { name, p, f, c ->
            onQuickAdd(name, p, f, c)
            showQuickAdd = false
        })
    }

    // Library picker for this date
    if (showLibrary) {
        LibraryPickerDialog(
            products = products,
            onDismiss = { showLibrary = false },
            onSelect = { product, grams ->
                onAddFromProduct(product, grams)
                showLibrary = false
            }
        )
    }

    // Edit log
    editingLog?.let { log ->
        EditLogDialog(
            log = log,
            onDismiss = { editingLog = null },
            onSave = { updated ->
                onUpdateLog(updated)
                editingLog = null
            }
        )
    }
}

@Composable
private fun LibraryPickerDialog(
    products: List<FoodProductEntity>,
    onDismiss: () -> Unit,
    onSelect: (FoodProductEntity, Double) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<FoodProductEntity?>(null) }

    val filtered = remember(search, products) {
        if (search.length < 2) products else products.filter { it.name.contains(search, ignoreCase = true) }
    }

    if (selectedProduct != null) {
        // Grams input
        var grams by remember { mutableStateOf("100") }
        val g = grams.toDoubleOrNull() ?: 0.0
        val product = selectedProduct!!
        val calories = (product.caloriesPerGram * g).roundToInt()
        AlertDialog(
            onDismissRequest = { selectedProduct = null },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(20.dp),
            title = { Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("$calories kcal", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AccentPrimary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = grams, onValueChange = { grams = it }, label = { Text("GRAMATURA (g)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }
            },
            confirmButton = { Button(onClick = { onSelect(product, g) }, colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black)) { Text("DODAJ") } },
            dismissButton = { TextButton(onClick = { selectedProduct = null }) { Text("WSTECZ") } }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp)).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Wybierz produkt", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("SZUKAJ") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filtered) { product ->
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF121212), RoundedCornerShape(8.dp)).clickable { selectedProduct = product }.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                Text("${(product.caloriesPerGram * 100).roundToInt()} kcal/100g", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("ANULUJ") }
            }
        }
    }
}

@Composable
private fun EditLogDialog(log: FoodLogEntity, onDismiss: () -> Unit, onSave: (FoodLogEntity) -> Unit) {
    var grams by remember { mutableStateOf(if (log.grams > 0) log.grams.roundToInt().toString() else "") }
    var protein by remember { mutableStateOf(log.protein.toString()) }
    var fats by remember { mutableStateOf(log.fats.toString()) }
    var carbs by remember { mutableStateOf(log.carbs.toString()) }

    val p = protein.toIntOrNull() ?: 0
    val f = fats.toIntOrNull() ?: 0
    val c = carbs.toIntOrNull() ?: 0
    val calories = p * 4 + f * 9 + c * 4

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        title = { Text(log.sourceName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$calories kcal", fontSize = 22.sp, fontWeight = FontWeight.Black, color = AccentPrimary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                if (log.grams > 0) {
                    OutlinedTextField(value = grams, onValueChange = { grams = it }, label = { Text("GRAMATURA (g)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }
                OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("BIAŁKO (g)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = fats, onValueChange = { fats = it }, label = { Text("TŁUSZCZE (g)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("WĘGLOWODANY (g)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(log.copy(
                        grams = grams.toDoubleOrNull() ?: log.grams,
                        protein = p, fats = f, carbs = c, calories = calories
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) { Text("ZAPISZ", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ", color = Color(0xFF9E9E9E)) } }
    )
}
