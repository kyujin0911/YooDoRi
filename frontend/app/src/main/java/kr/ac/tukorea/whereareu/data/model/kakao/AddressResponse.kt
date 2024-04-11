package kr.ac.tukorea.whereareu.data.model.kakao

import com.google.gson.annotations.SerializedName

data class AddressResponse(
    @SerializedName("road_address")
    val roadAddress: RoadAddress,
    val address: Address
)
