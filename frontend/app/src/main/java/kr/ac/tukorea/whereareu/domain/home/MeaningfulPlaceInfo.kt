package kr.ac.tukorea.whereareu.domain.home

import kr.ac.tukorea.whereareu.data.model.nok.home.TimeInfo

data class MeaningfulPlaceInfo(
    val address: String,
    val timeInfo: List<TimeInfo>,
    val latitude: Double,
    val longitude: Double,
    var isExpanded: Boolean = false,
    var policeStationInfo: List<PoliceStationInfo>
)
