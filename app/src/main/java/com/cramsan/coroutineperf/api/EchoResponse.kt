package com.cramsan.coroutineperf.api

import com.squareup.moshi.Json

data class EchoResponse(
    @field:Json(name = "success") val success: String
)