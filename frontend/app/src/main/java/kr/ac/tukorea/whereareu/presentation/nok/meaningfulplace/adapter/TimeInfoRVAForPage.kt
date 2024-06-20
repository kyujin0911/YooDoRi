package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemTimeInfoBinding
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo

import java.lang.StringBuilder

class TimeInfoRVAForPage(): ListAdapter<GroupedTimeInfo, TimeInfoRVAForPage.TimeInfoForPageViewHolder>(
    object :
        DiffUtil.ItemCallback<GroupedTimeInfo>() {
        override fun areItemsTheSame(
            oldItem: GroupedTimeInfo,
            newItem: GroupedTimeInfo
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: GroupedTimeInfo,
            newItem: GroupedTimeInfo
        ): Boolean {
            return oldItem == newItem
        }

    }) {
    inner class TimeInfoForPageViewHolder(private val binding: ItemTimeInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(timeInfo: GroupedTimeInfo) {
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
    ): TimeInfoForPageViewHolder {
        return TimeInfoForPageViewHolder(
            ItemTimeInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TimeInfoForPageViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}