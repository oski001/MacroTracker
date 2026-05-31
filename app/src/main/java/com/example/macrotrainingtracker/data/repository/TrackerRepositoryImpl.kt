package com.example.macrotrainingtracker.data.repository

import com.example.macrotrainingtracker.data.local.AppDatabase
import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import com.example.macrotrainingtracker.data.local.entity.NutritionPlanEntity
import com.example.macrotrainingtracker.data.local.entity.UserBodyEntity
import com.example.macrotrainingtracker.data.local.model.DailyMacroAggregate
import kotlinx.coroutines.flow.Flow

class TrackerRepositoryImpl(private val database: AppDatabase) : TrackerRepository {

    override fun observeBody(): Flow<UserBodyEntity?> = database.userBodyDao().observe()
    override suspend fun upsertBody(body: UserBodyEntity) = database.userBodyDao().upsert(body)
    override suspend fun getBody(): UserBodyEntity? = database.userBodyDao().get()

    override fun observeAllPlans(): Flow<List<NutritionPlanEntity>> = database.nutritionPlanDao().observeAll()
    override fun observeActivePlan(): Flow<NutritionPlanEntity?> = database.nutritionPlanDao().observeActive()
    override suspend fun insertPlan(plan: NutritionPlanEntity): Long = database.nutritionPlanDao().insert(plan)
    override suspend fun updatePlan(plan: NutritionPlanEntity) = database.nutritionPlanDao().update(plan)
    override suspend fun deletePlan(id: Long) = database.nutritionPlanDao().deleteById(id)
    override suspend fun setActivePlan(id: Long) {
        database.nutritionPlanDao().deactivateAll()
        database.nutritionPlanDao().activateById(id)
    }

    override fun observeFoodProducts(): Flow<List<FoodProductEntity>> = database.foodProductDao().observeAll()
    override suspend fun addFoodProduct(product: FoodProductEntity) { database.foodProductDao().insert(product) }
    override suspend fun deleteCustomFoodProduct(id: Long) = database.foodProductDao().deleteCustomById(id)
    override suspend fun setFavorite(id: Long, isFavorite: Boolean) = database.foodProductDao().setFavorite(id, isFavorite)

    override suspend fun addFoodLog(log: FoodLogEntity) = database.foodLogDao().insert(log)
    override suspend fun updateFoodLog(log: FoodLogEntity) = database.foodLogDao().update(log)
    override suspend fun deleteFoodLog(id: Long) = database.foodLogDao().deleteById(id)
    override fun observeDailyFoodLogs(date: String): Flow<List<FoodLogEntity>> = database.foodLogDao().observeByDate(date)
    override fun observeDailyFoodAggregate(date: String): Flow<DailyMacroAggregate?> = database.foodLogDao().observeDailyAggregate(date)
    override fun observeAllLoggedDates(): Flow<List<String>> = database.foodLogDao().observeAllDates()
}
