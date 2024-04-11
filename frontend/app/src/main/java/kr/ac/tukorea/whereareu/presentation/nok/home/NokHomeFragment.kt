package kr.ac.tukorea.whereareu.presentation.nok.home

import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.PointF
import android.util.Log
import android.view.View
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
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
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
import kr.ac.tukorea.whereareu.data.model.nok.home.LocationInfoResponse
import kr.ac.tukorea.whereareu.databinding.IconLocationOverlayLayoutBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.MeaningfulListRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kotlin.math.roundToInt


@AndroidEntryPoint
class NokHomeFragment : BaseFragment<kr.ac.tukorea.whereareu.databinding.FragmentHomeBinding>(R.layout.fragment_home),
    OnMapReadyCallback {
    private val viewModel: NokHomeViewModel by activityViewModels()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var naverMap: NaverMap? = null
    private var dementiaName: String? = null
    private val lastLocationMarker = Marker()
    private val meaningfulListRVA by lazy {
        MeaningfulListRVA()
    }
    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    private var countDownJob: Job? = null
    override fun initObserver() {
        repeatOnStarted {
            viewModel.isPredicted.collect{ isPredicted ->
                if(isPredicted) {
                    //viewModel.getDementiaLastInfo()
                    viewModel.makeList()
                    initBottomSheet()
                    initMeaningfulListRVA()
                    //showLoadingDialog(requireContext())
                } else {
                    countDownJob?.cancelAndJoin()
                    binding.countDownT.text = "00:00"
                    lastLocationMarker.map = null
                }
            }
        }

        repeatOnStarted {
            viewModel.predictEvent.collect{ predictEvent ->
                handlePredictEvent(predictEvent)
            }
        }
    }

    private fun handlePredictEvent(event: NokHomeViewModel.PredictEvent){
        when(event){
            is NokHomeViewModel.PredictEvent.DementiaLastInfoEvent -> {
                binding.averageMovementSpeedTv.text = String.format("%.2fkm", event.dementiaLastInfo.averageSpeed)
                naverMap?.locationOverlay?.isVisible = false

            }
            is NokHomeViewModel.PredictEvent.MeaningFulPlaceEvent -> {
                meaningfulListRVA.submitList(event.meaningfulPlace)
                dismissLoadingDialog()
                startCountDown()
            }

            is NokHomeViewModel.PredictEvent.LastLocationEvent -> {
                val latitude = event.lastAddress.latitude
                val longitude = event.lastAddress.longitude
                naverMap?.moveCamera(CameraUpdate.scrollTo(LatLng(latitude, longitude)))
                with(lastLocationMarker){
                    position = LatLng(latitude, longitude)
                    icon = MarkerIcons.RED
                    captionText = event.lastAddress.address
                    captionRequestedWidth = 400
                    map = naverMap
                }

                val infoWindow = InfoWindow()
                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(requireContext()) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "실종 직전 위치"
                    }
                }
                infoWindow.open(lastLocationMarker)
            }
        }
    }

    private fun updateDementiaMovementStatus(status: Int): String{
        return when(status){
            1 -> "정지"
            2 -> "도보"
            3 -> "차량"
            4 -> "지하철"
            else -> "알수없음"
        }
    }

    private fun updateDementiaStatus(dementiaStatus: LocationInfoResponse){
        with(binding){
            stateTv.text = updateDementiaMovementStatus(dementiaStatus.userStatus)
            batteryTv.text = "${dementiaStatus.battery}%"

            when(dementiaStatus.isRingstoneOn){
                0 -> {
                    ringModeTv.text = "무음"
                    ringModeIv.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_bell_off_24))
                }

                1 -> {
                    ringModeTv.text = "진동"
                    ringModeIv.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vibrate_24))
                }

                2 -> {
                    ringModeTv.text = "벨소리"
                    ringModeIv.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_bell_24))
                }

                else -> {
                    ringModeTv.text = "알수없음"
                }
            }
        }
    }

    private fun trackingDementiaLocation(coord: LatLng, bearing: Float, name: String, speed: Float){
        naverMap?.let {
            val locationOverlay = it.locationOverlay
            val iconBinding = IconLocationOverlayLayoutBinding.inflate(layoutInflater)
            val icon = iconBinding.layout

            with(locationOverlay){
                isVisible = true
                // m/s to km/h
                iconBinding.speedTv.text = (speed * 3.6).roundToInt().toString()
                iconBinding.nameTv.text = name
                locationOverlay.icon = OverlayImage.fromView(icon)
                circleRadius = 0
                position = coord
                anchor = PointF(0.5f, 1f)
            }

            it.moveCamera(CameraUpdate.scrollTo(coord))
        }
    }

    override fun initView() {
        binding.view = this
        binding.viewModel = viewModel
        checkLocationPermission()
        updateDementiaName()
        initMap()
    }

    fun predict(){
        viewModel.setIsPredicted(true)
    }

    fun stopPredict(){
        viewModel.setIsPredicted(false)
    }

    private fun initMeaningfulListRVA(){
        binding.rv.apply {
            adapter = meaningfulListRVA
            addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )}
        /*val list = listOf<MeaningfulPlace>(
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
            MeaningfulPlace("화", "0408", "시흥시"),
        )
        meaningfulListRVA.submitList(list)*/
    }

    private fun initBottomSheet(){
        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.peekHeight = 20
        behavior.isFitToContents = false
        behavior.halfExpandedRatio = 0.3f

        //bottom sheet predict layout과 높이 맞추기
        /*val viewTreeObserver: ViewTreeObserver = binding.predictLayout.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                behavior.expandedOffset = binding.predictLayout.height + 35
                binding.predictLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })*/

        // half expanded state일 때 접기 제어
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            var isHalfExpanded = false
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                /*when(newState){
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        isHalfExpanded = true
                    }
                    BottomSheetBehavior.STATE_COLLAPSED and BottomSheetBehavior.STATE_HALF_EXPANDED-> {
                        isHalfExpanded = false
                    }
                }*/
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                /*if(isHalfExpanded && slideOffset < 0.351f){
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }*/
            }

        })
    }

    private fun startCountDown(){
        countDownJob = lifecycleScope.launch {
            var second = 0
            var minute = 0
            while (true) {
                second+=1
                if(second % 60 == 0){
                    minute += 1
                    second = 0
                }
                binding.countDownT.text = String.format("%02d:%02d",minute, second)
                delay(1000L)
            }
        }
    }

    private fun updateDementiaName(){
        val spf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
        dementiaName = spf.getString("name", "")
        if (!dementiaName.isNullOrBlank()){
            binding.dementiaNameTv.text = dementiaName
        }
    }
    private fun initMap() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync { map ->
            naverMap = map
        }
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

    override fun onResume() {
        super.onResume()
        Log.d("resume", "resume")
        repeatOnStarted {
            delay(500)
            viewModel.dementiaLocation.collect{ response ->
                Log.d("response", response.toString())
                updateDementiaStatus(response)
                val coord = LatLng(response.latitude, response.longitude)
                trackingDementiaLocation(coord, response.bearing, dementiaName ?: "", response.currentSpeed)
            }
        }
    }

    override fun onMapReady(p0: NaverMap) {
    }
}