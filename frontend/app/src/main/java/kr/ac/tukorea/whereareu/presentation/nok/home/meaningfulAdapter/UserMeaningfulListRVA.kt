package kr.ac.tukorea.whereareu.presentation.nok.home.meaningfulAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemInnerMeaningfulListBottomSheetBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo


class UserMeaningfulListRVA() :
    ListAdapter<MeaningfulPlaceInfo, UserMeaningfulListRVA.UserMeaningfulListViewHolder>(diffUtil) {
    private val onItemClickListener : OnItemClickListener? =  null

    inner class UserMeaningfulListViewHolder(
        private val binding: ItemInnerMeaningfulListBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(meaningfulPlace: MeaningfulPlaceInfo){
            binding.nameTv.text = meaningfulPlace.address
            binding.roadAddressTv.text = meaningfulPlace.address

        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserMeaningfulListRVA.UserMeaningfulListViewHolder {
        return UserMeaningfulListViewHolder(
            ItemInnerMeaningfulListBottomSheetBinding.inflate(
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
}