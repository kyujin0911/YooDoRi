package kr.ac.tukorea.whereareu.domain.home

data class MeaningfulPlace(
    val address: String,
    val dayOfWeek: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
)
