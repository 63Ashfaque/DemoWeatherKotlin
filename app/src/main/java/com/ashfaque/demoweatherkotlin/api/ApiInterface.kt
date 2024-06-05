package com.ashfaque.demoweatherkotlin.api

import com.ashfaque.demoweatherkotlin.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("/v1/current.json")
    suspend fun getWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi:String="yes"
    ): Response<WeatherResponse>
}