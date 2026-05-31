package com.example.macrotrainingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.ui.model.FoodTrackerUiState
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.example.macrotrainingtracker.ui.theme.Warning
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun TodayScreen(
    state: FoodTrackerUiState,
    onDeleteFood: (Long) -> Unit,
    onUpdateFood: (FoodLogEntity) -> Unit,
    onQuickAdd: (String, Int, Int, Int) -> Unit,
    onSnoozeRecalcReminder: () -> Unit
) {
    var showQuickAdd by remember { mutableStateOf(false) }
    var editingLog   by remember { mutableStateOf<FoodLogEntity?>(null) }

    val today = LocalDate.now()
    val plan  = state.activePlan
    val logs  = state.todayLogs

    val consumed  = logs.sumOf { it.calories }
    val consumedP = logs.sumOf { it.protein }
    val consumedF = logs.sumOf { it.fats }
    val consumedC = logs.sumOf { it.carbs }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        if (state.shouldShowRecalcReminder) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Warning.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .border(1.dp, Warning.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Czas na przeliczenie", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Warning)
                    }
                    TextButton(onClick = onSnoozeRecalcReminder) { Text("Odłóż", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DZIŚ", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                val dayName = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("pl", "PL")).replaceFirstChar { it.uppercase() }
                val dateStr = "${today.dayOfMonth} ${today.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("pl", "PL")).replaceFirstChar { it.uppercase() }} ${today.year}"
                Text(dayName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(dateStr, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)).padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SPOŻYTE DZIŚ", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                    Text("$consumed", fontSize = 56.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, letterSpacing = (-2).sp)
                    Text("KCAL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    if (plan != null) {
                        val pct = (consumed.toFloat() / plan.targetCalories).coerceIn(0f, 1f)
                        LinearProgressIndicator(progress = { pct }, modifier = Modifier.fillMaxWidth().height(8.dp), color = if (consumed > plan.targetCalories) Warning else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outline)
                        val remaining = plan.targetCalories - consumed
                        Text(if (remaining >= 0) "$remaining kcal pozostało" else "${-remaining} kcal ponad cel", fontSize = 12.sp, color = if (remaining < 0) Warning else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (plan != null) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DayInfoChip("DZIEŃ", state.daysSinceStart.toString(), Modifier.weight(1f))
                    DayInfoChip("CEL", "${plan.targetCalories} kcal", Modifier.weight(2f))
                }
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF121212), RoundedCornerShape(14.dp)).border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(14.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("MAKROSKŁADNIKI", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp)
                        TodayMacroRow("Białko", consumedP, plan.targetProtein, AccentPrimary)
                        TodayMacroRow("Tłuszcze", consumedF, plan.targetFats, Color(0xFFFF9800))
                        TodayMacroRow("Węglowodany", consumedC, plan.targetCarbs, Color(0xFF4FC3F7))
                    }
                }
            }
        }

        item {
            Button(onClick = { showQuickAdd = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = AccentPrimary)
            ) { Text("+ QUICK ADD", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }

        if (logs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF121212), RoundedCornerShape(14.dp)).border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(14.dp)).padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🍽️", fontSize = 36.sp)
                        Text("Brak wpisów na dziś", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9E9E9E))
                        Text("Dodaj produkty z zakładki Żywienie", fontSize = 12.sp, color = Color(0xFF616161))
                    }
                }
            }
        } else {
            item {
                Text("ZALOGOWANE DZIŚ  •  ${logs.size} pozycji", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp)
            }
            items(logs, key = { it.id }) { log ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                        .clickable { editingLog = log }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.sourceName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            "B: ${log.protein}g  T: ${log.fats}g  W: ${log.carbs}g${if (log.grams > 0) "  •  ${"%.1f".format(log.grams)}g" else ""}",
                            fontSize = 11.sp, color = Color(0xFF9E9E9E)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${log.calories}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AccentPrimary)
                            Text("kcal", fontSize = 9.sp, color = Color(0xFF616161))
                        }
                        IconButton(onClick = { onDeleteFood(log.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = Color(0xFF616161), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showQuickAdd) {
        QuickAddDialog(onDismiss = { showQuickAdd = false }, onAdd = { name, p, f, c -> onQuickAdd(name, p, f, c); showQuickAdd = false })
    }

    editingLog?.let { log ->
        EditTodayLogDialog(
            log = log,
            onDismiss = { editingLog = null },
            onSave = { updated -> onUpdateFood(updated); editingLog = null }
        )
    }
}

// ── Edit dialog for today's log entries ──────────────────────────────────────

@Composable
private fun EditTodayLogDialog(log: FoodLogEntity, onDismiss: () -> Unit, onSave: (FoodLogEntity) -> Unit) {
    var grams   by remember(log.id) { mutableStateOf(if (log.grams > 0) "%.1f".format(log.grams) else "") }
    var protein by remember(log.id) { mutableStateOf(log.protein.toString()) }
    var fats    by remember(log.id) { mutableStateOf(log.fats.toString()) }
    var carbs   by remember(log.id) { mutableStateOf(log.carbs.toString()) }

    val p = protein.toIntOrNull() ?: 0
    val f = fats.toIntOrNull()    ?: 0
    val c = carbs.toIntOrNull()   ?: 0
    val calories = p * 4 + f * 9 + c * 4

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp),
        title = { Text(log.sourceName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("$calories kcal", fontSize = 28.sp, fontWeight = FontWeight.Black, color = AccentPrimary)
                }
                if (log.grams > 0) {
                    OutlinedTextField(
                        value = grams, onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) grams = it },
                        label = { Text("GRAMATURA (g)", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                    )
                }
                OutlinedTextField(
                    value = protein, onValueChange = { if (it.matches(Regex("^\\d*$"))) protein = it },
                    label = { Text("BIAŁKO (g)", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
                OutlinedTextField(
                    value = fats, onValueChange = { if (it.matches(Regex("^\\d*$"))) fats = it },
                    label = { Text("TŁUSZCZE (g)", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
                OutlinedTextField(
                    value = carbs, onValueChange = { if (it.matches(Regex("^\\d*$"))) carbs = it },
                    label = { Text("WĘGLOWODANY (g)", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(log.copy(
                    grams    = grams.toDoubleOrNull() ?: log.grams,
                    protein  = p, fats = f, carbs = c, calories = calories
                ))
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary), shape = RoundedCornerShape(10.dp)) {
                Text("ZAPISZ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ANULUJ", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun DayInfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = AccentPrimary)
        }
    }
}

@Composable
private fun TodayMacroRow(label: String, current: Int, target: Int, barColor: Color) {
    val pct = if (target > 0) current.toFloat() / target else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = Color(0xFF9E9E9E))
            Text("$current / $target g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (pct > 1f) Warning else Color.White)
        }
        LinearProgressIndicator(progress = { pct.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(5.dp), color = if (pct > 1f) Warning else barColor, trackColor = Color(0xFF2A2A2A))
    }
}

