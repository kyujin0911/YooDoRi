package kr.ac.tukorea.whereareu.domain.safearea

import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker

data class SafeAreaMetaData(
    val settingMarker: Marker = Marker(),
    val settingCircleOverlay: CircleOverlay = CircleOverlay(),
    val markers: MutableList<Marker> = mutableListOf(),
    val circleOverlays: MutableList<CircleOverlay> = mutableListOf(),
    var isSettingSafeArea: Boolean = false
)
