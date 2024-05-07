package kr.ac.tukorea.whereareu.presentation.nok

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginBottom
import androidx.core.view.setMargins
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
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
import kr.ac.tukorea.whereareu.databinding.ActivityNokMainBinding
import kr.ac.tukorea.whereareu.databinding.IconLocationOverlayLayoutBinding
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.presentation.base.BaseActivity
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.PoliceStationRVA
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel
import kr.ac.tukorea.whereareu.util.extension.getUserKey
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kotlin.math.roundToInt

@AndroidEntryPoint
class NokMainActivity : BaseActivity<ActivityNokMainBinding>(R.layout.activity_nok_main),
    OnMapReadyCallback, PoliceStationRVA.PoliceStationRVAClickListener {
    private val homeViewModel: NokHomeViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private var updateLocationJob: Job? = null
    private var countDownJob: Job? = null
    private var naverMap: NaverMap? = null
    private val lastLocationMarker = Marker()
    private val circleOverlay = CircleOverlay()
    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var navController: NavController
    private fun saveUserKeys() {
        val dementiaKey = getUserKey("dementia")
        homeViewModel.setDementiaKey(dementiaKey)

        val nokKey = getUserKey("nok")
        homeViewModel.setNokKey(nokKey)
        settingViewModel.setNokKey(nokKey)
    }

    private fun getUpdateLocationJob(duration: Long): Job {
        return lifecycleScope.launch {
            while (true) {
                homeViewModel.getDementiaLocation()
                delay(duration)
            }
        }
    }

    override fun initObserver() {
        saveUserKeys()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver, IntentFilter("gps")
        )

        // 앱 처음 실행, 예측 중지 시 보호대상자 위치를 갖고 오는 job이 없으면 새로운 job을 생성해서 실행
        repeatOnStarted {
            homeViewModel.updateRate.collect { updateRate ->
                Log.d("home updatteRate", updateRate.toString())
                if (updateRate == 0L) {
                    return@collect
                }

                // 위치 업데이트 주기 변경 시 기존 job을 취소하고 updateRate에 맞게 재시작
                if (updateLocationJob != null) {
                    Log.d("job home cancel", "cc")
                    updateLocationJob?.cancelAndJoin()
                }
                updateLocationJob = getUpdateLocationJob(updateRate.times(60 * 1000))
            }
        }

        repeatOnStarted {
            homeViewModel.isPredicted.collect {
                Log.d("homeViewModel", it.toString())
            }
        }

        // 보호 대상자 위치 UI 업데이트
        repeatOnStarted {
            delay(100)
            homeViewModel.dementiaLocationInfo.collect { response ->
                Log.d("dementiaLocation response", response.toString())
                //예측 기능 사용시 보호대상자 위치 UI 업데이트 X
                if (homeViewModel.isPredicted.value || navController.currentDestination?.id != R.id.nokHomeFragment) {
                    return@collect
                }
                val coord = LatLng(response.latitude, response.longitude)
                initLocationOverlay(coord, response.currentSpeed)
            }
        }

        // 예측 기능 실행
        repeatOnStarted {
            homeViewModel.predictEvent.collect { predictEvent ->
                handlePredictEvent(predictEvent)
            }
        }


        // 리사이클러뷰 아이템 클릭 이벤트에 따른 bottomSheet, Naver Map 제어
        repeatOnStarted {
            homeViewModel.innerItemClickEvent.collect { event ->
                behavior.state = event.behavior
                naverMap?.moveCamera(CameraUpdate.scrollTo(event.coord))
            }
        }
    }

    private fun handlePredictEvent(event: NokHomeViewModel.PredictEvent) {
        when (event) {
            is NokHomeViewModel.PredictEvent.StartPredict -> {
                homeViewModel.predict()
                stopGetDementiaLocation()
                //binding.bottomSheet.visibility = View.VISIBLE
                showLoadingDialog(this)
            }

            is NokHomeViewModel.PredictEvent.DisplayDementiaLastInfo -> {
                startCountDownJob(event.averageSpeed, event.coord)

                binding.averageMovementSpeedTv.text = String.format("%.2fkm", event.averageSpeed)
                Log.d("predictLayout", binding.predictLayout.bottom.toString())
                //binding.bottomSheet.layoutParams = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, binding.bottomSheet.height - binding.predictLayout.bottom + 20)
                behavior.expandedOffset = binding.predictLayout.bottom + 20
                binding.bottomSheet.layoutParams.height = binding.coordinatorLayout.height - binding.predictLayout.bottom
            }
            is NokHomeViewModel.PredictEvent.MeaningFulPlaceEvent -> {

                event.meaningfulPlaceForList.forEach { meaningfulPlace ->
                    val latitude = meaningfulPlace.latitude
                    val longitude = meaningfulPlace.longitude

                    val marker = Marker()
                    with(marker) {
                        position = LatLng(latitude, longitude)
                        icon = MarkerIcons.YELLOW
                        captionText = meaningfulPlace.address
                        captionRequestedWidth = 400
                        map = naverMap
                    }

                    val infoWindow = InfoWindow()
                    infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this) {
                        override fun getText(infoWindow: InfoWindow): CharSequence {
                            return "예상 위치"
                        }
                    }
                    infoWindow.open(marker)
                }
            }

            is NokHomeViewModel.PredictEvent.SearchNearbyPoliceStation -> {
                event.policeStationList.forEach { policeStation ->
                    val marker = Marker()
                    with(marker) {
                        position = LatLng(
                            policeStation.latitude.toDouble(),
                            policeStation.longitude.toDouble()
                        )
                        icon = MarkerIcons.BLUE
                        captionText = policeStation.policeName
                        captionRequestedWidth = 400
                        map = naverMap
                    }
                }

                dismissLoadingDialog()
            }

            is NokHomeViewModel.PredictEvent.DisplayDementiaLastLocation -> {
                binding.lastLocationTv.text = event.lastLocation.address

                val latitude = event.lastLocation.latitude
                val longitude = event.lastLocation.longitude
                naverMap?.moveCamera(CameraUpdate.scrollTo(LatLng(latitude, longitude)))

                with(lastLocationMarker) {
                    position = LatLng(latitude, longitude)
                    icon = MarkerIcons.RED
                    captionText = event.lastLocation.address
                    captionRequestedWidth = 400
                    map = naverMap
                }

                val infoWindow = InfoWindow()
                infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this) {
                    override fun getText(infoWindow: InfoWindow): CharSequence {
                        return "실종 직전 위치"
                    }
                }
                infoWindow.open(lastLocationMarker)
            }

            is NokHomeViewModel.PredictEvent.StopPredict -> {
                lifecycleScope.launch {
                    countDownJob?.cancelAndJoin()
                    countDownJob = null
                    circleOverlay.isVisible = false
                    binding.countDownT.text = "00:00"
                    lastLocationMarker.map = null
                }
            }
        }
    }

    private fun initLocationOverlay(coord: LatLng, speed: Float) {
        val binding = IconLocationOverlayLayoutBinding.inflate(layoutInflater)
        val view = binding.layout
        naverMap?.let {
            val locationOverlay = it.locationOverlay
            with(locationOverlay) {
                isVisible = true

                // m/s to km/h
                binding.speedTv.text = (speed * 3.6).roundToInt().toString()
                binding.nameTv.text = homeViewModel.dementiaName.value
                circleRadius = 0
                position = coord
                anchor = PointF(0.5f, 1f)
                icon = OverlayImage.fromView(view)
            }

            it.moveCamera(CameraUpdate.scrollTo(coord))
        }
    }

    private fun startCountDownJob(averageSpeed: Double, coord: LatLng) {
        with(circleOverlay) {
            center = coord
            color = ContextCompat.getColor(this@NokMainActivity, R.color.purple)
            outlineWidth = 5
            outlineColor = ContextCompat.getColor(this@NokMainActivity, R.color.deep_purple)
            radius = 0.0
        }

        countDownJob = lifecycleScope.launch {
            var second = 0
            var minute = 0
            while (true) {
                second += 1
                circleOverlay.radius += averageSpeed
                circleOverlay.map = naverMap
                if (second % 60 == 0) {
                    minute += 1
                    second = 0
                }
                binding.countDownT.text = String.format("%02d:%02d", minute, second)
                delay(1000L)
            }
        }
    }

    private fun stopGetDementiaLocation() {
        lifecycleScope.launch {
            Log.d("NokMainActivity", "stopGetDementiaLocation")
            updateLocationJob?.cancelAndJoin()
        }
        naverMap?.locationOverlay?.isVisible = false
        //homeViewModel.setIsPredicted(false)
        homeViewModel.setUpdateRate(0)
    }

    fun predict() {
        homeViewModel.setIsPredicted(true)
    }

    fun stopPredict() {
        homeViewModel.setIsPredicted(false)
    }

    override fun initView() {
        //상태바 투명 설정
        binding.view = this
        binding.viewModel = homeViewModel
        initMap()
        initBottomSheet()
        initNavigator()
    }

    private fun initMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync { map ->
            naverMap = map
        }

    }

    private fun initBottomSheet() {
        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        //behavior.peekHeight = 10
        //behavior.peekHeight = binding.
        //behavior.halfExpandedRatio = 0.3f
        behavior.isFitToContents = false
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                if(slideOffset <= 0.3f) {
                    binding.layout.translationY = -slideOffset * bottomSheet.height
                }
            }
        })
    }

    private fun initNavigator() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Log.d("current destination", R.id.settingUpdateTimeFragment.toString())
            Log.d("destination", destination.id.toString())
            var event: NokHomeViewModel.NavigateEvent = NokHomeViewModel.NavigateEvent.Home
            when (destination.id) {
                R.id.nokHomeFragment -> {
                    behavior.isDraggable = true
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    event = NokHomeViewModel.NavigateEvent.Home
                    homeViewModel.fetchUserInfo()
                }

                R.id.nokSettingFragment, R.id.modifyUserInfoFragment, R.id.settingUpdateTimeFragment -> {
                    Log.d("in setting tab", "dd")

                    behavior.isDraggable = false
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    stopGetDementiaLocation()
                    event = NokHomeViewModel.NavigateEvent.Setting
                }

                R.id.safeAreaFragment -> {
                    behavior.isDraggable = true
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    stopGetDementiaLocation()
                    event = NokHomeViewModel.NavigateEvent.SafeArea
                }

                R.id.meaningfulPlaceFragment -> {
                    behavior.isDraggable = true
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    stopGetDementiaLocation()
                    event = NokHomeViewModel.NavigateEvent.MeaningfulPlace
                }

                R.id.locationHistoryFragment -> {
                    behavior.isDraggable = true
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    stopGetDementiaLocation()
                    event = NokHomeViewModel.NavigateEvent.LocationHistory
                }
            }
            homeViewModel.eventNavigate(event)
        }
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val location = intent?.getDoubleArrayExtra("location")
            //val long = intent?.getDoubleExtra("long", 0.0)
            Log.d("location log", "${location?.get(0)}, ${location?.get(1)}")
            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    override fun onMapReady(p0: NaverMap) {

    }

    override fun onClickMoreView(policeStationInfo: PoliceStationInfo) {
        Log.d("d","d")
    }

    override fun onClickCopyPhoneNumber(phoneNumber: String) {

    }

    override fun onClickCopyAddress(address: String) {

    }
}