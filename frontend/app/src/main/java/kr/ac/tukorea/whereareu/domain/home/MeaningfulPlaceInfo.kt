package kr.ac.tukorea.whereareu.domain.home

data class MeaningfulPlaceInfo(
    val address: String,
    val meaningfulPlaceListInfo: List<MeaningfulPlaceListInfo>,
    val latitude: Double,
    val longitude: Double,
    var isExpanded: Boolean = false,
    var policeStationInfo: List<PoliceStationInfo> = emptyList()
)
