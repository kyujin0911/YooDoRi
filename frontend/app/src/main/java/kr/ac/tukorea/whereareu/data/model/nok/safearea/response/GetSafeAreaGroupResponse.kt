package kr.ac.tukorea.whereareu.data.model.nok.safearea.response

import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaDto

data class GetSafeAreaGroupResponse(
    val safeAreas: List<SafeAreaDto>
)
