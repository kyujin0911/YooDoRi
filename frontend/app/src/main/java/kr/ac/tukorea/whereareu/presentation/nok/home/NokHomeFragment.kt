package kr.ac.tukorea.whereareu.presentation.nok.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.home.TimeInfo
import kr.ac.tukorea.whereareu.databinding.FragmentHomeBinding
import kr.ac.tukorea.whereareu.domain.home.MeaningfulPlaceInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.PoliceStationRVA
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.MeaningfulPlaceRVA
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.TimeInfoRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToastShort


@AndroidEntryPoint
class NokHomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home),
    MeaningfulPlaceRVA.MeaningfulPlaceRVAClickListener {
    private val viewModel: NokHomeViewModel by activityViewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val meaningfulPlaceRVA by lazy {
        MeaningfulPlaceRVA()
    }
    private val navigator: NavController by lazy {
        findNavController()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initView()
    }
    override fun initObserver() {
        repeatOnStarted {
            viewModel.predictEvent.collect{ predictEvent ->
                handlePredictEvent(predictEvent)
            }
        }

        repeatOnStarted {
            viewModel.meaningfulPlace.collect{
                meaningfulPlaceRVA.submitList(it)
            }
        }
    }

    private fun handlePredictEvent(event: NokHomeViewModel.PredictEvent){
        when(event){
            is NokHomeViewModel.PredictEvent.StartPredict -> {
                initMeaningfulListRVA()
            }

            /*is NokHomeViewModel.PredictEvent.MeaningFulPlace -> {
                if(meaningfulPlaceRVA.currentList.isEmpty()) {
                    Log.d("ds", "isEmpty")
                    meaningfulPlaceRVA.submitList(event.meaningfulPlaceForList)
                }
                else{
                    Log.d("ds", "isNotEmpty")
                }
            }*/

            else -> {}
        }
    }

    override fun initView() {
        binding.view = this
        binding.viewModel = viewModel
        checkLocationPermission()
    }

    private fun initMeaningfulListRVA(){
        binding.rv.apply {
            adapter = meaningfulPlaceRVA
            addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )}
        meaningfulPlaceRVA.setRVAClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.eventMeaningfulPlace()
        initMeaningfulListRVA()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한이 이미 허용된 경우 위치 업데이트 요청
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 위치 권한이 허용된 경우 위치 업데이트 요청
                } else {
                    // 권한이 거부된 경우 처리 (예: 사용자에게 권한이 필요하다고 알리기)
                }
            }
        }
    }


    // inner RVA 클릭 이벤트
    override fun onClickMapView(latLng: LatLng) {
        viewModel.eventPredict(NokHomeViewModel.PredictEvent.RVAClick(BottomSheetBehavior.STATE_COLLAPSED, latLng))
    }

    override fun onClickInfoView(meaningfulPlace: MeaningfulPlaceInfo) {
        val action = NokHomeFragmentDirections.actionNokHomeFragmentToMeaningfulPlaceDetailFragment(
            meaningfulPlace
        )
        navigator.navigate(action)
    }
}