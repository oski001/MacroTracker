package com.example.macrotrainingtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_body")
data class UserBodyEntity(
    @PrimaryKey val id: Int = 1,
    val gender: String,           // "MALE" or "FEMALE"
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val palLevel: String,         // PalLevel.name
    val dayStartedAt: Long = System.currentTimeMillis(),
    val lastRecalcAt: Long = System.currentTimeMillis(),
    val recalcReminderSnoozedUntil: Long = 0L
)

