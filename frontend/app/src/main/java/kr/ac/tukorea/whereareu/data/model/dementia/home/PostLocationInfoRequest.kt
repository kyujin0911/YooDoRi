package kr.ac.tukorea.whereareu.data.model.dementia.home

import java.io.Serializable

data class PostLocationInfoRequest(
    val dementiaKey: String,
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val date: String,
    val currentSpeed: Float,
    val accelerationSensor: List<Float>,
    val gyroSensor: List<Float>,
    val directionSensor: List<Float>,
    val lightSensor: List<Float>,
    val battery: Int,
    val isInternetOn: Boolean,
    val isGpsOn: Boolean,
    val isRingstoneOn: Int,
    val bearing: Float
): Serializable

