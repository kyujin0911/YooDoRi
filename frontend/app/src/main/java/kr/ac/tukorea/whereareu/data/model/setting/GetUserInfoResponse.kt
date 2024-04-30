package kr.ac.tukorea.whereareu.data.model.setting

import kr.ac.tukorea.whereareu.domain.login.DementiaInfo
import kr.ac.tukorea.whereareu.domain.login.NokInfo

data class GetUserInfoResponse(
    val dementiaInfoRecord : DementiaInfo,
    val nokInfoRecord : NokInfo
)
