package kr.ac.tukorea.whereareu.domain.safearea

data class SafeArea(
    val groupName: String,
    val areaName: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
)
