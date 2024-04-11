package kr.ac.tukorea.whereareu.data.model.kakao

import com.google.gson.annotations.SerializedName

data class RoadAddress(
    @SerializedName("road_address")
    val addressName: String,
    @SerializedName("building_name")
    val buildingName: String
)
