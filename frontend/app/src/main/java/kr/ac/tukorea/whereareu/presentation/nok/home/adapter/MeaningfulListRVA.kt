package kr.ac.tukorea.whereareu.presentation.nok.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulListBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceListInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo

class MeaningfulListRVA: ListAdapter<MeaningfulPlaceInfo, MeaningfulListRVA.MeaningfulListViewHolder>
    (object : DiffUtil.ItemCallback<MeaningfulPlaceInfo>(){
    override fun areItemsTheSame(oldItem: MeaningfulPlaceInfo, newItem: MeaningfulPlaceInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MeaningfulPlaceInfo, newItem: MeaningfulPlaceInfo): Boolean {
        return oldItem.address == newItem.address
    }

}) {

    inner class MeaningfulListViewHolder(private val binding: ItemMeaningfulListBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(meaningfulPlace: MeaningfulPlaceInfo){
            with(binding) {
                model = meaningfulPlace
                val listInfo = meaningfulPlace.meaningfulPlaceListInfo.mapIndexed { index, meaningfulPlaceListInfo ->
                    val date = convertDayOfWeekInKorean(meaningfulPlaceListInfo.date)
                    val time = "${meaningfulPlaceListInfo.time.substring(0 until 2)}시~${
                        meaningfulPlaceListInfo.time.substring(2 until 4)
                    }시"
                    MeaningfulPlaceListInfo(index+1, date, time, meaningfulPlaceListInfo.latitude, meaningfulPlaceListInfo.longitude)
                }
                val adapter = InnerMeaningfulListRVA()
                innerRv.adapter = adapter
                adapter.submitList(listInfo)
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