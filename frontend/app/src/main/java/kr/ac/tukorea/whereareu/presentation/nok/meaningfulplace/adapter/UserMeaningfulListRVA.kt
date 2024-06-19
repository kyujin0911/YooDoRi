package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.data.model.nok.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.databinding.ItemInnerMeaningfulListBottomSheetBinding


class UserMeaningfulListRVA() :
    ListAdapter<MeaningfulPlace, UserMeaningfulListRVA.UserMeaningfulListViewHolder>(diffUtil) {
    private val onItemClickListener : OnItemClickListener? =  null

    inner class UserMeaningfulListViewHolder(
        private val binding: ItemInnerMeaningfulListBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(meaningfulPlace: MeaningfulPlace){
            binding.nameTv.text = meaningfulPlace.address
            binding.roadAddressTv.text = meaningfulPlace.address
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserMeaningfulListViewHolder {
        return UserMeaningfulListViewHolder(
            ItemInnerMeaningfulListBottomSheetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: UserMeaningfulListViewHolder,
        position: Int
    ) {
        holder.bind(currentList[position])
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
}