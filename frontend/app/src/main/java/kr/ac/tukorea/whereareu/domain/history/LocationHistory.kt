package kr.ac.tukorea.whereareu.domain.history

data class LocationHistory(
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val userStatus: String,
    val distance: Float,
    val isLast: Boolean = false
) {
    constructor() : this(0.0, 0.0, "", "", 0f)
}
