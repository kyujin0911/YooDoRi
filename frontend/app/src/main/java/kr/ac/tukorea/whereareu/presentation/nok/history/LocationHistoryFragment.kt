package kr.ac.tukorea.whereareu.presentation.nok.history

import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistory
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryRequest
import kr.ac.tukorea.whereareu.databinding.FragmentLocationHistoryBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import java.lang.IndexOutOfBoundsException

@AndroidEntryPoint
class LocationHistoryFragment :
    BaseFragment<FragmentLocationHistoryBinding>(R.layout.fragment_location_history){
    private val viewModel: LocationHistoryViewModel by activityViewModels()
    override fun initObserver() {
        repeatOnStarted {
            viewModel.locationHistory.collect { list ->
                syncSeekBarWithLocationHistory(list)
            }
        }
    }

    override fun initView() {
        binding.viewModel = viewModel

        viewModel.fetchLocationHistory("2024-03-19", "253050")
        binding.next.setOnClickListener {
            binding.seekBar.progress += 1
            viewModel.setProgress(binding.seekBar.progress)
        }
        binding.previous.setOnClickListener {
            binding.seekBar.progress -= 1
            viewModel.setProgress(binding.seekBar.progress)
        }
    }

    private fun syncSeekBarWithLocationHistory(locationHistoryList: List<LocationHistory>) {
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("progress", progress.toString())
                try {
                    val locationInfo = locationHistoryList[progress]
                    Log.d("seek bar location info", locationInfo.toString())
                    viewModel.setProgress(progress)
                } catch (e: IndexOutOfBoundsException){
                    Log.d("IndexOutOfBoundsException", e.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

}