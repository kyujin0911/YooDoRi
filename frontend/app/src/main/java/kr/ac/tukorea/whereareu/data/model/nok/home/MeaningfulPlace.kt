package kr.ac.tukorea.whereareu.data.model.nok.home

import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo

data class MeaningfulPlace(
    val address: String,
    val timeInfo: List<TimeInfo>,
    val latitude: Double,
    val longitude: Double,
    val policeStationInfo: List<PoliceStationInfo>
)
