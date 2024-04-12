package kr.ac.tukorea.whereareu.data.model.kakao.address

import com.google.gson.annotations.SerializedName

data class Meta (
    @SerializedName("total_count")
    val totalCount: Int
)