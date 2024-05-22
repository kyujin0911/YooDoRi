package kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemSafeAreaBinding
import kr.ac.tukorea.whereareu.databinding.ItemSafeAreaGroupBinding
import kr.ac.tukorea.whereareu.databinding.ItemTimeInfoBinding
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import java.lang.StringBuilder

class SafeAreaRVA() : ListAdapter<SafeArea, RecyclerView.ViewHolder>(
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
    inner class SafeAreaViewHolder(private val binding: ItemSafeAreaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(safeArea: SafeArea) {
            with(binding) {
                model = safeArea
            }
        }
    }

    inner class SafeAreaGroupViewHolder(private val binding: ItemSafeAreaGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(safeArea: SafeArea) {
            with(binding) {
                model = safeArea
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType) {
            SAFE_AREA -> {
                SafeAreaViewHolder(
                    ItemSafeAreaBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                SafeAreaGroupViewHolder(
                    ItemSafeAreaGroupBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            SAFE_AREA -> {
                (holder as SafeAreaViewHolder).bind(currentList[position])
            }
            SAFE_AREA_GROUP -> {
                (holder as SafeAreaGroupViewHolder).bind(currentList[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].viewType
    }

    companion object{
        val SAFE_AREA = 0
        val SAFE_AREA_GROUP = 1
    }
}