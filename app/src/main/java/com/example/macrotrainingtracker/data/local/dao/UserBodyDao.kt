package com.example.macrotrainingtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.macrotrainingtracker.data.local.entity.UserBodyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBodyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: UserBodyEntity)

    @Query("SELECT * FROM user_body WHERE id = 1")
    fun observe(): Flow<UserBodyEntity?>

    @Query("SELECT * FROM user_body WHERE id = 1")
    suspend fun get(): UserBodyEntity?
}

