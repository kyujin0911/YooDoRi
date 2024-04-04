package kr.ac.tukorea.whereareu.domain.login.userinfo

data class GetUserInfoResultDementiaInfoRecord(
    val dementiaKey : String,
    val dementiaName : String,
    val dementiaPhoneNumber : String,
    val updateRate : Int,
)
