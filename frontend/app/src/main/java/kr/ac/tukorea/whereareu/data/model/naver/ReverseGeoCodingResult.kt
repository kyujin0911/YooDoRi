package kr.ac.tukorea.whereareu.data.model.naver

data class ReverseGeoCodingResult(
    val region: RegionInfo
)

data class RegionInfo(
    val area0: AreaInfo,
    val area1: AreaInfo,
    val area2: AreaInfo,
    val area3: AreaInfo,
    val area4: AreaInfo,
)
