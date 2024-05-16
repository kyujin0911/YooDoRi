package kr.ac.tukorea.whereareu.data.model.login.request

data class NokIdentityRequest(
    val keyFromDementia: String,
    val nokName: String,
    val nokPhoneNumber: String
) {
    constructor(): this("000000", "사용자", "010-9999-9999")
}
