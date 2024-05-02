package kr.ac.tukorea.whereareu.presentation.nok.history

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.history.LocationHistoryRequest
import kr.ac.tukorea.whereareu.databinding.FragmentLocationHistoryBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment

@AndroidEntryPoint
class LocationHistoryFragment: BaseFragment<FragmentLocationHistoryBinding>(R.layout.fragment_location_history) {
    private val viewModel: LocationHistoryViewModel by viewModels()
    override fun initObserver() {

    }

    override fun initView() {
        viewModel.fetchLocationHistory("2024-03-19", "253050")
    }
}