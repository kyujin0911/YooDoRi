package kr.ac.tukorea.whereareu.domain.home

import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker

data class PredictMetaData(
    val markers: MutableList<Marker> = mutableListOf(),
    val circleOverlay: CircleOverlay = CircleOverlay(),
    val safeMarkers: MutableList<Marker> = mutableListOf(),
    val safeCircleOverlays: MutableList<CircleOverlay> = mutableListOf(),
)
