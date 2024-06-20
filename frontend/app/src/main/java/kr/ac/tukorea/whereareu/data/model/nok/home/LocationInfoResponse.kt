package kr.ac.tukorea.whereareu.data.model.nok.home

import kr.ac.tukorea.whereareu.domain.home.LocationInfo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationInfoResponse(
    val battery: Int,
    val isGpsOn: Boolean,
    val isInternetOn: Boolean,
    val isRingstoneOn: Int,
    val latitude: Double,
    val longitude: Double,
    val userStatus: String,
    val bearing: Float,
    val currentSpeed: Float
): Parcelable{
    constructor(): this(100, true, true, 0, 37.5666103, 126.9783882, "", 0f, 0f)
    fun toModel(isPredicted: Boolean): LocationInfo {
        return LocationInfo(battery, isGpsOn, isInternetOn, isRingstoneOn, latitude, longitude, userStatus, bearing, currentSpeed, isPredicted)
    }
}
