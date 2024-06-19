package kr.ac.tukorea.whereareu.domain.home

data class UserMeaningfulPlaceInfo(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    var isExpanded: Boolean = false,
    var policeStationInfo: List<PoliceStationInfo> = emptyList()
)
