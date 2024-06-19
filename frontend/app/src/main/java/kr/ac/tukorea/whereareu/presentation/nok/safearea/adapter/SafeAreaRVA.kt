package kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaGroup
import kr.ac.tukorea.whereareu.databinding.ItemSafeAreaBinding
import kr.ac.tukorea.whereareu.databinding.ItemSafeAreaGroupBinding
import kr.ac.tukorea.whereareu.databinding.ItemTimeInfoBinding
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import java.lang.StringBuilder

class SafeAreaRVA() : ListAdapter<SafeAreaGroup, RecyclerView.ViewHolder>(
    object :
        DiffUtil.ItemCallback<SafeAreaGroup>() {
        override fun areItemsTheSame(
            oldItem: SafeAreaGroup,
            newItem: SafeAreaGroup
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: SafeAreaGroup,
            newItem: SafeAreaGroup
        ): Boolean {
            return oldItem == newItem
        }

    }) {
    private var safeRVAClickListener: SafeRVAClickListener? = null
    fun setSafeRVAClickListener(listener: SafeRVAClickListener){
        this.safeRVAClickListener = listener
    }
    /*inner class SafeAreaViewHolder(private val binding: ItemSafeAreaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(safeAreaGroup: SafeAreaGroup) {
            with(binding) {
                model = safeArea

                mapViewBtn.setOnClickListener {
                    val latLng = LatLng(safeArea.latitude, safeArea.longitude)
                    safeRVAClickListener?.onClickMapView(latLng)
                }
            }
        }
    }*/

    inner class SafeAreaGroupViewHolder(private val binding: ItemSafeAreaGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(safeAreaGroup: SafeAreaGroup) {
            with(binding) {
                model = safeAreaGroup
                infoViewBtn.setOnClickListener {
                    safeRVAClickListener?.onClickInfoView(safeAreaGroup.groupKey, safeAreaGroup.groupName)
                }
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType) {
            /*SAFE_AREA -> {
                SafeAreaViewHolder(
                    ItemSafeAreaBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }*/
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
            /*SAFE_AREA -> {
                (holder as SafeAreaViewHolder).bind(currentList[position])
            }*/
            SAFE_AREA_GROUP -> {
                (holder as SafeAreaGroupViewHolder).bind(currentList[position])
            }
        }
    }

    /*override fun getItemViewType(position: Int): Int {
        return currentList[position].viewType
    }*/

    interface SafeRVAClickListener{
        fun onClickMapView(latLng: LatLng)
        fun onClickInfoView(groupKey: String, groupName: String)
    }

    companion object{
        val SAFE_AREA = 1
        val SAFE_AREA_GROUP = 0
    }
}