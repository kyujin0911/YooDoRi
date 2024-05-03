package kr.ac.tukorea.whereareu.presentation.nok.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Build
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.IconLocationOverlayLayoutBinding
import kr.ac.tukorea.whereareu.domain.home.InnerItemClickEvent
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.PoliceStationRVA
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.MeaningfulPlaceRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToastShort
import kotlin.math.roundToInt


@AndroidEntryPoint
class NokHomeFragment : BaseFragment<kr.ac.tukorea.whereareu.databinding.FragmentHomeBinding>(R.layout.fragment_home),
    MeaningfulPlaceRVA.MeaningfulPlaceRVAClickListener, PoliceStationRVA.PoliceStationRVAClickListener {
    private val viewModel: NokHomeViewModel by activityViewModels()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val meaningfulPlaceRVA by lazy {
        MeaningfulPlaceRVA()
    }
    override fun initObserver() {
        repeatOnStarted {
            viewModel.predictEvent.collect{ predictEvent ->
                handlePredictEvent(predictEvent)
            }
        }
    }

    private fun handlePredictEvent(event: NokHomeViewModel.PredictEvent){
        when(event){
            is NokHomeViewModel.PredictEvent.StartPredict -> {
                initMeaningfulListRVA()
            }

            is NokHomeViewModel.PredictEvent.MeaningFulPlaceEvent -> {
                Log.d("뭐고", event.meaningfulPlaceForList.toString())
                meaningfulPlaceRVA.submitList(event.meaningfulPlaceForList)
            }

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
        meaningfulPlaceRVA.setRVAClickListener(this, this)
    }

    /*private fun initBottomSheet(){
        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.peekHeight = 20
        behavior.isFitToContents = false
        behavior.halfExpandedRatio = 0.3f

        //bottom sheet predict layout과 높이 맞추기
        *//*val viewTreeObserver: ViewTreeObserver = binding.predictLayout.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                behavior.expandedOffset = binding.predictLayout.height + 35
                binding.predictLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })*//*

        // half expanded state일 때 접기 제어
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            var isHalfExpanded = false
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                *//*when(newState){
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        isHalfExpanded = true
                    }
                    BottomSheetBehavior.STATE_COLLAPSED and BottomSheetBehavior.STATE_HALF_EXPANDED-> {
                        isHalfExpanded = false
                    }
                }*//*
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                *//*if(isHalfExpanded && slideOffset < 0.351f){
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }*//*
            }

        })
    }*/

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
    override fun onClickMoreView(policeStationInfo: PoliceStationInfo) {
        val coord = LatLng(policeStationInfo.latitude.toDouble(), policeStationInfo.longitude.toDouble())
        viewModel.eventInnerItemClick(InnerItemClickEvent(BottomSheetBehavior.STATE_COLLAPSED, coord))
    }

    override fun onClickCopyPhoneNumber(phoneNumber: String) {
        val clipboardManager = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", phoneNumber))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requireActivity().showToastShort(requireContext(), "전화번호가 복사되었습니다.")
        }
    }

    override fun onClickCopyAddress(address: String) {
        val clipboardManager = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", address))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requireActivity().showToastShort(requireContext(), "주소가 복사되었습니다.")
        }
    }

    override fun onClickMapView(latitude: Double, longitude: Double) {
        val coord = LatLng(latitude, longitude)
        viewModel.eventInnerItemClick(InnerItemClickEvent(BottomSheetBehavior.STATE_COLLAPSED, coord))
    }
}