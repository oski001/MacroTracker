package com.example.macrotrainingtracker.domain.usecase

import com.example.macrotrainingtracker.domain.model.Gender
import com.example.macrotrainingtracker.domain.model.NutritionResult
import com.example.macrotrainingtracker.domain.model.PalLevel
import com.example.macrotrainingtracker.domain.model.PhaseType
import kotlin.math.roundToInt

class CalculateNutritionTargetsUseCase {

    fun execute(
        gender: Gender,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        targetWeightKg: Double,
        palLevel: PalLevel,
        phase: PhaseType,
        modifierKcal: Int   // deficit for CUT, surplus for BULK
    ): NutritionResult {

        // Step 1: BMI and Body Fat
        val bmi = weightKg / ((heightCm / 100.0) * (heightCm / 100.0))
        val genderValue = if (gender == Gender.MALE) 1.0 else 0.0
        val bfPercent = ((1.20 * bmi) + (0.23 * age) - (10.8 * genderValue) - 5.4).coerceAtLeast(1.0)
        val fatMassKg = weightKg * (bfPercent / 100.0)
        val lbmKg = weightKg - fatMassKg

        // Step 2: BMR and TDEE (Mifflin-St Jeor)
        val bmr = when (gender) {
            Gender.MALE   -> (10 * weightKg + 6.25 * heightCm - 5 * age + 5).roundToInt()
            Gender.FEMALE -> (10 * weightKg + 6.25 * heightCm - 5 * age - 161).roundToInt()
        }
        val tdee = (bmr * palLevel.multiplier).roundToInt()

        // Step 3: Target calories by phase
        val targetCalories = when (phase) {
            PhaseType.CUT         -> (tdee - modifierKcal).coerceAtLeast(bmr)
            PhaseType.MAINTENANCE -> tdee
            PhaseType.BULK        -> tdee + modifierKcal
        }

        // Macros by phase
        // Protein: weight × 2.2 g for every phase
        val proteinGrams = (weightKg * 2.2).roundToInt()
        val fatsGrams = when (phase) {
            PhaseType.CUT         -> (weightKg * 0.8).roundToInt()
            PhaseType.MAINTENANCE -> (weightKg * 1.0).roundToInt()
            PhaseType.BULK        -> (weightKg * 1.1).roundToInt()
        }
        val proteinCalories = proteinGrams * 4
        val fatsCalories    = fatsGrams * 9
        val carbsGrams      = ((targetCalories - proteinCalories - fatsCalories) / 4.0)
            .roundToInt().coerceAtLeast(0)

        // Step 4: Time to goal
        val (daysToGoal, weeksToGoal) = when (phase) {
            PhaseType.CUT -> {
                val diff = weightKg - targetWeightKg
                if (modifierKcal > 0 && diff > 0) {
                    val d = ((diff * 7700) / modifierKcal).roundToInt()
                    Pair(d, d / 7.0)
                } else Pair(null, null)
            }
            PhaseType.BULK -> {
                val diff = targetWeightKg - weightKg
                if (modifierKcal > 0 && diff > 0) {
                    val d = ((diff * 7700) / modifierKcal).roundToInt()
                    Pair(d, d / 7.0)
                } else Pair(null, null)
            }
            PhaseType.MAINTENANCE -> Pair(null, null)
        }

        return NutritionResult(
            bmi           = bmi,
            bfPercent     = bfPercent,
            fatMassKg     = fatMassKg,
            lbmKg         = lbmKg,
            bmr           = bmr,
            tdee          = tdee,
            targetCalories = targetCalories,
            proteinGrams  = proteinGrams,
            fatsGrams     = fatsGrams,
            carbsGrams    = carbsGrams,
            daysToGoal    = daysToGoal,
            weeksToGoal   = weeksToGoal
        )
    }
}
