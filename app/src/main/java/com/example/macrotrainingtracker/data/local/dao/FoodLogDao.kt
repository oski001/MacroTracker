package com.example.macrotrainingtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.macrotrainingtracker.data.local.entity.FoodLogEntity
import com.example.macrotrainingtracker.data.local.model.DailyMacroAggregate
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodLogEntity)

    @Update
    suspend fun update(item: FoodLogEntity)

    @Delete
    suspend fun delete(item: FoodLogEntity)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY timestamp ASC")
    fun observeByDate(date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT DISTINCT date FROM food_logs ORDER BY date DESC")
    fun observeAllDates(): Flow<List<String>>

    @Query("""
        SELECT
            SUM(calories) AS calories,
            SUM(protein) AS protein,
            SUM(carbs) AS carbs,
            SUM(fats) AS fats
        FROM food_logs
        WHERE date = :date
        """)
    fun observeDailyAggregate(date: String): Flow<DailyMacroAggregate?>
}
