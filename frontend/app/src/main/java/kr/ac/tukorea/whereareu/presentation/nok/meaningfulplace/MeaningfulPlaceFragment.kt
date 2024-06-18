package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
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
    private val meaningfulPlaceRVAForPage by lazy{
        MeaningfulPlaceRVAForPage()
    }
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val tag = "MeaningfulPlaceFragment:"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObserver()
        viewModel.meaningful()
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
                viewModel.meaningEvent.collect{meaningfulEvent ->
                    handlePredictEvent(meaningfulEvent)
                }
            }
            repeatOnStarted {
                viewModel.meaningfulPlace.collect{
                    meaningfulPlaceRVAForPage.submitList(it)
                }
            }
        }
    }

    private fun handlePredictEvent(event: MeaningfulPlaceViewModel.MeaningfulEvent){
        when(event){
            is MeaningfulPlaceViewModel.MeaningfulEvent.StartMeaningful ->{
                initMeaningfulListRVAForPage()
            }
            else -> {}
        }
    }

    override fun initView() {
        binding.view = this
        binding.viewModel = viewModel
//        checkLocationPermission()
    }


    private fun initMeaningfulListRVAForPage(){
        binding.rv.adapter = meaningfulPlaceRVAForPage
        meaningfulPlaceRVAForPage.setRVAForPageClickListener(this)
    }

    override fun onClickMapView(latLng: LatLng) {
        Log.d(tag, "MapView button clicked: $latLng")
        viewModel.eventPredict(MeaningfulPlaceViewModel.MeaningfulEvent.MapView(BottomSheetBehavior.STATE_COLLAPSED, latLng))
    }

    override fun onClickInfoView(meaningfulPlace: MeaningfulPlaceInfo) {
        val action = MeaningfulPlaceFragmentDirections.actionMeaningfulPlaceFragmentToMeaningfulPlaceDetailForPageFragment(
            meaningfulPlace
        )
        navigator.navigate(action)
    }

}
