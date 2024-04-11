package kr.ac.tukorea.whereareu.presentation.nok.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemInnerMeaningfulListBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo

class InnerMeaningfulListRVA :
    ListAdapter<MeaningfulPlaceInfo, InnerMeaningfulListRVA.InnerMeaningfulListViewHolder>(object :
        DiffUtil.ItemCallback<MeaningfulPlaceInfo>() {
        override fun areItemsTheSame(
            oldItem: MeaningfulPlaceInfo,
            newItem: MeaningfulPlaceInfo
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: MeaningfulPlaceInfo,
            newItem: MeaningfulPlaceInfo
        ): Boolean {
            return oldItem == newItem
        }

    }) {
    inner class InnerMeaningfulListViewHolder(private val binding: ItemInnerMeaningfulListBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(meaningfulPlaceInfo: MeaningfulPlaceInfo){
                with(binding) {
                    model = meaningfulPlaceInfo
                    dateTv.text = convertDayOfWeekInKorean(meaningfulPlaceInfo.date)
                    timeTv.text = "${meaningfulPlaceInfo.time.substring(0 until 2)}시~${
                        meaningfulPlaceInfo.time.substring(2 until 4)
                    }시"
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

    private fun convertDayOfWeekInKorean(day: String): String{
        return when(day){
            "Monday" -> "월요일"
            "Tuesday" -> "화요일"
            "Wednesday" -> "수요일"
            "Thursday" -> "목요일"
            "Friday" -> "금요일"
            "Saturday" -> "토요일"
            "Sunday" -> "일요일"
            else-> "알 수 없음"
        }
    }
}