package com.example.macrotrainingtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_products")
data class FoodProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val name: String,
    val category: String, // carbs, protein, fats, dairy, drinks, fruit
    val caloriesPerGram: Double,
    val proteinPerGram: Double,
    val carbsPerGram: Double,
    val fatsPerGram: Double
)

