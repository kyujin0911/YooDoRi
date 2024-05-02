package kr.ac.tukorea.whereareu.presentation.nok.home.meaningfulAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulPlaceBottomSheetBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceListInfo

class UserMeaningfulListRVA() :
    ListAdapter<MeaningfulPlaceInfo, UserMeaningfulListRVA.UserMeaningfulListViewHolder>(diffUtil) {
    private val onItemClickListener : OnItemClickListener? =  null

    inner class UserMeaningfulListViewHolder(
        private val binding: ItemMeaningfulPlaceBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(meaningfulPlace: MeaningfulPlaceInfo){
            with(binding) {
                val listInfo =
                    meaningfulPlace.meaningfulPlaceListInfo.mapIndexed { index, meaningfulPlaceListInfo ->
                        val date = convertDayOfWeekInKorean(meaningfulPlaceListInfo.date)
                        val time = "${meaningfulPlaceListInfo.time.substring(0 until 2)}시~${
                            meaningfulPlaceListInfo.time.substring(2 until 4)
                        }"
                        MeaningfulPlaceListInfo(
                            date, time)
                    }
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserMeaningfulListRVA.UserMeaningfulListViewHolder {
        return UserMeaningfulListViewHolder(
            ItemMeaningfulPlaceBottomSheetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: UserMeaningfulListRVA.UserMeaningfulListViewHolder,
        position: Int
    ) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MeaningfulPlaceInfo>() {
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

        }
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
}