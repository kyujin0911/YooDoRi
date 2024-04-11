package kr.ac.tukorea.whereareu.data.model.kakao

import com.google.gson.annotations.SerializedName

data class AddressResponse(
    val meta: Meta,
    val documents: List<Documents>
)
