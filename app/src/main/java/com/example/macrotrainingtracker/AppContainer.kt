package com.example.macrotrainingtracker

import android.content.Context
import com.example.macrotrainingtracker.data.local.AppDatabase
import com.example.macrotrainingtracker.data.repository.TrackerRepository
import com.example.macrotrainingtracker.data.repository.TrackerRepositoryImpl
import com.example.macrotrainingtracker.domain.usecase.CalculateNutritionTargetsUseCase

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)
    val repository: TrackerRepository = TrackerRepositoryImpl(database)
    val calculateNutritionTargetsUseCase = CalculateNutritionTargetsUseCase()
}
