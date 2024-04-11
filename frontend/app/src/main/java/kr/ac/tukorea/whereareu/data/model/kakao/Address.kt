package kr.ac.tukorea.whereareu.data.model.kakao

import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("address_name")
    val addressName: String
)
