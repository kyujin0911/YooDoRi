package kr.ac.tukorea.whereareu.data.model.kakao.address

import com.google.gson.annotations.SerializedName

data class Documents (
    @SerializedName("road_address")
    val roadAddress: RoadAddress,
    val address: Address
)