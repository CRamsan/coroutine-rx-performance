package com.cramsan.coroutineperf.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Response(
    @PrimaryKey val uid: Int,
    val status: String,
)