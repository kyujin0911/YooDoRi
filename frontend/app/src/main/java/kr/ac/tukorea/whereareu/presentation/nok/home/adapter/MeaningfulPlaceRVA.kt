package kr.ac.tukorea.whereareu.presentation.nok.home.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulPlaceBinding
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.data.model.nok.home.TimeInfo

class MeaningfulPlaceRVA :
    ListAdapter<MeaningfulPlaceInfo, MeaningfulPlaceRVA.MeaningfulPlaceViewHolder>
        (object : DiffUtil.ItemCallback<MeaningfulPlaceInfo>() {
        override fun areItemsTheSame(
            oldItem: MeaningfulPlaceInfo,
            newItem: MeaningfulPlaceInfo
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: MeaningfulPlaceInfo,
            newItem: MeaningfulPlaceInfo
        ): Boolean {
            return oldItem.address == newItem.address
        }

    }) {
    private var meaningfulPlaceRVAClickListener: MeaningfulPlaceRVAClickListener? = null

    //RVA 클릭 리스너 초기화
    fun setRVAClickListener(outerListener: MeaningfulPlaceRVAClickListener){
        meaningfulPlaceRVAClickListener = outerListener
    }

    inner class MeaningfulPlaceViewHolder(private val binding: ItemMeaningfulPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(meaningfulPlace: MeaningfulPlaceInfo) {
            with(binding) {
                model = meaningfulPlace

                mapViewBtn.setOnClickListener {
                    meaningfulPlaceRVAClickListener?.onClickMapView(meaningfulPlace.latLng)
                }

                infoViewBtn.setOnClickListener {
                    meaningfulPlaceRVAClickListener?.onClickInfoView(meaningfulPlace)
                }

            }
        }
    }

    // 요일을 기준으로 시간 정보 그룹화
    private fun groupTimeInfoList(timeInfoMap: Map<String, List<TimeInfo>>): List<GroupedTimeInfo>{
        val groupedTimeInfoList = mutableListOf<GroupedTimeInfo>()

        val dayOfWeeks = timeInfoMap.keys
        dayOfWeeks.forEach { dayOfWeek ->
            val dayOfWeekInKorean = convertDayOfWeekInKorean(dayOfWeek)
            val timeList = timeInfoMap[dayOfWeek]?.map { timeInfo ->
                "${timeInfo.time.substring(0 until 2)}시~${
                    timeInfo.time.substring(2 until 4)
                }시"
            }
            groupedTimeInfoList.add(GroupedTimeInfo(dayOfWeekInKorean, timeList!!))
        }

        return groupedTimeInfoList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeaningfulPlaceViewHolder {
        return MeaningfulPlaceViewHolder(
            ItemMeaningfulPlaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MeaningfulPlaceViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun convertDayOfWeekInKorean(day: String): String {
        return when (day) {
            "Monday" -> "월"
            "Tuesday" -> "화"
            "Wednesday" -> "수"
            "Thursday" -> "목"
            "Friday" -> "금"
            "Saturday" -> "토"
            "Sunday" -> "일"
            else -> "알 수 없음"
        }
    }

    private fun convertTimeInKorean(time: String): String{
        return "${time.substring(0 until 2)}시~${time.substring(2 until 4)}시"
    }

    interface MeaningfulPlaceRVAClickListener{
        fun onClickMapView(latLng: LatLng)
        fun onClickInfoView(meaningfulPlace: MeaningfulPlaceInfo)
    }
}