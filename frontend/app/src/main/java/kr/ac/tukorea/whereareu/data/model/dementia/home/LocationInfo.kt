package kr.ac.tukorea.whereareu.data.model.dementia.home

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class LocationInfo(
    val dementiaKey: String,
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val date: String,
    val currentSpeed: Float,
    val accelerationsensor: List<Float>,
    val gyrosensor: List<Float>,
    val directionsensor: List<Float>,
    val lightsensor: List<Float>,
    val battery: Int,
    val isInternetOn: Boolean,
    val isGpsOn: Boolean,
    val isRingstoneOn: Int,
    val bearing: Float
): Serializable

