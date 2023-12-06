package com.htnguyen.weatherapp.model


class WeatherDaily(
    var dt: Long,
    var sunrise: Long,
    var sunset: Long,
    var tempMin: Float,
    var temMax: Float,
    var humidity: Float,
    var weatherIcon: String

)