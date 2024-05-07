package kr.ac.tukorea.whereareu.data.model.nok.history

data class LocationHistory(
    val latitude: Double,
    val longitude: Double,
    val time: String
){
    constructor(): this(0.0, 0.0, "")
}
