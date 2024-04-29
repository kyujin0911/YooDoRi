package kr.ac.tukorea.whereareu.presentation.nok.home.meaningfulAdapter

import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemInnerMeaningfulListBottomSheetBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlace
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo

class UserMeaningfulListAdapter() :
    ListAdapter<MeaningfulPlace, UserMeaningfulListAdapter.UserMeaningfulListViewHolder>(diffUtil) {
    private val onItemClickListener : OnItemClickListener? =  null

    inner class UserMeaningfulListViewHolder(
        private val binding: ItemInnerMeaningfulListBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(meaningfulPlace: MeaningfulPlace){
            with(binding) {
                mapViewBtn.setOnClickListener {
                    onItemClickListener?.onClickMoreView(meaningfulPlace)
                }
                copyRoadAddressBtn.setOnClickListener {
                    onItemClickListener?.onClickCopyAddress(policeStationInfo.roadAddressName)
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
    interface OnItemClickListener {
        fun onClickMoreView(policeStationInfo: PoliceStationInfo)
        fun onClickCopyPhoneNumber(phoneNumber: String)
        fun onClickCopyAddress(address: String)
    }
}