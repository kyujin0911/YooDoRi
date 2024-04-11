package kr.ac.tukorea.whereareu.data.model.kakao

import com.google.gson.annotations.SerializedName

data class RoadAddress(
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("building_name")
    val buildingName: String
)
