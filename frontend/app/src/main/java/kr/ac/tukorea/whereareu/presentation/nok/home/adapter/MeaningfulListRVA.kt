package kr.ac.tukorea.whereareu.presentation.nok.home.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulListBinding
import kr.ac.tukorea.whereareu.domain.home.DateInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceListInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo

class MeaningfulListRVA :
    ListAdapter<MeaningfulPlaceInfo, MeaningfulListRVA.MeaningfulListViewHolder>
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
    private var innerRVAClickListener: InnerMeaningfulListRVA.InnerRVAClickListener? = null
    private var outerRVAClickListener: OuterRVAClickListener? = null
    fun setRVAClickListener(outerListener: OuterRVAClickListener , innerListener: InnerMeaningfulListRVA.InnerRVAClickListener){
        innerRVAClickListener = innerListener
        outerRVAClickListener = outerListener
    }
    inner class MeaningfulListViewHolder(private val binding: ItemMeaningfulListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(meaningfulPlace: MeaningfulPlaceInfo) {
            with(binding) {
                model = meaningfulPlace
                val dateInfoList = mutableListOf<DateInfo>()
                val listGroupedByDayOfWeek = meaningfulPlace.meaningfulPlaceListInfo.groupBy { it.date }
                listGroupedByDayOfWeek.keys.forEach { key ->
                    val dayOfWeek = convertDayOfWeekInKorean(key)
                    val times = listGroupedByDayOfWeek[key]?.map { timeInfo ->
                        "${timeInfo.time.substring(0 until 2)}시~${
                            timeInfo.time.substring(2 until 4)
                        }시"
                    }
                    dateInfoList.add(DateInfo(dayOfWeek, times!!))
                }
                Log.d("dateInfoList", dateInfoList.toString())
                moreViewBtn.setOnClickListener {
                    meaningfulPlace.isExpanded = meaningfulPlace.isExpanded.not()
                    notifyItemChanged(bindingAdapterPosition)
                    Log.d("isExpanded", meaningfulPlace.isExpanded.toString())
                }

                mapViewBtn.setOnClickListener {
                    outerRVAClickListener?.onClickMapView(meaningfulPlace.latitude, meaningfulPlace.longitude)
                }

                val adapter = InnerMeaningfulListRVA()
                adapter.setInnerRVAClickListener(innerRVAClickListener!!)
                innerRv.adapter = adapter
                adapter.submitList(meaningfulPlace.policeStationInfo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeaningfulListViewHolder {
        return MeaningfulListViewHolder(
            ItemMeaningfulListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MeaningfulListViewHolder, position: Int) {
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

    interface OuterRVAClickListener{
        fun onClickMapView(latitude: Double, longitude: Double)
    }
}