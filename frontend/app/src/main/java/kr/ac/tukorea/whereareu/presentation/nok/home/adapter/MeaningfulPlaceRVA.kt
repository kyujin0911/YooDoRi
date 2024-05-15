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
    private var policeStationRVAClickListener: PoliceStationRVA.PoliceStationRVAClickListener? = null
    private var meaningfulPlaceRVAClickListener: MeaningfulPlaceRVAClickListener? = null

    //RVA 클릭 리스너 초기화
    fun setRVAClickListener(outerListener: MeaningfulPlaceRVAClickListener, innerListener: PoliceStationRVA.PoliceStationRVAClickListener){
        policeStationRVAClickListener = innerListener
        meaningfulPlaceRVAClickListener = outerListener
    }

    inner class MeaningfulPlaceViewHolder(private val binding: ItemMeaningfulPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(meaningfulPlace: MeaningfulPlaceInfo) {
            with(binding) {
                model = meaningfulPlace

                moreViewBtn.setOnClickListener {
                    meaningfulPlace.isExpanded = meaningfulPlace.isExpanded.not()
                    notifyItemChanged(bindingAdapterPosition)
                    Log.d("isExpanded", meaningfulPlace.isExpanded.toString())
                }

                mapViewBtn.setOnClickListener {
                    meaningfulPlaceRVAClickListener?.onClickMapView(meaningfulPlace.latLng)
                }

                val policeStationRVA = PoliceStationRVA()
                policeStationRVA.setPoliceStationRVAClickListener(policeStationRVAClickListener!!)
                policeRv.adapter = policeStationRVA
                policeStationRVA.submitList(meaningfulPlace.policeStationInfo)

                val timeInfoRVA = TimeInfoRVA()
                timeInfoRv.adapter = timeInfoRVA
                val timeInfo = meaningfulPlace.timeInfo.map {info ->
                    TimeInfo(convertDayOfWeekInKorean(info.dayOfTheWeek), convertTimeInKorean(info.time))
                }
                timeInfoRVA.submitList(timeInfo)
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
            "Monday" -> "월요일"
            "Tuesday" -> "화요일"
            "Wednesday" -> "수요일"
            "Thursday" -> "목요일"
            "Friday" -> "금요일"
            "Saturday" -> "토요일"
            "Sunday" -> "일요일"
            else -> "알 수 없음"
        }
    }

    private fun convertTimeInKorean(time: String): String{
        return "${time.substring(0 until 2)}시~${time.substring(2 until 4)}시"
    }

    interface MeaningfulPlaceRVAClickListener{
        fun onClickMapView(latLng: LatLng)
    }
}