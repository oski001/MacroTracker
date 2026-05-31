package com.example.macrotrainingtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.example.macrotrainingtracker.domain.model.Gender
import com.example.macrotrainingtracker.domain.model.PalLevel
import com.example.macrotrainingtracker.domain.model.PhaseType
import com.example.macrotrainingtracker.domain.usecase.CalculateNutritionTargetsUseCase
import com.example.macrotrainingtracker.ui.model.OnboardingInput
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.example.macrotrainingtracker.ui.theme.Success
import com.example.macrotrainingtracker.ui.theme.Warning
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(onSave: (OnboardingInput) -> Unit) {
    var step by remember { mutableStateOf(0) }
    var input by remember { mutableStateOf(OnboardingInput()) }
    val useCase = remember { CalculateNutritionTargetsUseCase() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Step indicator
            StepIndicator(currentStep = step, totalSteps = 3)

            when (step) {
                0 -> PhaseSelectionStep(input = input, onInputChange = { input = it })
                1 -> BodyDataStep(input = input, onInputChange = { input = it })
                2 -> {
                    val result = remember(input) {
                        val age    = input.age.toIntOrNull()
                        val height = input.heightCm.toIntOrNull()
                        val weight = input.weightKg.toDoubleOrNull()
                        val target = input.targetWeightKg.toDoubleOrNull() ?: (weight ?: 70.0)
                        if (age != null && height != null && weight != null) {
                            useCase.execute(
                                gender = input.gender, age = age, heightCm = height,
                                weightKg = weight, targetWeightKg = target,
                                palLevel = input.palLevel, phase = input.phase,
                                modifierKcal = input.modifierKcal.roundToInt()
                            )
                        } else null
                    }
                    SummaryStep(input = input, result = result)
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) { Text("WSTECZ") }
                }

                val isNextEnabled = when (step) {
                    0 -> input.planName.isNotBlank()
                    1 -> input.age.isNotBlank() && input.heightCm.isNotBlank() &&
                            input.weightKg.isNotBlank() &&
                            (input.phase == PhaseType.MAINTENANCE || input.targetWeightKg.isNotBlank())
                    else -> true
                }

                Button(
                    onClick = {
                        if (step < 2) step++
                        else onSave(input)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isNextEnabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        if (step == 2) "ZAPISZ PLAN" else "DALEJ",
                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "KONFIGURACJA",
            fontSize = 11.sp, fontWeight = FontWeight.Medium,
            color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp
        )
        Text(
            when (currentStep) {
                0 -> "Wybierz cel"
                1 -> "Dane ciała"
                else -> "Podsumowanie planu"
            },
            fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalSteps) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == currentStep) 28.dp else 8.dp, 8.dp)
                        .background(
                            if (i <= currentStep) AccentPrimary else Color(0xFF2A2A2A),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

@Composable
internal fun PhaseSelectionStep(input: OnboardingInput, onInputChange: (OnboardingInput) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Plan name
        OutlinedTextField(
            value = input.planName,
            onValueChange = { onInputChange(input.copy(planName = it)) },
            label = { Text("NAZWA PLANU", fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp) },
            placeholder = { Text("np. Redukcja wiosna 2026", color = Color(0xFF616161)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPrimary,
                unfocusedBorderColor = Color(0xFF2A2A2A),
                focusedTextColor = Color.White, unfocusedTextColor = Color.White
            )
        )

        Text("WYBIERZ FAZĘ", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp)

        PhaseType.entries.forEach { phase ->
            val isSelected = input.phase == phase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) Color(0xFF1A1A1A) else Color(0xFF121212),
                        RoundedCornerShape(14.dp)
                    )
                    .border(
                        2.dp,
                        if (isSelected) AccentPrimary else Color(0xFF2A2A2A),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onInputChange(input.copy(phase = phase)) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(phase.emoji, fontSize = 28.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(phase.label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (isSelected) AccentPrimary else Color.White)
                        Text(phase.description, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier.size(20.dp).background(AccentPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("✓", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BodyDataStep(input: OnboardingInput, onInputChange: (OnboardingInput) -> Unit) {
    var showPalDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Gender selector
        Text("PŁEĆ", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Gender.entries.forEach { g ->
                val isSelected = input.gender == g
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color(0xFF121212), RoundedCornerShape(12.dp))
                        .border(1.5.dp, if (isSelected) AccentPrimary else Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                        .clickable { onInputChange(input.copy(gender = g)) }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (g == Gender.MALE) "♂ Mężczyzna" else "♀ Kobieta",
                        color = if (isSelected) AccentPrimary else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }

        BodyNumberField("WIEK (lata)", input.age) { onInputChange(input.copy(age = it)) }
        BodyNumberField("WZROST (cm)", input.heightCm) { onInputChange(input.copy(heightCm = it)) }
        BodyNumberField("AKTUALNA WAGA (kg)", input.weightKg) { onInputChange(input.copy(weightKg = it)) }

        if (input.phase != PhaseType.MAINTENANCE) {
            BodyNumberField(
                if (input.phase == PhaseType.CUT) "WAGA DOCELOWA (kg)" else "WAGA DOCELOWA (kg)",
                input.targetWeightKg
            ) { onInputChange(input.copy(targetWeightKg = it)) }
        }

        // PAL Selector
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("POZIOM AKTYWNOŚCI (PAL)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { showPalDialog = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Info, contentDescription = "Info o PAL", tint = AccentPrimary)
            }
        }

        PalLevel.entries.forEach { pal ->
            val isSelected = input.palLevel == pal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) Color(0xFF1A1A1A) else Color(0xFF0F0F0F), RoundedCornerShape(10.dp))
                    .border(1.dp, if (isSelected) AccentPrimary else Color(0xFF2A2A2A), RoundedCornerShape(10.dp))
                    .clickable { onInputChange(input.copy(palLevel = pal)) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onInputChange(input.copy(palLevel = pal)) },
                    colors = RadioButtonDefaults.colors(selectedColor = AccentPrimary, unselectedColor = Color(0xFF616161))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(pal.label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) AccentPrimary else Color.White)
                    Text(pal.shortDesc, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                }
            }
        }

        // Modifier (deficit / surplus) – only for CUT and BULK
        if (input.phase != PhaseType.MAINTENANCE) {
            val label = if (input.phase == PhaseType.CUT) "DEFICYT KALORYCZNY" else "NADWYŻKA KALORYCZNA"
            val modVal = input.modifierKcal.roundToInt()
            Text("$label: $modVal kcal", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp)
            Slider(
                value = input.modifierKcal,
                onValueChange = { onInputChange(input.copy(modifierKcal = it)) },
                valueRange = 300f..800f,
                steps = 19,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentPrimary,
                    activeTrackColor = AccentPrimary,
                    inactiveTrackColor = Color(0xFF2A2A2A)
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("300 kcal", fontSize = 10.sp, color = Color(0xFF616161))
                Text("800 kcal", fontSize = 10.sp, color = Color(0xFF616161))
            }
        }
    }

    if (showPalDialog) {
        PalInfoDialog(onDismiss = { showPalDialog = false })
    }
}

@Composable
internal fun BodyNumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPrimary,
            unfocusedBorderColor = Color(0xFF2A2A2A),
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedLabelColor = AccentPrimary, unfocusedLabelColor = Color(0xFF9E9E9E)
        )
    )
}

