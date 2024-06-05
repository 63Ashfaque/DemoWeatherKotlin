package com.ashfaque.demoweatherkotlin.model

data class WeatherResponse(
    val current: Current,
    val location: Location
)