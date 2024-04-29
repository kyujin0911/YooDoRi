package kr.ac.tukorea.whereareu.domain.home

data class MeaningfulPlaceTimeInfo(
    val dayOfTheWeek: String,
    val time: String,
    var isExpanded: Boolean = false,
)