@Composable
internal fun SummaryStep(input: OnboardingInput, result: com.example.macrotrainingtracker.domain.model.NutritionResult?) {
    if (result == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) { Text("Uzupełnij dane w poprzednim kroku.", color = Color(0xFF9E9E9E), textAlign = TextAlign.Center) }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Plan header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(14.dp))
                .border(1.dp, AccentPrimary, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    input.planName.ifBlank { "Mój Plan" },
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                Text(
                    "${input.phase.emoji} ${input.phase.label}",
                    fontSize = 13.sp, color = AccentPrimary, fontWeight = FontWeight.Medium
                )
            }
        }

        // Body composition
        SummaryCard("SKŁAD CIAŁA") {
            SummaryRow("BMI", "%.1f".format(result.bmi), result.bmiCategory, colorForBmi(result.bmi))
            SummaryRow("Tkanka tłuszczowa*", "%.1f%%".format(result.bfPercent), "szacunek z BMI", Color(0xFF9E9E9E))
            SummaryRow("Masa tłuszczu", "%.1f kg".format(result.fatMassKg))
            SummaryRow("Beztłuszczowa masa (LBM)", "%.1f kg".format(result.lbmKg))
        }

        // Energy
        SummaryCard("ENERGETYKA") {
            SummaryRow("BMR (podstawowa przemiana)", "${result.bmr} kcal")
            SummaryRow("TDEE (z aktywnością)", "${result.tdee} kcal")
            val modLabel = when (input.phase) {
                PhaseType.CUT -> "deficyt ${input.modifierKcal.roundToInt()} kcal"
                PhaseType.BULK -> "nadwyżka ${input.modifierKcal.roundToInt()} kcal"
                PhaseType.MAINTENANCE -> "brak modyfikatora"
            }
            SummaryRow("Cel kaloryczny", "${result.targetCalories} kcal", modLabel, AccentPrimary)
        }

        // Macros
        SummaryCard("MAKROSKŁADNIKI / DZIEŃ") {
            val totalCals = result.proteinGrams * 4 + result.fatsGrams * 9 + result.carbsGrams * 4
            MacroSummaryRow("Białko", result.proteinGrams, result.proteinGrams * 4, totalCals)
            MacroSummaryRow("Tłuszcze", result.fatsGrams, result.fatsGrams * 9, totalCals)
            MacroSummaryRow("Węglowodany", result.carbsGrams, result.carbsGrams * 4, totalCals)
        }

        // Time to goal
        if (result.daysToGoal != null && result.weeksToGoal != null) {
            SummaryCard("CZAS DO CELU") {
                val weeks = result.weeksToGoal!!
                SummaryRow("Dni", "${result.daysToGoal} dni")
                SummaryRow("Tygodnie", "~%.1f tygodni".format(weeks))
                SummaryRow("Waga docelowa", "${input.targetWeightKg} kg")
            }
        }

        // BF note
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF0F0F0F), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("*Jak mierzyć tkankę tłuszczową?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentPrimary)
                Text(
                    "Kalkulator podaje wartość szacunkową opartą na BMI. Aby realnie śledzić spadek tkanki tłuszczowej, raz w tygodniu mierz centymetrem obwód pasa (na wysokości pępka) oraz bioder. Najlepszym darmowym miernikiem są też zdjęcia sylwetki (przód, tył, bok) co 2 tygodnie – zawsze w tym samym miejscu i świetle.",
                    fontSize = 11.sp, color = Color(0xFF9E9E9E), lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
internal fun SummaryCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFF121212), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E), letterSpacing = 1.5.sp)
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, subtitle: String? = null, valueColor: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(label, fontSize = 13.sp, color = Color(0xFF9E9E9E))
            if (subtitle != null) Text(subtitle, fontSize = 10.sp, color = valueColor.copy(alpha = 0.7f))
        }
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun MacroSummaryRow(label: String, grams: Int, kcal: Int, totalKcal: Int) {
    val pct = if (totalKcal > 0) (kcal * 100f / totalKcal).roundToInt() else 0
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = Color(0xFF9E9E9E), modifier = Modifier.weight(1f))
        Text("${grams}g", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.width(8.dp))
        Text("$kcal kcal", fontSize = 12.sp, color = Color(0xFF9E9E9E))
        Spacer(Modifier.width(8.dp))
        Text("$pct%", fontSize = 12.sp, color = AccentPrimary)
    }
}

private fun colorForBmi(bmi: Double): Color = when {
    bmi < 18.5 -> Warning
    bmi < 25.0 -> Success
    bmi < 30.0 -> Warning
    else -> Color(0xFFFF5252)
}

@Composable
internal fun PalInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Precyzyjne progi PAL", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                PalLevel.entries.forEach { pal ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF121212), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(pal.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentPrimary)
                        Text(pal.detailedDesc, fontSize = 11.sp, color = Color(0xFF9E9E9E), lineHeight = 16.sp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, contentColor = Color.Black)
                ) { Text("ZAMKNIJ", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
