package kr.ac.tukorea.whereareu.domain.login.userinfo

data class GetUserInfoResult(
    val dementiaInfoRecord : GetUserInfoResultDementiaInfoRecord,
    val nokInfoRecord : GetUserInfoResultNokRecord
)
