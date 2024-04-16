package kr.ac.tukorea.whereareu.presentation.nok.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemInnerMeaningfulListBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceListInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo

class InnerMeaningfulListRVA() :
    ListAdapter<PoliceStationInfo, InnerMeaningfulListRVA.InnerMeaningfulListViewHolder>(
        object :
            DiffUtil.ItemCallback<PoliceStationInfo>() {
            override fun areItemsTheSame(
                oldItem: PoliceStationInfo,
                newItem: PoliceStationInfo
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: PoliceStationInfo,
                newItem: PoliceStationInfo
            ): Boolean {
                return oldItem == newItem
            }

        }) {

        private var innerRVAClickListener: InnerRVAClickListener? = null
    fun setInnerRVAClickListener(listener: InnerRVAClickListener){
        innerRVAClickListener = listener
    }
    inner class InnerMeaningfulListViewHolder(private val binding: ItemInnerMeaningfulListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(policeStationInfo: PoliceStationInfo) {
            with(binding) {
                model = policeStationInfo
                /*dateTv.text = convertDayOfWeekInKorean(meaningfulPlaceListInfo.date)
                timeTv.text = "${meaningfulPlaceListInfo.time.substring(0 until 2)}시~${
                    meaningfulPlaceListInfo.time.substring(2 until 4)
                }시"*/
                mapViewBtn.setOnClickListener {
                    innerRVAClickListener?.onClick(policeStationInfo)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InnerMeaningfulListViewHolder {
        return InnerMeaningfulListViewHolder(
            ItemInnerMeaningfulListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: InnerMeaningfulListViewHolder, position: Int) {
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

    interface InnerRVAClickListener {
        fun onClick(policeStationInfo: PoliceStationInfo)
    }
}