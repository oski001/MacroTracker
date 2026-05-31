package com.example.macrotrainingtracker.domain.usecase

import com.example.macrotrainingtracker.domain.model.Gender
import com.example.macrotrainingtracker.domain.model.GoalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCasesTest {

    @Test
    fun cut_calories_never_below_bmr() {
        val useCase = CalculateNutritionTargetsUseCase()

        val result = useCase.execute(
            gender = Gender.FEMALE,
            age = 45,
            heightCm = 150,
            weightKg = 45.0,
            pal = 1.2,
            goalType = GoalType.CUT
        )

        assertTrue(result.targetCalories >= result.bmr)
    }

    @Test
    fun cardio_bonus_is_split_90_10_between_carbs_and_protein() {
        val useCase = CalculateCardioBonusUseCase()
        val result = useCase.execute(weightKg = 80.0, met = 8.0, durationMinutes = 60)

        assertEquals(320, result.bonusCalories)
        assertEquals(8, result.bonusProteinGrams)
        assertEquals(72, result.bonusCarbsGrams)
    }

    @Test
    fun epley_formula_is_applied() {
        val useCase = EstimateOneRepMaxUseCase()
        val result = useCase.execute(weightKg = 100.0, reps = 5)

        assertEquals(117, result)
    }
}

