package com.example.bustops

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HslApi {

    @POST("routing/v1/routers/hsl/index/graphql")
    @Headers("Content-Type: application/graphql")
    fun sentStopData(
        @Body stopPost: String
    ):Call<String>
}
