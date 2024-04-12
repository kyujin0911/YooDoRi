package kr.ac.tukorea.whereareu.data.model.setting

data class UpdateRateRequest(
    val key: String,
    val isDementia : Int,
    val updateRate: Int
)
