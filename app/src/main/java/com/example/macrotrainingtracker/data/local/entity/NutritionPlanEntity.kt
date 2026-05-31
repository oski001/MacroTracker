package com.example.macrotrainingtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutrition_plans")
data class NutritionPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phase: String,            // PhaseType.name
    val targetWeightKg: Double = 0.0,
    val modifierKcal: Int = 500,  // deficit (CUT) or surplus (BULK) in kcal
    val targetCalories: Int,
    val targetProtein: Int,
    val targetFats: Int,
    val targetCarbs: Int,
    val daysToGoal: Int = 0,      // 0 = not applicable (MAINTENANCE)
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

