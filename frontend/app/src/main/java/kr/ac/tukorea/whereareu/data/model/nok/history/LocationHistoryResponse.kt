package kr.ac.tukorea.whereareu.data.model.nok.history

data class LocationHistoryResponse(
    val locationHistory: List<LocationHistoryDto>
){
    constructor(): this(listOf(LocationHistoryDto()))
}
