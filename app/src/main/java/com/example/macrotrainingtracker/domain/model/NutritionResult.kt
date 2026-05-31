package com.example.macrotrainingtracker.domain.model

data class NutritionResult(
    val bmi: Double,
    val bfPercent: Double,
    val fatMassKg: Double,
    val lbmKg: Double,
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val fatsGrams: Int,
    val carbsGrams: Int,
    val daysToGoal: Int? = null,
    val weeksToGoal: Double? = null
) {
    val bmiCategory: String get() = when {
        bmi < 18.5 -> "Niedowaga"
        bmi < 25.0 -> "Norma"
        bmi < 30.0 -> "Nadwaga"
        else -> "Otyłość"
    }
}

