package kr.ac.tukorea.whereareu.domain.login.userinfo

data class GetUserInfoResultNokRecord(
    val nokKey : String,
    val nokName : String,
    val nokPhoneNumber : String,
    val updateRate : Boolean,
)
