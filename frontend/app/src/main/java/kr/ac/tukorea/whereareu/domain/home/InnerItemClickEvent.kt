package kr.ac.tukorea.whereareu.domain.home

import com.naver.maps.geometry.LatLng

data class InnerItemClickEvent(
    val behavior: Int,
    val coord: LatLng
)
