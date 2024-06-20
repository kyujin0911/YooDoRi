package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.databinding.ItemPoliceStationBinding
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo

class PoliceStationRVAForPage() :
    ListAdapter<PoliceStationInfo, PoliceStationRVAForPage.PoliceStationForPageViewHolder>(
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

    private var policeStationRVAForPageClickListener: PoliceStationRVAForPageClickListener? = null
    fun setPoliceStationRVAForPageClickListener(listener: PoliceStationRVAForPageClickListener){
        policeStationRVAForPageClickListener = listener
    }
    inner class PoliceStationForPageViewHolder(private val binding: ItemPoliceStationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(policeStationInfo: PoliceStationInfo) {
            Log.d("police station info", policeStationInfo.toString())
            with(binding) {
                model = policeStationInfo

                mapViewBtn.setOnClickListener {
                    policeStationRVAForPageClickListener?.onClickMapView(policeStationInfo)
                }

                copyPhoneNumberBtn.setOnClickListener {
                    policeStationRVAForPageClickListener?.onClickCopyPhoneNumber(policeStationInfo.policePhoneNumber)
                }

                copyRoadAddressBtn.setOnClickListener {
                    policeStationRVAForPageClickListener?.onClickCopyAddress(policeStationInfo.policeAddress)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PoliceStationForPageViewHolder {
        return PoliceStationForPageViewHolder(
            ItemPoliceStationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PoliceStationForPageViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
    interface PoliceStationRVAForPageClickListener {
        fun onClickMapView(policeStationInfo: PoliceStationInfo)
        fun onClickCopyPhoneNumber(phoneNumber: String)
        fun onClickCopyAddress(address: String)
    }
}