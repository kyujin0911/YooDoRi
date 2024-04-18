package kr.ac.tukorea.whereareu.domain.home

data class MeaningfulPlaceInfo(
    val address: String,
    val timeInfo: List<TimeInfo>,
    val latitude: Double,
    val longitude: Double,
    var isExpanded: Boolean = false,
    var policeStationInfo: List<PoliceStationInfo> = emptyList()
)
