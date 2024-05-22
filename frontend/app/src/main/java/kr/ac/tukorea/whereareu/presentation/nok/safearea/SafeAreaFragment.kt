package kr.ac.tukorea.whereareu.presentation.nok.safearea

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.databinding.FragmentSafeAreaBinding
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter.SafeAreaRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class SafeAreaFragment : BaseFragment<FragmentSafeAreaBinding>(R.layout.fragment_safe_area), SafeAreaRVA.SafeRVAClickListener {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val safeAreaRVA: SafeAreaRVA by lazy {
        SafeAreaRVA()
    }
    override fun initObserver() {
        repeatOnStarted {
            viewModel.safeAreaEvent.collect{event ->
                handleSafeAreaEvent(event)
            }
        }
    }
    override fun initView() {
        initSafeAreaRVA()
        /*viewModel.registerSafeArea(
            RegisterSafeAreaRequest(
                "253050",
                "테스트 그룹",
                "테스트 지역",
                37.4016759,
                126.9341057,
                100
            )
        )*/
        /*binding.tv.setOnClickListener {
            navigator.navigate(R.id.safeAreaDetailFragment)
        }*/

    }

    override fun onResume() {
        super.onResume()
        showLoadingDialog(requireContext(), "안심구역을 불러오고 있습니다.")
        viewModel.fetchSafeArea()
    }

    private fun initSafeAreaRVA(){
        binding.rv.adapter = safeAreaRVA
        safeAreaRVA.setSafeRVAClickListener(this)
        /*val list = listOf(
            SafeArea("그룹 1", "", 0.0, 0.0, 0, SafeAreaRVA.SAFE_AREA_GROUP),
            SafeArea("그룹 2", "", 0.0, 0.0, 0, SafeAreaRVA.SAFE_AREA_GROUP),
            SafeArea("", "안심구역 1", 37.3397811, 126.7348403, 10, SafeAreaRVA.SAFE_AREA),
            SafeArea("", "안심구역 2", 37.3397604, 126.7349086, 50, SafeAreaRVA.SAFE_AREA),
        )*/
    }

    private fun handleSafeAreaEvent(event: SafeAreaViewModel.SafeAreaEvent){
        when(event){
            is SafeAreaViewModel.SafeAreaEvent.FetchSafeArea -> {
                safeAreaRVA.submitList(event.safeAreas, kotlinx.coroutines.Runnable {
                    dismissLoadingDialog()
                })
            }

            is SafeAreaViewModel.SafeAreaEvent.MapView -> {}
        }
    }

    override fun onClickMapView(latLng: LatLng) {
        viewModel.eventSafeArea(SafeAreaViewModel.SafeAreaEvent.MapView(BottomSheetBehavior.STATE_COLLAPSED, latLng))
    }

    override fun onClickInfoView(safeArea: SafeArea) {

    }
}