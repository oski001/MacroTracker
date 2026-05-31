package com.example.macrotrainingtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.macrotrainingtracker.data.local.entity.FoodProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodProductDao {
    @Query("SELECT * FROM food_products ORDER BY name ASC")
    fun observeAll(): Flow<List<FoodProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodProductEntity): Long

    @Query("DELETE FROM food_products WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomById(id: Long)

    @Query("SELECT COUNT(*) FROM food_products")
    suspend fun count(): Int

    @Query("UPDATE food_products SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
