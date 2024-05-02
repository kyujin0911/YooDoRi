package kr.ac.tukorea.whereareu.data.model.nok.history

data class LocationHistoryResponse(
    val locationHistory: List<LocationHistory>
){
    constructor(): this(listOf(LocationHistory()))
}
