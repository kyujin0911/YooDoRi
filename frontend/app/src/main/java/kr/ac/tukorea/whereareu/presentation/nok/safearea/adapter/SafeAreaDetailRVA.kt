package kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemSafeAreaBinding
import kr.ac.tukorea.whereareu.databinding.ItemSateAreaDetailBinding
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.domain.safearea.SafeAreaDetail

class SafeAreaDetailRVA() : ListAdapter<SafeAreaDetail, SafeAreaDetailRVA.SafeAreaDetailViewHolder>(
    object :
        DiffUtil.ItemCallback<SafeAreaDetail>() {
        override fun areItemsTheSame(
            oldItem: SafeAreaDetail,
            newItem: SafeAreaDetail
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: SafeAreaDetail,
            newItem: SafeAreaDetail
        ): Boolean {
            return oldItem == newItem
        }

    }) {
    inner class SafeAreaDetailViewHolder(private val binding: ItemSateAreaDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(safeAreaDetail: SafeAreaDetail) {
            with(binding) {

            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SafeAreaDetailViewHolder {
        return SafeAreaDetailViewHolder(
            ItemSateAreaDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SafeAreaDetailViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}