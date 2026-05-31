package com.example.macrotrainingtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.macrotrainingtracker.data.local.entity.NutritionPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NutritionPlanEntity): Long

    @Update
    suspend fun update(item: NutritionPlanEntity)

    @Query("DELETE FROM nutrition_plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM nutrition_plans ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NutritionPlanEntity>>

    @Query("SELECT * FROM nutrition_plans WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<NutritionPlanEntity?>

    @Query("UPDATE nutrition_plans SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE nutrition_plans SET isActive = 1 WHERE id = :id")
    suspend fun activateById(id: Long)

    @Query("SELECT COUNT(*) FROM nutrition_plans")
    suspend fun count(): Int
}

