package kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemTimeInfoBinding
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import java.lang.StringBuilder

class SafeAreaRVA(): ListAdapter<SafeArea, SafeAreaRVA.SafeAreaViewHolder>(
object :
    DiffUtil.ItemCallback<SafeArea>() {
    override fun areItemsTheSame(
        oldItem: SafeArea,
        newItem: SafeArea
    ): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(
        oldItem: SafeArea,
        newItem: SafeArea
    ): Boolean {
        return oldItem == newItem
    }

}) {
    inner class SafeAreaViewHolder(private val binding: ItemTimeInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(safeArea: SafeArea) {
            Log.d("police station info", timeInfo.toString())
            with(binding) {
                model = timeInfo
                val time = StringBuilder()
                val timeList = timeInfo.timeList.sortedBy { it }
                val last = timeList.get(timeList.lastIndex)
                timeList.forEach {
                    if(it == last){
                        time.append(it)
                    } else {
                        time.append("$it,  ")
                    }
                }
                timeTv.text = time
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeInfoViewHolder {
        return TimeInfoViewHolder(
            ItemTimeInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TimeInfoViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}