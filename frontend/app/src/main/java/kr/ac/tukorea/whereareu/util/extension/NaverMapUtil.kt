package kr.ac.tukorea.whereareu.util.extension

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.MarkerIcons
import kr.ac.tukorea.whereareu.R

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
    val zIndex = when(markerIconColor){
        MarkerIcons.YELLOW -> 10
        MarkerIcons.PINK -> 12
        MarkerIcons.GREEN -> 20
        MarkerIcons.BLUE -> 5
        MarkerIcons.RED -> 15
        else -> 0
    }
    with(this) {
        position = latLng
        icon = markerIconColor
        captionText = text
        captionRequestedWidth = captionWidth
        map = naverMap
        this.zIndex = zIndex
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
    val zIndex = when(markerIconColor){
        MarkerIcons.YELLOW -> 10
        MarkerIcons.GREEN -> 20
        MarkerIcons.BLUE -> 5
        MarkerIcons.RED -> 15
        else -> 0
    }
    this.zIndex = zIndex
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

fun PathOverlay.setPath(context: Context, latLngList: List<LatLng>, pathColor: Int, naverMap: NaverMap?){
    with(this){
        coords = latLngList
        width = 30
        color = ContextCompat.getColor(context, pathColor)
        patternImage = OverlayImage.fromResource(R.drawable.ic_arrow_up_white_24)
        patternInterval = 50
        outlineColor = Color.WHITE
        map = naverMap
    }
}

fun Marker.setInfoWindowText(context: Context, text: String){
    InfoWindow().apply {
        setAdapter(context, text)
        open(this@setInfoWindowText)
    }
}