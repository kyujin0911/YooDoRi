package kr.ac.tukorea.whereareu.data.model.kakao

import com.google.gson.annotations.SerializedName

data class Address(
    @SerializedName("road_address")
    val addressName: String
)
