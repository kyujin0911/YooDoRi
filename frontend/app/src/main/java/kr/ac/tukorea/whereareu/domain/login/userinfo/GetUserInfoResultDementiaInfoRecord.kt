package kr.ac.tukorea.whereareu.domain.login.userinfo

data class GetUserInfoResultDementiaInfoRecord(
    val dementiaKey : Int,
    val dementiaName : String,
    val dementiaPhoneNumber : String,
    val updateRate : Boolean,
)
