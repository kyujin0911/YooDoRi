package kr.ac.tukorea.whereareu.presentation.nok.history.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistory
import kr.ac.tukorea.whereareu.data.model.nok.home.TimeInfo
import kr.ac.tukorea.whereareu.databinding.ItemLocationHistoryBinding
import kr.ac.tukorea.whereareu.databinding.ItemTimeInfoBinding
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.TimeInfoRVA

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
    inner class LocationHistoryViewHolder(private val binding: ItemLocationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(locationHistory: LocationHistory) {
            Log.d("location history info", locationHistory.toString())
            with(binding) {
                model = locationHistory
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
}