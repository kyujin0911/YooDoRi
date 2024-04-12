package kr.ac.tukorea.whereareu.data.model.kakao.keyword

import com.google.gson.annotations.SerializedName

data class Meta(
    @SerializedName("same_name")
    val sameName: SameName
)
