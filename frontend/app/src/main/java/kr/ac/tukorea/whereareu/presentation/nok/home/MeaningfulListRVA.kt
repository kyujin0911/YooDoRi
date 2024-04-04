package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulListBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace

class MeaningfulListRVA: ListAdapter<MeaningfulPlace, MeaningfulListRVA.MeaningfulListViewHolder>
    (object : DiffUtil.ItemCallback<MeaningfulPlace>(){
    override fun areItemsTheSame(oldItem: MeaningfulPlace, newItem: MeaningfulPlace): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MeaningfulPlace, newItem: MeaningfulPlace): Boolean {
        return oldItem.address == newItem.address
    }

}) {

    inner class MeaningfulListViewHolder(private val binding: ItemMeaningfulListBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(meaningfulPlace: MeaningfulPlace){
            with(binding) {
                addressTv.text = meaningfulPlace.address
                dateTv.text = convertDayOfWeekInKorean(meaningfulPlace.date)
                timeTv.text = "${meaningfulPlace.time.substring(0 until 2)}시~${meaningfulPlace.time.substring(2 until 4)}시"
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