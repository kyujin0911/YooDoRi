package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.flow.collect
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulPlaceBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.MeaningfulPlaceRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

class MeaningfulPlaceFragment :
    BaseFragment<FragmentMeaningfulPlaceBinding>(R.layout.fragment_meaningful_place),
    MeaningfulPlaceRVA.MeaningfulPlaceRVAClickListener {
        private val viewModel: NokHomeViewModel by activityViewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val meaningfulPlaceRVA by lazy{
        MeaningfulPlaceRVA()
    }
    private val navigator: NavController by lazy{
        findNavController()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initView()
    }

//    override fun initObserver() {
//        repeatOnStarted {
//            viewModel.predictEvent.collect{predictEvent ->
//                handlePredictEvent(predictEvent)
//            }
//        }
//
//    }

//    private fun handlePredictEvent(event: NokHomeViewModel.PredictEvent){
//        when(event){
//            is NokHomeViewModel.PredictEvent.StartPredict -> {
//                initMeaningfulListRVA()
//            }
//            is NokHomeViewModel.PredictEvent.PredictLocation ->{
//                with(event.predictLocation){
//                    val address = meaningfulPlaceInfo.address
//                    meaningfulPlaceInfo.latLng
//                    binding.addressTV.text = address
//                }
//            }
//        }
//    }

//    override fun initView() {
//
//    }
//
//    override fun onClickMapView(latLng: LatLng) {
//
//    }
//
//    override fun onClickInfoView(meaningfulPlace: MeaningfulPlaceInfo) {
//
//    }

    override fun initObserver() {
        repeatOnStarted {
            viewModel.meaningfulPlace.collect {
                meaningfulPlaceRVA.submitList(it)
            }
        }
    }

    override fun initView() {
        binding.view = this
        binding.viewModel = viewModel
        initMeaningfulListRVA()
    }

    private fun initMeaningfulListRVA(){
        binding.rv.adapter = meaningfulPlaceRVA
        meaningfulPlaceRVA.setRVAClickListener(this)
    }

    override fun onClickMapView(latLng: LatLng) {
        viewModel.eventPredict(NokHomeViewModel.PredictEvent.MapView(BottomSheetBehavior.STATE_COLLAPSED, latLng))
    }

    override fun onClickInfoView(meaningfulPlace: MeaningfulPlaceInfo) {
        val action = MeaningfulPlaceFragmentDirections.actionMeaningfulPlaceFragmentToMeaningfulPlaceDetailFragmentForPage(
            meaningfulPlace
        )
        navigator.navigate(action)
    }
}