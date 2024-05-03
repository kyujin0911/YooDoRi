package kr.ac.tukorea.whereareu.domain.login

data class NokInfo(
    val nokKey : String,
    val nokName : String,
    val nokPhoneNumber : String,
    val updateRate : Int = 60
) {
    constructor(): this("", "", "", 0)
}
