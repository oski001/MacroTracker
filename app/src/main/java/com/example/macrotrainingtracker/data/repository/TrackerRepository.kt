package com.example.macrotrainingtracker.data.repository

import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import com.example.macrotrainingtracker.data.local.entity.NutritionPlanEntity
import com.example.macrotrainingtracker.data.local.entity.UserBodyEntity
import com.example.macrotrainingtracker.data.local.model.DailyMacroAggregate
import kotlinx.coroutines.flow.Flow

interface TrackerRepository {
    // Body profile
    fun observeBody(): Flow<UserBodyEntity?>
    suspend fun upsertBody(body: UserBodyEntity)
    suspend fun getBody(): UserBodyEntity?

    // Nutrition plans
    fun observeAllPlans(): Flow<List<NutritionPlanEntity>>
    fun observeActivePlan(): Flow<NutritionPlanEntity?>
    suspend fun insertPlan(plan: NutritionPlanEntity): Long
    suspend fun updatePlan(plan: NutritionPlanEntity)
    suspend fun deletePlan(id: Long)
    suspend fun setActivePlan(id: Long)

    // Food products
    fun observeFoodProducts(): Flow<List<FoodProductEntity>>
    suspend fun addFoodProduct(product: FoodProductEntity)
    suspend fun deleteCustomFoodProduct(id: Long)
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    // Food logs
    suspend fun addFoodLog(log: FoodLogEntity)
    suspend fun updateFoodLog(log: FoodLogEntity)
    suspend fun deleteFoodLog(id: Long)
    fun observeDailyFoodLogs(date: String): Flow<List<FoodLogEntity>>
    fun observeDailyFoodAggregate(date: String): Flow<DailyMacroAggregate?>
    fun observeAllLoggedDates(): Flow<List<String>>
}
