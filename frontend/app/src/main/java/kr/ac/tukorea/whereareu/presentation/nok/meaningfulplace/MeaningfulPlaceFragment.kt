package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulPlaceBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter.MeaningfulPlaceRVAForPage
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class MeaningfulPlaceFragment :
    BaseFragment<FragmentMeaningfulPlaceBinding>(R.layout.fragment_meaningful_place),
    MeaningfulPlaceRVAForPage.MeaningfulPlaceRVAForPageClickListener {

    private val viewModel: MeaningfulPlaceViewModel by activityViewModels()
    private val tag = "MeaningfulPlaceFragment:"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObserver()
        viewModel.eventMeaningfulPlaceForPage()
    }

    private fun initRecyclerView() {
        val meaningfulPlaceAdapter = MeaningfulPlaceRVAForPage()
        binding.rv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = meaningfulPlaceAdapter
        }

        meaningfulPlaceAdapter.setRVAForPageClickListener(this)
    }

    override fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnStarted {
                viewModel.predictEvent.collect{predictEvent ->
                    handlePredictEvent(predictEvent)
                }
            }
            repeatOnStarted {
                viewModel.meaningfulPlace.collect { meaningfulPlaces ->
                    Log.d("$tag meaningfulPlace collect", meaningfulPlaces.toString())
                    (binding.rv.adapter as MeaningfulPlaceRVAForPage).submitList(meaningfulPlaces)
                }
            }
        }
    }

    private fun handlePredictEvent(event: MeaningfulPlaceViewModel.PredictEvent){
        when(event){
            is MeaningfulPlaceViewModel.PredictEvent.StartPredict ->{
                initMeaningfulListRVAForPage()
            }
        }
//        MeaningfulPlaceViewModel.PredictEvent.StartPredict -> {
//            initMeaninfulListRVAForPage()
//        }
    }

    override fun initView() {
    }

    override fun onClickMapView(latLng: LatLng) {
        Log.d(tag, "MapView button clicked: $latLng")
        homeViewModel.eventPredict(NokHomeViewModel.PredictEvent.MapView(BottomSheetBehavior.STATE_COLLAPSED, latLng))
    }

    override fun onClickInfoView(meaningfulPlace: MeaningfulPlaceInfo) {
//        Log.d(tag, "InfoView button clicked: ${meaningfulPlace.address}")
//        val action = MeaningfulPlaceFragmentDirections.actionMeaningfulPlaceFragmentToMeaningfulPlaceDetailFragment(meaningfulPlace)
//        findNavController().navigate(action)
    }
}
