package kr.ac.tukorea.whereareu.data.model.nok.home

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
){
    constructor(): this(100, true, true, 0, 37.5666103, 126.9783882, "", 0f, 0f)
}
