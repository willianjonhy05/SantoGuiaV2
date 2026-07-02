package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExameDao {
    @Query("SELECT * FROM exame_consciencia")
    fun getAllExameItens(): Flow<List<ExameItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itens: List<ExameItem>)

    @Update
    suspend fun updateItem(item: ExameItem)

    @Query("UPDATE exame_consciencia SET isChecked = 0")
    suspend fun resetAllCheckmarks()
}
