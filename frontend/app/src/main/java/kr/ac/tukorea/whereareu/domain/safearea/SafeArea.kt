package kr.ac.tukorea.whereareu.domain.safearea

import java.io.Serializable

data class SafeArea(
    val groupName: String,
    val groupKey: String,
    val areaKey: String,
    val areaName: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
    val viewType: Int
): Serializable
