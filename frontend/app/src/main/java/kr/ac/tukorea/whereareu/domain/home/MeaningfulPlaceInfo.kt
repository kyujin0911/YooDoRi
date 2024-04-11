package kr.ac.tukorea.whereareu.domain.home

data class MeaningfulPlaceInfo(
    val address: String,
    val meaningfulPlaceListInfo: List<MeaningfulPlaceListInfo>,
    var isExpanded: Boolean = false
)
