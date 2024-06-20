package kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaDto
import kr.ac.tukorea.whereareu.databinding.ItemSafeAreaBinding
import kr.ac.tukorea.whereareu.databinding.ItemSafeAreaDetailBinding
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.domain.safearea.SafeAreaDetail

class SafeAreaDetailRVA() : ListAdapter<SafeAreaDto, SafeAreaDetailRVA.SafeAreaDetailViewHolder>(
    object :
        DiffUtil.ItemCallback<SafeAreaDto>() {
        override fun areItemsTheSame(
            oldItem: SafeAreaDto,
            newItem: SafeAreaDto
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: SafeAreaDto,
            newItem: SafeAreaDto
        ): Boolean {
            return oldItem == newItem
        }

    }) {
    private var safeDetailRVAClickListener: SafeDetailRVAClickListener? = null
    fun setSafeRVAClickListener(listener: SafeAreaDetailRVA.SafeDetailRVAClickListener){
        this.safeDetailRVAClickListener = listener
    }
    inner class SafeAreaDetailViewHolder(private val binding: ItemSafeAreaDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(safeAreaDetail: SafeAreaDto) {
            with(binding) {
                model = safeAreaDetail
                mapViewBtn.setOnClickListener {
                    safeDetailRVAClickListener?.onClickMapView(with(safeAreaDetail){ LatLng(latitude, longitude) })
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SafeAreaDetailViewHolder {
        return SafeAreaDetailViewHolder(
            ItemSafeAreaDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SafeAreaDetailViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    interface SafeDetailRVAClickListener{
        fun onClickMapView(latLng: LatLng)
    }
}