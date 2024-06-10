package kr.ac.tukorea.whereareu.data.model.nok.safearea

data class RegisterSafeAreaRequest(
    val dementiaKey: String,
    val groupKey: String,
    val areaName: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
)
