package com.cramsan.coroutineperf.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Response::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ResponseDao
}