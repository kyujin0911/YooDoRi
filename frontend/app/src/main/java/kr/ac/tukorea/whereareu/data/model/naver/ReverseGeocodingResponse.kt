package kr.ac.tukorea.whereareu.data.model.naver

data class ReverseGeocodingResponse(
    val status: StatusResult,
    val results: ReverseGeoCodingResult
)
