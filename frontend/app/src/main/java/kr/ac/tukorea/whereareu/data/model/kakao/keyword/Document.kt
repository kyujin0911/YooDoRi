package kr.ac.tukorea.whereareu.data.model.kakao.keyword

import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("place_name")
    val placeName: String,
    val distance: String,
    @SerializedName("place_url")
    val placeUrl: String,
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("road_address_name")
    val roadAddressName: String,
    val phone: String,
    val x: String,
    val y: String
)