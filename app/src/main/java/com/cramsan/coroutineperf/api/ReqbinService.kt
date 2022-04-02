package com.cramsan.coroutineperf.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers

interface ReqbinService {

    @Headers("Cache-control: no-cache")
    @GET("echo/get/json")
    fun echo(): Single<EchoResponse>

    @Headers("Cache-control: no-cache")
    @GET("echo/get/json")
    suspend fun echoSuspend(): EchoResponse

}