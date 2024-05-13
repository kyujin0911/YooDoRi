package kr.ac.tukorea.whereareu.presentation.nok.history.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryDto
import kr.ac.tukorea.whereareu.databinding.ItemLocationHistoryBinding
import kr.ac.tukorea.whereareu.domain.history.LocationHistory

class LocationHistoryRVA(): ListAdapter<LocationHistory, LocationHistoryRVA.LocationHistoryViewHolder>(
    object :
        DiffUtil.ItemCallback<LocationHistory>() {
        override fun areItemsTheSame(
            oldItem: LocationHistory,
            newItem: LocationHistory
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: LocationHistory,
            newItem: LocationHistory
        ): Boolean {
            return oldItem == newItem
        }

    }) {

    private var onLoadingListener: OnLoadingListener? = null
    fun setOnLoadingListener(listener: OnLoadingListener){
        this.onLoadingListener = listener
    }
    inner class LocationHistoryViewHolder(private val binding: ItemLocationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(locationHistory: LocationHistory) {
            Log.d("location history info", locationHistory.toString())
            with(binding) {
                model = locationHistory
            }
            if (locationHistory.isLast){
                onLoadingListener?.onLoading()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationHistoryViewHolder {
        return LocationHistoryViewHolder(
            ItemLocationHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LocationHistoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    interface OnLoadingListener{
        fun onLoading()
    }
}