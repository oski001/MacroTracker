package com.example.macrotrainingtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val timestamp: Long,
    val sourceName: String,
    val grams: Double,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)

