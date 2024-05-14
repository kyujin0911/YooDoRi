package kr.ac.tukorea.whereareu.presentation.nok

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PointF
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.MarkerIcons
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.ActivityNokMainBinding
import kr.ac.tukorea.whereareu.databinding.IconLocationOverlayLayoutBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseActivity
import kr.ac.tukorea.whereareu.presentation.nok.history.CalendarDialogFragment
import kr.ac.tukorea.whereareu.presentation.nok.history.LocationHistoryViewModel
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel
import kr.ac.tukorea.whereareu.util.extension.getUserKey
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.setMarker
import kr.ac.tukorea.whereareu.util.extension.setMarkerWithInfoWindow
import java.lang.IndexOutOfBoundsException
import kotlin.math.roundToInt

@AndroidEntryPoint
class NokMainActivity : BaseActivity<ActivityNokMainBinding>(R.layout.activity_nok_main),
    OnMapReadyCallback {
    private val homeViewModel: NokHomeViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private val locationHistoryViewModel: LocationHistoryViewModel by viewModels()
    private var updateLocationJob: Job? = null
    private var countDownJob: Job? = null
    private var naverMap: NaverMap? = null
    private val circleOverlay = CircleOverlay()
    private val path = PathOverlay()
    private val historyMarker = Marker()
    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var navController: NavController
    private val predictMarkers = mutableListOf<Marker>()
    private var zoom = 14.0
    private var currentProgress = 0
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
        repeatOnStarted {
            homeViewModel.navigateEvent.collect{
                Log.d("navigateEvent", it)
            }
        }

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

        repeatOnStarted {
            locationHistoryViewModel.locationHistory.collect {
                //delay(100)
                Log.d("history", it.toString())
                if (it.isEmpty()) {
                    return@collect
                }
                val latLngList = it.map { LatLng(it.latitude, it.longitude) }
                with(path) {
                    coords = latLngList
                    width = 30
                    color = ContextCompat.getColor(this@NokMainActivity, R.color.yellow)
                    patternImage = OverlayImage.fromResource(R.drawable.ic_arrow_up_white_24)
                    patternInterval = 50
                    outlineColor = Color.WHITE
                    map = naverMap
                }
                historyMarker.setMarkerWithInfoWindow(
                    context = this@NokMainActivity,
                    latLng = latLngList[0],
                    markerIconColor = MarkerIcons.BLUE,
                    "",
                    naverMap,
                    "현재 위치 기록"
                )
            }
        }
        repeatOnStarted {
            locationHistoryViewModel.progress.collect { progress ->
                Log.d("progress collect", progress.toString())
                if (progress == -1) {
                    return@collect
                }
                try {
                    val latLng = path.coords[progress]
                    val distance =
                        if (currentProgress - progress <= 0) naverMap?.cameraPosition?.target?.distanceTo(
                            path.coords[progress + 1]
                        )
                        else naverMap?.cameraPosition?.target?.distanceTo(path.coords[progress - 1])

                    var animation = CameraAnimation.Fly
                    var duration = 2000L
                    historyMarker.position = latLng
                    if (progress == 0) {
                        naverMap?.moveCamera(
                            CameraUpdate.scrollAndZoomTo(latLng, zoom)
                                .animate(animation, duration)
                        )
                    } else {
                        if (distance != null) {
                            when (distance) {
                                //이동 상태 정보도 받아와야 될듯
                                /*
                                거리에 따라 디테일 하게 줌 변경
                                in 0.0..0.01 -> {
                                    zoom = 19.0
                                    animation = CameraAnimation.Easing
                                    duration = 100L
                                }

                                in 400.0..Double.MAX_VALUE -> {
                                    zoom = 15.0
                                    animation = CameraAnimation.Easing
                                    duration = 1500L
                                }

                                in 100.0..399.9 -> {
                                    //zoom = 16.0
                                    animation = CameraAnimation.Easing
                                    duration = 500L
                                }

                                else -> {
                                    //zoom = 17.0
                                    animation = CameraAnimation.Easing
                                    duration = 500L
                                }*/
                                in 0.0..0.01 -> {
                                    zoom = 18.0
                                    animation = CameraAnimation.Easing
                                    duration = 100L
                                }

                                else -> {
                                    zoom = 15.0
                                    animation = CameraAnimation.Easing
                                    duration = 1000L
                                }
                            }
                            naverMap?.moveCamera(
                                CameraUpdate.scrollAndZoomTo(latLng, zoom)
                                    .animate(animation, duration)
                            )
                            Log.d("zoom", zoom.toString())
                        }
                        Log.d("distance", distance.toString())
                    }
                    //naverMap?.moveCamera(CameraUpdate.scrollAndZoomTo(latLng, 18.0))
                } catch (e: IndexOutOfBoundsException) {
                    Log.d("IndexOutOfBoundsException", e.toString())
                }
                currentProgress = progress
            }
        }

        /*repeatOnStarted {
            locationHistoryViewModel.isLoadingComplete.collect{ isLoading ->
                if (isLoading){
                    showLoadingDialog(this@NokMainActivity)
                } else {
                    delay(200)
                    dismissLoadingDialog()
                }
            }
        }*/
    }


    private fun handlePredictEvent(event: NokHomeViewModel.PredictEvent) {
        when (event) {
            // 예측 시작 -> 보호대상자 마지막 정보, 의미 장소 api 호출, 보호대상자 위치 업데이트 api 정지, 로딩화면 표시
            is NokHomeViewModel.PredictEvent.StartPredict -> {
                homeViewModel.predict()
                stopGetDementiaLocation()
                showLoadingDialog(this)
            }

            // 보호대상자 마지막 정보 UI 업데이트, 실종 시각 카운트다운 시작
            is NokHomeViewModel.PredictEvent.DisplayDementiaLastInfo -> {
                startCountDownJob(event.averageSpeed, event.coord)

                binding.averageMovementSpeedTv.text = String.format("%.2fkm", event.averageSpeed)

                // bottom sheet expanded offset 지정 및 높이 지정

                behavior.expandedOffset = binding.predictLayout.bottom + 20
                binding.bottomSheet.layoutParams.height =
                    binding.coordinatorLayout.height - binding.predictLayout.bottom
                naverMap?.uiSettings?.setLogoMargin(20, 0, 0, behavior.peekHeight + 15)
            }

            // 의미장소 마커 지도에 표시
            is NokHomeViewModel.PredictEvent.MeaningFulPlaceEvent -> {
                event.meaningfulPlaceForList.forEach { meaningfulPlace ->
                    predictMarkers.add(
                        Marker().apply {
                            setMarkerWithInfoWindow(
                                context = this@NokMainActivity,
                                latLng = meaningfulPlace.latLng,
                                markerIconColor = MarkerIcons.YELLOW,
                                markerText = meaningfulPlace.address,
                                naverMap = naverMap,
                                infoText = "예상 위치"
                            )
                        }
                    )
                    Log.d("markers 2", predictMarkers.toString())
                }
            }

            // 의미장소 주변 경찰서 마커 지도에 표시
            is NokHomeViewModel.PredictEvent.SearchNearbyPoliceStation -> {
                event.policeStationList.forEach { policeStation ->
                    predictMarkers.add(Marker().apply {
                        setMarker(
                            latLng = policeStation.latLng,
                            MarkerIcons.BLUE,
                            policeStation.policeName,
                            naverMap
                        )
                    })
                }

                dismissLoadingDialog()
            }

            // 보호대상자 마지막 위치 마커 지도에 표시
            is NokHomeViewModel.PredictEvent.DisplayDementiaLastLocation -> {
                binding.lastLocationTv.text = event.lastLocation.address

                naverMap?.moveCamera(CameraUpdate.scrollTo(event.lastLocation.latLng))

                predictMarkers.add(LAST_LOCATION, Marker().apply {
                    setMarkerWithInfoWindow(
                        context = this@NokMainActivity,
                        latLng = event.lastLocation.latLng,
                        markerIconColor = MarkerIcons.RED,
                        markerText = event.lastLocation.address,
                        naverMap = naverMap,
                        infoText = "실종 직전 위치"
                    )
                })
            }

            // 예측 중지, 실종 시각 카운트다운 중지
            is NokHomeViewModel.PredictEvent.StopPredict -> {
                lifecycleScope.launch {
                    countDownJob?.cancelAndJoin()
                    countDownJob = null
                    circleOverlay.isVisible = false
                    binding.countDownT.text = "00:00"
                }
                binding.bottomSheet.layoutParams.height = binding.coordinatorLayout.height
                behavior.expandedOffset = 0
                naverMap?.uiSettings?.setLogoMargin(20, 0, 0, 40)
                predictMarkers.forEach { marker ->
                    marker.map = null
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
        homeViewModel.setUpdateRate(0)
    }

    fun predict() {
        homeViewModel.setIsPredicted(true)
    }

    fun stopPredict() {
        homeViewModel.setIsPredicted(false)
    }

    override fun initView() {
        binding.view = this
        binding.viewModel = homeViewModel
        initBottomSheet()
        initMap()
        initNavigator()
    }

    private fun initMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync { map ->
            map.uiSettings.setLogoMargin(20, 0, 0, 40)
            naverMap = map
        }
    }

    private fun initBottomSheet() {
        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isFitToContents = false
        behavior.setPeekHeight(200, true)
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                if (slideOffset <= 0.3f) {
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
            var event: NokHomeViewModel.NavigateEvent = NokHomeViewModel.NavigateEvent.Home

            if (destination.id != R.id.nokHomeFragment) {
                stopGetDementiaLocation()
                Log.d("margin", naverMap?.uiSettings?.logoMargin?.contentToString().toString())
                //naverMap?.uiSettings?.setLogoMargin(20,0,0, behavior.peekHeight + 15)
                Log.d("margin", naverMap?.uiSettings?.logoMargin?.contentToString().toString())
                homeViewModel.setIsPredicted(false)
                naverMap?.uiSettings?.setLogoMargin(20, 0, 0, behavior.peekHeight + 15)
            }

            if (destination.id != R.id.locationHistoryFragment) {
                path.map = null
                historyMarker.map = null
            }

            if (destination.id != R.id.nokSettingFragment or R.id.modifyUserInfoFragment or R.id.settingUpdateTimeFragment) {
                behavior.isDraggable = true
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            when (destination.id) {
                R.id.nokHomeFragment -> {
                    Log.d("margin", naverMap?.uiSettings?.logoMargin?.contentToString().toString())
                    naverMap?.uiSettings?.setLogoMargin(20, 0, 0, 40)
                    event = NokHomeViewModel.NavigateEvent.Home
                    homeViewModel.fetchUserInfo()
                }

                R.id.nokSettingFragment, R.id.modifyUserInfoFragment, R.id.settingUpdateTimeFragment -> {
                    behavior.isDraggable = false
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    event = NokHomeViewModel.NavigateEvent.Setting
                }

                R.id.safeAreaFragment -> {
                    event = NokHomeViewModel.NavigateEvent.SafeArea
                }

                R.id.meaningfulPlaceFragment -> {
                    //binding.homeGroup.isVisible = false
                    event = NokHomeViewModel.NavigateEvent.MeaningfulPlace
                }

                R.id.locationHistoryFragment -> {
                    locationHistoryViewModel.setIstLoading(true)
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

    companion object {
        const val LAST_LOCATION = 0
    }
}