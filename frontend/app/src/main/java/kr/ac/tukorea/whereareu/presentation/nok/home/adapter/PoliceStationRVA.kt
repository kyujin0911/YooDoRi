package kr.ac.tukorea.whereareu.presentation.nok.home.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemPoliceStationBinding
import kr.ac.tukorea.whereareu.data.model.nok.home.PoliceStationInfoResponse
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo

class PoliceStationRVA() :
    ListAdapter<PoliceStationInfo, PoliceStationRVA.PoliceStationViewHolder>(
        object :
            DiffUtil.ItemCallback<PoliceStationInfo>() {
            override fun areItemsTheSame(
                oldItem: PoliceStationInfo,
                newItem: PoliceStationInfo
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: PoliceStationInfo,
                newItem: PoliceStationInfo
            ): Boolean {
                return oldItem == newItem
            }

        }) {

        private var policeStationRVAClickListener: PoliceStationRVAClickListener? = null
    fun setPoliceStationRVAClickListener(listener: PoliceStationRVAClickListener){
        policeStationRVAClickListener = listener
    }
    inner class PoliceStationViewHolder(private val binding: ItemPoliceStationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(policeStationInfo: PoliceStationInfo) {
            Log.d("police station info", policeStationInfo.toString())
            with(binding) {
                model = policeStationInfo

                mapViewBtn.setOnClickListener {
                    policeStationRVAClickListener?.onClickMapView(policeStationInfo)
                }

                copyPhoneNumberBtn.setOnClickListener {
                    policeStationRVAClickListener?.onClickCopyPhoneNumber(policeStationInfo.policePhoneNumber)
                }

                copyRoadAddressBtn.setOnClickListener {
                    policeStationRVAClickListener?.onClickCopyAddress(policeStationInfo.policeAddress)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PoliceStationViewHolder {
        return PoliceStationViewHolder(
            ItemPoliceStationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PoliceStationViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    interface PoliceStationRVAClickListener {
        fun onClickMapView(policeStationInfo: PoliceStationInfo)
        fun onClickCopyPhoneNumber(phoneNumber: String)
        fun onClickCopyAddress(address: String)
    }
}