package com.example.macrotrainingtracker.ui.model

import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import com.example.macrotrainingtracker.data.local.entity.NutritionPlanEntity
import com.example.macrotrainingtracker.data.local.entity.UserBodyEntity
import com.example.macrotrainingtracker.domain.model.Gender
import com.example.macrotrainingtracker.domain.model.PalLevel
import com.example.macrotrainingtracker.domain.model.PhaseType

data class OnboardingInput(
    val gender: Gender = Gender.MALE,
    val age: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val targetWeightKg: String = "",
    val palLevel: PalLevel = PalLevel.LIGHTLY_ACTIVE,
    val phase: PhaseType = PhaseType.CUT,
    val modifierKcal: Float = 500f,
    val planName: String = ""
)

data class FoodTrackerUiState(
    val products: List<FoodProductEntity> = emptyList(),
    val todayLogs: List<FoodLogEntity> = emptyList(),
    val activePlan: NutritionPlanEntity? = null,
    val daysSinceStart: Int = 0,
    val shouldShowRecalcReminder: Boolean = false
)

data class LogsUiState(
    val allLoggedDates: Set<String> = emptySet(),
    val selectedDateLogs: List<FoodLogEntity> = emptyList()
)

data class MenuUiState(
    val body: UserBodyEntity? = null,
    val plans: List<NutritionPlanEntity> = emptyList(),
    val activePlan: NutritionPlanEntity? = null,
    val daysSinceStart: Int = 0
)
