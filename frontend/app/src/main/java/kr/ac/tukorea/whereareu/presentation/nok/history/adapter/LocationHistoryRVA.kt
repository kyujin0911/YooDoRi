package kr.ac.tukorea.whereareu.presentation.nok.history.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryDto
import kr.ac.tukorea.whereareu.databinding.ItemLocationHistoryBinding
import kr.ac.tukorea.whereareu.databinding.ItemLocationHistoryStopStatusBinding
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
            Log.d("bind item", locationHistory.toString())
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
        if(currentList[position].isLast){
            onLoadingListener?.onLoading()
            Log.d("bind last", "bind last")
        }
        holder.bind(currentList[position])
    }

    interface OnLoadingListener{
        fun onLoading()
    }
}
/*
class LocationHistoryRVA(): ListAdapter<LocationHistory, RecyclerView.ViewHolder>(
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

    inner class LocationHistoryStopStatusViewHolder(private val binding: ItemLocationHistoryStopStatusBinding) :
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
    ): RecyclerView.ViewHolder {
        when(viewType){
            LocationHistory.OTHER_STATUS -> {
                return LocationHistoryViewHolder(
                    ItemLocationHistoryBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                return LocationHistoryStopStatusViewHolder(
                    ItemLocationHistoryStopStatusBinding.inflate(
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
            LocationHistory.OTHER_STATUS -> {
                (holder as LocationHistoryViewHolder).bind(currentList[position])
            }
            else -> {
                (holder as LocationHistoryStopStatusViewHolder).bind(currentList[position])
            }
        }
    }

    interface OnLoadingListener{
        fun onLoading()
    }
}*/
