package com.cramsan.coroutineperf.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ResponseDao {
    @Query("SELECT * FROM response")
    fun getAll(): List<Response>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(response: Response)

    @Delete
    fun delete(response: Response)

    @Query("DELETE FROM response")
    fun deleteAll()

    @Query("SELECT * FROM response")
    suspend fun getAllSuspend(): List<Response>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuspend(response: Response)

    @Delete
    suspend fun deleteSuspend(response: Response)

    @Query("DELETE FROM response")
    suspend fun deleteAllSuspend()
}