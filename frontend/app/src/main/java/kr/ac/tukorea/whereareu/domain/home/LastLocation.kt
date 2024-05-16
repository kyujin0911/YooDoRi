package kr.ac.tukorea.whereareu.domain.home

import com.naver.maps.geometry.LatLng

data class LastLocation(
    val latLng: LatLng,
    val address: String
)
