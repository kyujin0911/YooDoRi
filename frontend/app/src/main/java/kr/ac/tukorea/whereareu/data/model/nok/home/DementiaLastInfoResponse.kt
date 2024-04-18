package kr.ac.tukorea.whereareu.data.model.nok.home

data class DementiaLastInfoResponse(
    val averageSpeed: Float,
    val lastLatitude: Double,
    val lastLongitude: Double,
){
    constructor(): this(0f, 0.0, 0.0)
}