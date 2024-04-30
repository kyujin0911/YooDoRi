package kr.ac.tukorea.whereareu.presentation.nok.home.meaningfulAdapter

import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemInnerMeaningfulListBottomSheetBinding
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulListBinding
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulPlaceBottomSheetBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo

class UserMeaningfulListAdapter() :
    ListAdapter<MeaningfulPlace, UserMeaningfulListAdapter.UserMeaningfulListViewHolder>(diffUtil) {
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
                    }
                mapViewBtn.setOnClickListener {
                    onItemClickListener?.onClickMoreView(meaningfulPlace)
                }
                copyRoadAddressBtn.setOnClickListener {
                    onItemClickListener?.onClickCopyAddress()
                }
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserMeaningfulListAdapter.UserMeaningfulListViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(
        holder: UserMeaningfulListAdapter.UserMeaningfulListViewHolder,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MeaningfulPlace>() {
            override fun areItemsTheSame(
                oldItem: MeaningfulPlace,
                newItem: MeaningfulPlace
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: MeaningfulPlace,
                newItem: MeaningfulPlace
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