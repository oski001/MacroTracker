package com.example.macrotrainingtracker.data.local.model

data class DailyMacroAggregate(
    val calories: Int?,
    val protein: Int?,
    val carbs: Int?,
    val fats: Int?
)

data class DailyCardioAggregate(
    val bonusCalories: Int?,
    val bonusProtein: Int?,
    val bonusCarbs: Int?
)

