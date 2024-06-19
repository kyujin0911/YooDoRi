package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import kr.ac.tukorea.whereareu.data.model.nok.home.TimeInfo
import kr.ac.tukorea.whereareu.databinding.ItemMeaningfulPlaceBinding
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo

class MeaningfulPlaceRVAForPage :
    ListAdapter<MeaningfulPlaceInfo, MeaningfulPlaceRVAForPage.MeaningfulPlaceForPageViewHolder>
        (object : DiffUtil.ItemCallback<MeaningfulPlaceInfo>() {
        override fun areItemsTheSame(
            oldItem: MeaningfulPlaceInfo,
            newItem: MeaningfulPlaceInfo
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: MeaningfulPlaceInfo,
            newItem: MeaningfulPlaceInfo
        ): Boolean {
            return oldItem.address == newItem.address
        }

    }) {
    private var meaningfulPlaceRVAForPageClickListener: MeaningfulPlaceRVAForPageClickListener? = null

    fun setRVAForPageClickListener(outerListener: MeaningfulPlaceRVAForPageClickListener){
        meaningfulPlaceRVAForPageClickListener = outerListener
    }

    inner class MeaningfulPlaceForPageViewHolder(private val binding: ItemMeaningfulPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(meaningfulPlace: MeaningfulPlaceInfo) {
            with(binding) {
                model = meaningfulPlace

                mapViewBtn.setOnClickListener {
                    meaningfulPlaceRVAForPageClickListener?.onClickMapView(meaningfulPlace.latLng)
                }
                infoViewBtn.setOnClickListener {
                    meaningfulPlaceRVAForPageClickListener?.onClickInfoView(meaningfulPlace)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeaningfulPlaceForPageViewHolder {
        return MeaningfulPlaceForPageViewHolder(
            ItemMeaningfulPlaceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MeaningfulPlaceForPageViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    interface MeaningfulPlaceRVAForPageClickListener{
        fun onClickMapView(latLng: LatLng)
        fun onClickInfoView(meaningfulPlace: MeaningfulPlaceInfo)
    }
}