package kr.ac.tukorea.whereareu.data.model.nok.safearea

data class GetSafeAreaResponse(
    val groupName: String,
    val safeAreaList: List<SafeAreaDto>
)
