package kr.ac.tukorea.whereareu.util.extension

import android.content.Context
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

fun Marker.setMarker(
    latLng: LatLng,
    markerIconColor: OverlayImage,
    text: String,
    naverMap: NaverMap?,
    captionWidth: Int = 400
) {
    if (naverMap == null) {
        return
    }
    with(this) {
        position = latLng
        icon = markerIconColor
        captionText = text
        captionRequestedWidth = captionWidth
        map = naverMap
    }
}

fun Marker.setMarkerWithInfoWindow(
    context: Context,
    latLng: LatLng,
    markerIconColor: OverlayImage,
    markerText: String,
    naverMap: NaverMap?,
    infoText: String,
    captionWidth: Int = 400
) {
    this.setMarker(latLng, markerIconColor, markerText, naverMap, captionWidth)
    InfoWindow().apply {
        setAdapter(context, infoText)
        open(this@setMarkerWithInfoWindow)
    }
}

fun InfoWindow.setAdapter(context: Context, text: String) {
    this.adapter = object : InfoWindow.DefaultTextAdapter(context) {
        override fun getText(infoWindow: InfoWindow): CharSequence {
            return text
        }
    }
}