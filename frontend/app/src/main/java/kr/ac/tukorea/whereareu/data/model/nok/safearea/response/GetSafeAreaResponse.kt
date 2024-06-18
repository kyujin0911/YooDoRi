package kr.ac.tukorea.whereareu.data.model.nok.safearea.response

import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaGroup

data class GetSafeAreaResponse(
    val groupList: List<SafeAreaGroup>
)
