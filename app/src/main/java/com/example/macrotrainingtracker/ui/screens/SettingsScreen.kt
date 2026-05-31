package com.example.macrotrainingtracker.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macrotrainingtracker.data.local.entity.NutritionPlanEntity
import com.example.macrotrainingtracker.data.local.entity.UserBodyEntity
import com.example.macrotrainingtracker.domain.model.Gender
import com.example.macrotrainingtracker.domain.model.PalLevel
import com.example.macrotrainingtracker.domain.model.PhaseType
import com.example.macrotrainingtracker.ui.model.MenuUiState
import com.example.macrotrainingtracker.ui.model.OnboardingInput
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.example.macrotrainingtracker.ui.theme.AppThemePreset
import com.example.macrotrainingtracker.ui.theme.Success
import com.example.macrotrainingtracker.ui.theme.ThemeManager
import com.example.macrotrainingtracker.ui.theme.Warning
import kotlin.math.roundToInt

var showThemeSettings: Boolean by mutableStateOf(false)

@Composable
fun SettingsScreen(
    state: MenuUiState,
    onSetActivePlan: (Long) -> Unit,
    onUpdatePlan: (NutritionPlanEntity) -> Unit,
    onDeletePlan: (Long) -> Unit,
    onAddNewPlan: (OnboardingInput) -> Unit,
    onUpdateBody: (OnboardingInput) -> Unit,
    onRestartDayCounter: () -> Unit,
    context: Context
) {
    var showNewPlanSetup  by remember { mutableStateOf(false) }
    var showEditBody      by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var editingPlan       by remember { mutableStateOf<NutritionPlanEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("USTAWIENIA", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
            Text("Menu", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        // Active plan summary
        state.activePlan?.let { plan ->
            Box(modifier = Modifier.fillMaxWidth()
                .background(AccentPrimary.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                .border(1.dp, AccentPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("AKTYWNY PLAN", fontSize = 11.sp, color = AccentPrimary, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("DZIEŃ ${state.daysSinceStart}", fontSize = 11.sp, color = Color(0xFF9E9E9E), fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { editingPlan = plan }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edytuj plan", tint = AccentPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Text(plan.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    val phase = runCatching { PhaseType.valueOf(plan.phase) }.getOrNull()
                    Text("${phase?.emoji ?: ""} ${phase?.label ?: plan.phase}", fontSize = 13.sp, color = AccentPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MacroChip("${plan.targetCalories}", "kcal")
                        MacroChip("${plan.targetProtein}g", "białko")
                        MacroChip("${plan.targetFats}g", "tłuszcze")
                        MacroChip("${plan.targetCarbs}g", "węgle")
                    }
                    if (plan.daysToGoal > 0) Text("Cel: ${plan.daysToGoal} dni (${plan.daysToGoal / 7} tyg.)", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }
        }

        // Plans section
        SectionHeader("PLANY ŻYWIENIOWE")
        state.plans.forEach { plan ->
            PlanCard(
                plan = plan,
                isActive = plan.id == state.activePlan?.id,
                onActivate = { onSetActivePlan(plan.id) },
                onEdit = { editingPlan = plan },
                onDelete = { if (state.plans.size > 1) onDeletePlan(plan.id) }
            )
        }
        Button(onClick = { showNewPlanSetup = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A), contentColor = AccentPrimary)
        ) { Text("+ NOWY PLAN", fontSize = 13.sp, fontWeight = FontWeight.Bold) }

        // Body profile
        SectionHeader("PARAMETRY CIAŁA")
        state.body?.let { body ->
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("TWOJE PARAMETRY", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                        IconButton(onClick = { showEditBody = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    BodyRow("Płeć", if (body.gender == "MALE") "Mężczyzna" else "Kobieta")
                    BodyRow("Wiek", "${body.age} lat")
                    BodyRow("Wzrost", "${body.heightCm} cm")
                    BodyRow("Waga", "${body.weightKg} kg")
                    BodyRow("Poziom PAL", runCatching { PalLevel.valueOf(body.palLevel).label }.getOrDefault(body.palLevel))
                }
            }
        }

        // Theme
        SectionHeader("STYL APLIKACJI")
        Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("KOLOR AKCENTU", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                    Text(ThemeManager.currentPreset.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentPrimary).border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape))
                    IconButton(onClick = { showThemeSettings = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Palette, contentDescription = "Styl", tint = AccentPrimary, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        // Day counter
        SectionHeader("LICZNIK DNI")
        Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Dzień ${state.daysSinceStart}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = AccentPrimary)
                    Text("od rozpoczęcia śledzenia", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { showRestartDialog = true }, shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                ) { Text("RESTART", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    // Dialogs
    if (showNewPlanSetup) {
        val bodyInput = state.body?.let { b ->
            OnboardingInput(
                gender = runCatching { Gender.valueOf(b.gender) }.getOrDefault(Gender.MALE),
                age = b.age.toString(), heightCm = b.heightCm.toString(),
                weightKg = b.weightKg.toString(),
                palLevel = runCatching { PalLevel.valueOf(b.palLevel) }.getOrDefault(PalLevel.LIGHTLY_ACTIVE)
            )
        } ?: OnboardingInput()
        NewPlanDialog(initialInput = bodyInput, onDismiss = { showNewPlanSetup = false }, onSave = { onAddNewPlan(it); showNewPlanSetup = false })
    }

    if (showEditBody) {
        EditBodyDialog(body = state.body, onDismiss = { showEditBody = false }, onSave = { onUpdateBody(it); showEditBody = false })
    }

    editingPlan?.let { plan ->
        EditPlanDialog(plan = plan, onDismiss = { editingPlan = null }, onSave = { updated -> onUpdatePlan(updated); editingPlan = null })
    }

    if (showThemeSettings) {
        AdvancedThemeSettingsDialog(context = context, onDismiss = { showThemeSettings = false })
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp),
            title = { Text("Restart do Dnia 1?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Licznik zostanie wyzerowany do Dnia 1. Twoje plany i zalogowane posiłki pozostają bez zmian.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = { onRestartDayCounter(); showRestartDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Warning, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("RESTART", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showRestartDialog = false }) { Text("ANULUJ") } }
        )
    }
}

// ── EditPlanDialog ─────────────────────────────────────────────────────────────

@Composable
private fun EditPlanDialog(plan: NutritionPlanEntity, onDismiss: () -> Unit, onSave: (NutritionPlanEntity) -> Unit) {
    // Start from existing plan ratios
    val initCal = plan.targetCalories.coerceAtLeast(1)
    val initProteinPct = ((plan.targetProtein * 4).toFloat() / initCal).coerceIn(0.1f, 0.5f)
    val initFatPct     = ((plan.targetFats   * 9).toFloat() / initCal).coerceIn(0.1f, 0.5f)

    var caloriesText by remember { mutableStateOf(plan.targetCalories.toString()) }
    var proteinPct   by remember { mutableStateOf(initProteinPct) }
    var fatPct       by remember { mutableStateOf(initFatPct) }

    val calories = caloriesText.toIntOrNull()?.coerceAtLeast(100) ?: plan.targetCalories

    // Clamp so that protein% + fat% <= 0.9 (leaving at least 10% for carbs)
    val clampedProtein = proteinPct.coerceIn(0.1f, 0.5f)
    val clampedFat     = fatPct.coerceIn(0.1f, (0.9f - clampedProtein).coerceAtLeast(0.1f))
    val carbPct        = (1f - clampedProtein - clampedFat).coerceAtLeast(0f)

    val proteinG = ((calories * clampedProtein) / 4).roundToInt()
    val fatG     = ((calories * clampedFat)     / 9).roundToInt()
    val carbG    = ((calories * carbPct)        / 4).roundToInt()

    // Actual kcal from macros (may differ slightly due to rounding)
    val actualKcal = proteinG * 4 + fatG * 9 + carbG * 4

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp)).padding(20.dp)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Calories input
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { if (it.matches(Regex("^\\d*$"))) caloriesText = it },
                    label = { Text("CEL KALORYCZNY (kcal)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )

                // Macro summary box
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF121212), RoundedCornerShape(12.dp)).padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        MacroSummaryPill("BIAŁKO",  proteinG, "g", AccentPrimary,        "${(clampedProtein * 100).roundToInt()}%")
                        MacroSummaryPill("TŁUSZCZE",fatG,    "g", Color(0xFFFF9800),    "${(clampedFat     * 100).roundToInt()}%")
                        MacroSummaryPill("WĘGLE",   carbG,   "g", Color(0xFF4FC3F7),    "${(carbPct        * 100).roundToInt()}%")
                        MacroSummaryPill("RAZEM",   actualKcal, "kcal", Color.White,    "")
                    }
                }

                // Protein slider
                MacroSlider(
                    label     = "BIAŁKO",
                    value     = clampedProtein,
                    onValueChange = { proteinPct = it },
                    valueRange = 0.1f..0.5f,
                    color     = AccentPrimary,
                    display   = "${proteinG}g  (${(clampedProtein * 100).roundToInt()}%)"
                )

                // Fat slider
                MacroSlider(
                    label     = "TŁUSZCZE",
                    value     = clampedFat,
                    onValueChange = { fatPct = it },
                    valueRange = 0.1f..(0.9f - clampedProtein).coerceAtLeast(0.1f),
                    color     = Color(0xFFFF9800),
                    display   = "${fatG}g  (${(clampedFat * 100).roundToInt()}%)"
                )

                // Carbs (auto)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("WĘGLE (auto)", fontSize = 11.sp, color = Color(0xFF9E9E9E), letterSpacing = 1.sp)
                    Text("${carbG}g  (${(carbPct * 100).roundToInt()}%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FC3F7))
                }
                LinearProgressIndicator(
                    progress = { carbPct },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color(0xFF4FC3F7), trackColor = Color(0xFF2A2A2A)
                )

                // Info text
                Text(
                    "⚠ Kalorie = B×4 + T×9 + W×4. Wynikowe kalorie mogą się minimalnie różnić od celu przez zaokrąglenie gramów.",
                    fontSize = 10.sp, color = Color(0xFF616161)
                )

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("ANULUJ", color = Color(0xFF9E9E9E)) }
                    Button(
                        onClick = {
                            onSave(plan.copy(
                                targetCalories = actualKcal,
                                targetProtein  = proteinG,
                                targetFats     = fatG,
                                targetCarbs    = carbG
                            ))
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black)
                    ) { Text("ZAPISZ", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun MacroSlider(label: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, color: Color, display: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E), letterSpacing = 1.sp)
            Text(display, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value, onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color, inactiveTrackColor = Color(0xFF2A2A2A))
        )
    }
}

@Composable
private fun MacroSummaryPill(label: String, value: Int, unit: String, color: Color, pct: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Text("$value", fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
        Text(unit, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        if (pct.isNotEmpty()) Text(pct, fontSize = 9.sp, color = color.copy(alpha = 0.7f))
    }
}


// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
}

@Composable
private fun MacroChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun BodyRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PlanCard(plan: NutritionPlanEntity, isActive: Boolean, onActivate: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val phase = runCatching { PhaseType.valueOf(plan.phase) }.getOrNull()
    Box(modifier = Modifier.fillMaxWidth()
        .background(if (isActive) Success.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .border(1.dp, if (isActive) Success.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(plan.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (isActive) Text("AKTYWNY", fontSize = 9.sp, color = Success, fontWeight = FontWeight.Bold)
                }
                Text("${phase?.emoji ?: ""} ${phase?.label ?: plan.phase}  •  ${plan.targetCalories} kcal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!isActive) {
                Button(onClick = onActivate, shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary.copy(alpha = 0.15f), contentColor = AccentPrimary)
                ) { Text("Aktywuj", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = AccentPrimary.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun NewPlanDialog(initialInput: OnboardingInput, onDismiss: () -> Unit, onSave: (OnboardingInput) -> Unit) {
    var input by remember { mutableStateOf(initialInput) }
    var step  by remember { mutableStateOf(0) }
    val useCase = remember { com.example.macrotrainingtracker.domain.usecase.CalculateNutritionTargetsUseCase() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp)).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (step == 0) "Wybierz fazę" else if (step == 1) "Dane" else "Podsumowanie", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                when (step) {
                    0 -> PhaseSelectionForDialog(input = input, onInputChange = { input = it })
                    1 -> BodyDataStep(input = input, onInputChange = { input = it })
                    2 -> {
                        val result = remember(input) {
                            val age = input.age.toIntOrNull() ?: return@remember null
                            val h = input.heightCm.toIntOrNull() ?: return@remember null
                            val w = input.weightKg.toDoubleOrNull() ?: return@remember null
                            val t = input.targetWeightKg.toDoubleOrNull() ?: w
                            useCase.execute(input.gender, age, h, w, t, input.palLevel, input.phase, input.modifierKcal.toInt())
                        }
                        SummaryStep(input = input, result = result)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (step > 0) OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("WSTECZ") }
                    Button(
                        onClick = { if (step < 2) step++ else onSave(input) },
                        modifier = Modifier.weight(1f),
                        enabled = when (step) {
                            0 -> input.planName.isNotBlank()
                            1 -> input.age.isNotBlank() && input.heightCm.isNotBlank() && input.weightKg.isNotBlank()
                            else -> true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black)
                    ) { Text(if (step == 2) "ZAPISZ" else "DALEJ", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun PhaseSelectionForDialog(input: OnboardingInput, onInputChange: (OnboardingInput) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = input.planName, onValueChange = { onInputChange(input.copy(planName = it)) }, label = { Text("NAZWA PLANU") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = Color(0xFF2A2A2A), focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        PhaseType.entries.forEach { phase ->
            val isSelected = input.phase == phase
            Row(modifier = Modifier.fillMaxWidth().background(if (isSelected) Color(0xFF1A1A1A) else Color(0xFF121212), RoundedCornerShape(10.dp)).border(1.5.dp, if (isSelected) AccentPrimary else Color(0xFF2A2A2A), RoundedCornerShape(10.dp)).clickable { onInputChange(input.copy(phase = phase)) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(phase.emoji, fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(phase.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) AccentPrimary else Color.White)
                    Text(phase.description, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }
        }
    }
}

@Composable
private fun EditBodyDialog(body: UserBodyEntity?, onDismiss: () -> Unit, onSave: (OnboardingInput) -> Unit) {
    var input by remember {
        mutableStateOf(
            if (body != null) OnboardingInput(
                gender = runCatching { Gender.valueOf(body.gender) }.getOrDefault(Gender.MALE),
                age = body.age.toString(),
                heightCm = body.heightCm.toString(),
                weightKg = body.weightKg.toString(),
                palLevel = runCatching { PalLevel.valueOf(body.palLevel) }.getOrDefault(PalLevel.LIGHTLY_ACTIVE)
            ) else OnboardingInput()
        )
    }
    var showPalDialog by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp)).padding(16.dp)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Edytuj parametry ciała", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Gender.entries.forEach { g ->
                        val isSel = input.gender == g
                        Box(modifier = Modifier.weight(1f).background(if (isSel) AccentPrimary.copy(alpha = 0.15f) else Color(0xFF121212), RoundedCornerShape(10.dp)).border(1.5.dp, if (isSel) AccentPrimary else Color(0xFF2A2A2A), RoundedCornerShape(10.dp)).clickable { input = input.copy(gender = g) }.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(if (g == Gender.MALE) "♂ Mężczyzna" else "♀ Kobieta", color = if (isSel) AccentPrimary else Color.White, fontSize = 13.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                BodyNumberField("WIEK (lata)", input.age) { input = input.copy(age = it) }
                BodyNumberField("WZROST (cm)", input.heightCm) { input = input.copy(heightCm = it) }
                BodyNumberField("WAGA (kg)", input.weightKg) { input = input.copy(weightKg = it) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("POZIOM PAL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showPalDialog = true }) { Text("?", color = AccentPrimary) }
                }
                PalLevel.entries.forEach { pal ->
                    val isSel = input.palLevel == pal
                    Row(modifier = Modifier.fillMaxWidth().background(if (isSel) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).border(1.dp, if (isSel) AccentPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)).clickable { input = input.copy(palLevel = pal) }.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RadioButton(selected = isSel, onClick = { input = input.copy(palLevel = pal) }, colors = RadioButtonDefaults.colors(selectedColor = AccentPrimary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant))
                        Column {
                            Text(pal.label, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) AccentPrimary else MaterialTheme.colorScheme.onSurface)
                            Text(pal.shortDesc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("ANULUJ") }
                    Button(onClick = { onSave(input) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black)) { Text("ZAPISZ", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
    if (showPalDialog) PalInfoDialog(onDismiss = { showPalDialog = false })
}
