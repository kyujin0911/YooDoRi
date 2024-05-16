package kr.ac.tukorea.whereareu.domain.home

import com.naver.maps.geometry.LatLng
import kr.ac.tukorea.whereareu.data.model.nok.home.PoliceStationInfoResponse
import kr.ac.tukorea.whereareu.data.model.nok.home.TimeInfo

data class MeaningfulPlaceInfo(
    val address: String,
    val timeInfo: List<TimeInfo>,
    val latLng: LatLng,
    var isExpanded: Boolean = false,
    var policeStationInfo: List<PoliceStationInfo>
)
