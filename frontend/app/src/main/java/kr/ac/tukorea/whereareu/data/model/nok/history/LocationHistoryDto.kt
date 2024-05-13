package kr.ac.tukorea.whereareu.data.model.nok.history

data class LocationHistoryDto(
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val userStatus: String,
    val distance: Float
){
    constructor(): this(0.0, 0.0, "", "", 0f)
}
