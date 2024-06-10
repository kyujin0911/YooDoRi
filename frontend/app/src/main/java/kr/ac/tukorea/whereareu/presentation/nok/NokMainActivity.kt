package kr.ac.tukorea.whereareu.presentation.nok

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PointF
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
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
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.ZoomControlView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.ActivityNokMainBinding
import kr.ac.tukorea.whereareu.databinding.IconLocationOverlayLayoutBinding
import kr.ac.tukorea.whereareu.domain.history.LocationHistory
import kr.ac.tukorea.whereareu.domain.history.LocationHistoryMetaData
import kr.ac.tukorea.whereareu.domain.home.PredictMetaData
import kr.ac.tukorea.whereareu.domain.safearea.SafeAreaMetaData
import kr.ac.tukorea.whereareu.presentation.base.BaseActivity
import kr.ac.tukorea.whereareu.presentation.nok.history.LocationHistoryViewModel
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.safearea.SafeAreaDetailFragmentDirections
import kr.ac.tukorea.whereareu.presentation.nok.safearea.SafeAreaViewModel
import kr.ac.tukorea.whereareu.presentation.nok.safearea.SelectGroupDialogFragment
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel
import kr.ac.tukorea.whereareu.util.extension.EditTextUtil.setOnEditorActionListener
import kr.ac.tukorea.whereareu.util.extension.getUserKey
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.setInfoWindowText
import kr.ac.tukorea.whereareu.util.extension.setMarker
import kr.ac.tukorea.whereareu.util.extension.setMarkerWithInfoWindow
import kr.ac.tukorea.whereareu.util.extension.setPath
import kotlin.math.roundToInt


@AndroidEntryPoint
class NokMainActivity : BaseActivity<ActivityNokMainBinding>(R.layout.activity_nok_main),
    OnMapReadyCallback {
    private val homeViewModel: NokHomeViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private val locationHistoryViewModel: LocationHistoryViewModel by viewModels()
    private val safeAreaViewModel: SafeAreaViewModel by viewModels()

    private var updateLocationJob: Job? = null
    private var countDownJob: Job? = null
    private var naverMap: NaverMap? = null
    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var navController: NavController
    private val predictMetaData = PredictMetaData()
    private val locationHistoryMetaData = LocationHistoryMetaData()
    private val safeAreMetaData = SafeAreaMetaData()
    private val tag = "NokMainActivity:"
    private val isFirstNavigationEvent = mutableListOf(true, true, true)
    private var isRequireStopHomeFragmentJob = true

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
            homeViewModel.navigateEvent.collect { event ->
                Log.d("$tag navigateEvent collect", event.toString())
                handleNavigationEvent(event)
            }
        }

        repeatOnStarted {
            homeViewModel.navigateEventToString.collect {
                Log.d("naviate to string", it)
            }
        }

        // 앱 처음 실행, 예측 중지 시 보호대상자 위치를 갖고 오는 job이 없으면 새로운 job을 생성해서 실행
        repeatOnStarted {
            homeViewModel.updateRate.collect { updateRate ->
                Log.d("$tag updateRate collect", updateRate.toString())
                if (updateRate == 0L) {
                    return@collect
                }

                // 위치 업데이트 주기 변경 시 기존 job을 취소하고 updateRate에 맞게 재시작
                if (updateLocationJob != null) {
                    Log.d("$tag updateRate restart", updateRate.toString())
                    updateLocationJob?.cancelAndJoin()
                }
                updateLocationJob = getUpdateLocationJob(updateRate.times(60 * 1000))
            }
        }

        // 보호 대상자 위치 UI 업데이트
        repeatOnStarted {
            delay(100)
            homeViewModel.dementiaLocationInfo.collect { response ->
                Log.d("$tag dementiaLocationInfo collect", response.toString())

                //예측 기능 사용시 보호대상자 위치 UI 업데이트 X
                if (navController.currentDestination?.id != R.id.nokHomeFragment) {
                    return@collect
                }
                val coord = LatLng(response.latitude, response.longitude)
                initLocationOverlay(coord, response.currentSpeed)
            }
        }

        // 예측 기능 실행
        repeatOnStarted {
            homeViewModel.predictEvent.collect { event ->
                Log.d("$tag predictEvent collect", event.toString())
                handlePredictEvent(event)
            }
        }

        repeatOnStarted {
            locationHistoryViewModel.locationHistoryEvent.collect { event ->
                Log.d("$tag locationHistoryEvent collect", event.toString())
                handleLocationHistoryEvent(event)
            }
        }

        repeatOnStarted {
            safeAreaViewModel.safeAreaEvent.collect { event ->
                handleSafeAreaEvent(event)
            }
        }

        repeatOnStarted {
            homeViewModel.isPredicted.collect {
                Log.d("isPredicted", it.toString())
            }
        }
    }

    private fun handleSafeAreaEvent(event: SafeAreaViewModel.SafeAreaEvent) {
        when (event) {
            is SafeAreaViewModel.SafeAreaEvent.FetchSafeArea -> {
                /*event.safeAreas.forEach { safeArea ->
                    with(safeArea) {
                        val latLng = LatLng(latitude, longitude)
                        safeAreMetaData.markers.add(
                            Marker().apply {
                                setMarker(
                                    latLng = latLng,
                                    markerIconColor = MarkerIcons.YELLOW,
                                    text = areaName,
                                    naverMap = naverMap,
                                )
                            }
                        )
                        safeAreMetaData.circleOverlays.add(
                            CircleOverlay().apply {
                                radius = safeArea.radius.toDouble()
                                center = latLng
                                outlineWidth = 5
                                outlineColor = ContextCompat.getColor(
                                    this@NokMainActivity,
                                    R.color.deep_yellow
                                )
                                color = ContextCompat.getColor(
                                    this@NokMainActivity,
                                    R.color.transparent_yellow
                                )
                                map = naverMap
                            }
                        )
                    }
                }*/

            }

            is SafeAreaViewModel.SafeAreaEvent.MapView -> {
                behavior.state = event.behavior
                naverMap?.moveCamera(CameraUpdate.scrollTo(event.coord))
            }

            is SafeAreaViewModel.SafeAreaEvent.SettingSafeArea -> {
                if (event.isSettingSafeArea) {
                    behavior.isDraggable = false
                    if(navController.currentDestination?.id == R.id.safeAreaFragment){
                        navController.navigate(R.id.action_safeAreaFragment_to_settingSafeAreaFragment)
                    } else {
                        val action = SafeAreaDetailFragmentDirections.actionSafeAreaDetailFragmentToSettingSafeAreaFragment()
                        navController.navigate(R.id.action_safeAreaDetailFragment_to_settingSafeAreaFragment)
                    }

                    with(safeAreMetaData) {
                        binding.bottomSheetTopIv.isVisible = false
                        isSettingSafeArea = true
                        settingMarker.isVisible = true
                        settingCircleOverlay.isVisible = true
                    }
                } else {
                    behavior.isDraggable = true
                    with(safeAreMetaData) {
                        binding.bottomSheetTopIv.isVisible = true
                        isSettingSafeArea = false
                        settingMarker.isVisible = false
                        settingCircleOverlay.isVisible = false
                        safeAreaViewModel.setSelectedSafeAreaGroup("기본 그룹")
                    }
                    navController.popBackStack()
                }
            }

            is SafeAreaViewModel.SafeAreaEvent.RadiusChange -> {
                val zoom = when (event.radius) {
                    "0.5" -> 14.0
                    "1" -> 13.5
                    "1.5" -> 13.0
                    "2" -> 12.5
                    "2.5" -> 12.0
                    "3" -> 11.5
                    else -> 14.0
                }
                naverMap?.moveCamera(CameraUpdate.zoomTo(zoom))
                safeAreMetaData.settingCircleOverlay.radius = event.radius.toDouble().times(1000)
            }

            is SafeAreaViewModel.SafeAreaEvent.ChangeSafeAreaGroup -> {
                binding.groupTv.text = event.groupName
            }

            else -> {}
        }
    }

    private fun handleNavigationEvent(event: NokHomeViewModel.NavigateEvent) {
        when (event) {
            NokHomeViewModel.NavigateEvent.Home -> {}

            is NokHomeViewModel.NavigateEvent.HomeState -> {
                behavior.isDraggable = true
                if (event.isPredicted) {
                    setBottomSheetBehaviorForFirstNavigationEvent(HOME)
                } else {
                    if (navController.currentDestination?.id == R.id.nokHomeFragment) {
                        homeViewModel.fetchUserInfo()
                        binding.layout.translationY = 0f
                    }
                }
            }

            NokHomeViewModel.NavigateEvent.LocationHistory -> {
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }

            NokHomeViewModel.NavigateEvent.MeaningfulPlace -> {
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }

            NokHomeViewModel.NavigateEvent.SafeArea -> {
                if(navController.currentDestination?.id == R.id.safeAreaFragment) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
                behavior.isDraggable = false
            }

            NokHomeViewModel.NavigateEvent.Setting -> {
                binding.navermapLogo.isVisible = false
                behavior.isDraggable = false
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            NokHomeViewModel.NavigateEvent.SafeAreaDetail -> {
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }

            NokHomeViewModel.NavigateEvent.SafeAreaSetting -> {
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                with(safeAreMetaData) {
                    settingMarker.apply {
                        setMarker(
                            naverMap?.cameraPosition?.target!!,
                            MarkerIcons.YELLOW,
                            "",
                            naverMap
                        )
                        isVisible = true
                    }

                    settingCircleOverlay.apply {
                        center = settingMarker.position
                        radius = 500.0
                        color =
                            ContextCompat.getColor(
                                this@NokMainActivity,
                                R.color.transparent_yellow
                            )
                        outlineWidth = 5
                        outlineColor =
                            ContextCompat.getColor(this@NokMainActivity, R.color.deep_yellow)
                        map = naverMap
                        isVisible = true

                    }

                    naverMap?.addOnCameraChangeListener { _, _ ->
                        if (!isSettingSafeArea) {
                            return@addOnCameraChangeListener
                        }
                        Log.d("change", "change")

                        val currentPosition = naverMap?.cameraPosition?.target!!
                        Log.d("position", currentPosition.toString())
                        settingMarker.position = currentPosition
                        settingCircleOverlay.center = currentPosition
                    }
                }
            }
        }
    }

    private fun handleLocationHistoryEvent(event: LocationHistoryViewModel.LocationHistoryEvent) {
        when (event) {
            LocationHistoryViewModel.LocationHistoryEvent.FetchFail -> {

            }

            is LocationHistoryViewModel.LocationHistoryEvent.FetchSuccessSingle -> {
                Log.d("history", event.locationHistory.toString())
                locationHistoryMetaData.locationHistory = event.locationHistory
                val latLngList = event.locationHistory.map { LatLng(it.latitude, it.longitude) }
                initLocationHistory(latLngList)
            }

            is LocationHistoryViewModel.LocationHistoryEvent.FetchSuccessMultiple -> {
                Log.d("multipla success", "성공")
                with(locationHistoryMetaData) {
                    locationHistory = event.locationHistory[0]
                    locationHistory2 = event.locationHistory[1]
                    val latLngList = locationHistory.map { LatLng(it.latitude, it.longitude) }
                    val latLngList2 = locationHistory2.map { LatLng(it.latitude, it.longitude) }

                    initLocationHistory(latLngList, infoText = locationHistory[0].date)
                    initLocationHistory(
                        latLngList2,
                        paths[1],
                        R.color.purple,
                        markers[1],
                        MarkerIcons.PINK,
                        infoText = locationHistory2[0].date
                    )
                }
                locationHistoryViewModel.setIsLoading(false)
            }

            is LocationHistoryViewModel.LocationHistoryEvent.OnProgress2Changed -> {
                with(locationHistoryMetaData) {
                    moveCameraAlongLocationHistory(
                        paths[1],
                        markers[1],
                        event.progress,
                        locationHistory2
                    )
                }
            }

            is LocationHistoryViewModel.LocationHistoryEvent.OnProgressChanged -> {
                with(locationHistoryMetaData) {
                    moveCameraAlongLocationHistory(
                        paths[0],
                        markers[0],
                        event.progress,
                        locationHistory
                    )
                }
            }
        }
    }

    private fun initLocationHistory(
        coords: List<LatLng>,
        path: PathOverlay = locationHistoryMetaData.paths[0],
        pathColor: Int = R.color.deep_yellow,
        marker: Marker = locationHistoryMetaData.markers[0],
        markerColor: OverlayImage = MarkerIcons.YELLOW,
        infoText: String = "현재 위치 기록"
    ) {
        path.setPath(this@NokMainActivity, coords, pathColor, naverMap)

        marker.setMarkerWithInfoWindow(
            context = this@NokMainActivity,
            latLng = coords[0],
            markerIconColor = markerColor,
            "",
            naverMap,
            infoText
        )
    }

    private fun moveCameraAlongLocationHistory(
        path: PathOverlay,
        marker: Marker,
        progress: Int,
        list: List<LocationHistory>
    ) {
        if (progress == -1) {
            return
        }
        try {
            val latLng = path.coords[progress]
            var animation = CameraAnimation.Fly
            val distance = list[progress].distance.toDouble()
            var duration = 1000L
            marker.position = latLng
            marker.setInfoWindowText(this, list[progress].time)
            if (progress == 0) {
                naverMap?.moveCamera(
                    CameraUpdate.scrollAndZoomTo(latLng, locationHistoryMetaData.zoom)
                        .animate(animation, duration)
                )
            } else {
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
                    /*in 0.0..0.01 -> {
                        zoom = 18.0
                        animation = CameraAnimation.Easing
                        duration = 100L
                    }

                    else -> {
                        zoom = 15.0
                        animation = CameraAnimation.Easing
                        duration = 1000L
                    }*/
                }
                naverMap?.moveCamera(
                    CameraUpdate.scrollTo(latLng)
                        .animate(CameraAnimation.Easing, duration)
                )
                Log.d("distance", distance.toString())
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.d("$tag moveCameraAlongLocationHistory IndexOutOfBoundsException", e.toString())
        }
    }


    private fun handlePredictEvent(event: NokHomeViewModel.PredictEvent) {
        when (event) {
            // 예측 시작 -> 보호대상자 마지막 정보, 의미 장소 api 호출, 보호대상자 위치 업데이트 api 정지, 로딩화면 표시
            is NokHomeViewModel.PredictEvent.StartPredict -> {
                homeViewModel.predict()
                stopGetDementiaLocation()
                showLoadingDialog(this, "예측 장소를 추출중입니다...")
            }

            // 보호대상자 마지막 정보 UI 업데이트, 실종 시각 카운트다운 시작
            is NokHomeViewModel.PredictEvent.DisplayDementiaLastInfo -> {
                startCountDownJob(event.averageSpeed, event.coord)

                binding.averageMovementSpeedTv.text = String.format("%.2fkm", event.averageSpeed)

                // bottom sheet expanded offset 지정 및 높이 지정
                behavior.expandedOffset = binding.predictLayout.bottom + 20
            }

            // 의미장소 마커 지도에 표시
            is NokHomeViewModel.PredictEvent.MeaningFulPlace -> {
                event.meaningfulPlaceForList.forEach { meaningfulPlace ->
                    predictMetaData.markers.add(
                        Marker().apply {
                            setMarker(
                                latLng = meaningfulPlace.latLng,
                                markerIconColor = MarkerIcons.YELLOW,
                                text = meaningfulPlace.address,
                                naverMap = naverMap,
                            )
                        }
                    )
                }
            }

            // 의미장소 주변 경찰서 마커 지도에 표시
            is NokHomeViewModel.PredictEvent.SearchNearbyPoliceStation -> {
                event.policeStationList.forEach { policeStation ->
                    predictMetaData.markers.add(Marker().apply {
                        setMarker(
                            latLng = policeStation.latLng,
                            MarkerIcons.BLUE,
                            policeStation.policeName,
                            naverMap
                        )
                    })
                }
            }

            // 보호대상자 마지막 위치 마커 지도에 표시
            is NokHomeViewModel.PredictEvent.DisplayDementiaLastLocation -> {
                binding.lastLocationTv.text = event.lastLocation.address
                predictMetaData.markers.add(LAST_LOCATION, Marker().apply {
                    setMarkerWithInfoWindow(
                        context = this@NokMainActivity,
                        latLng = event.lastLocation.latLng,
                        markerIconColor = MarkerIcons.RED,
                        markerText = event.lastLocation.address,
                        naverMap = naverMap,
                        infoText = "실종 직전 위치"
                    )
                })
                binding.mapViewBtn.setOnClickListener {
                    naverMap?.moveCamera(CameraUpdate.scrollTo(predictMetaData.markers[LAST_LOCATION].position))
                }
            }

            // 예측 중지, 실종 시각 카운트다운 중지
            is NokHomeViewModel.PredictEvent.StopPredict -> {
                lifecycleScope.launch {
                    countDownJob?.cancelAndJoin()
                    countDownJob = null
                }
                with(predictMetaData) {
                    circleOverlay.isVisible = false
                    binding.countDownT.text = "00:00"
                    behavior.expandedOffset = 0

                    markers.forEach { marker ->
                        marker.map = null
                        marker.isVisible = false
                    }
                    markers.clear()
                    binding.layout.translationY = 0f
                }
            }

            is NokHomeViewModel.PredictEvent.PredictLocation -> {
                with(event.predictLocation.meaningfulPlaceInfo) {
                    naverMap?.moveCamera(
                        CameraUpdate.scrollTo(
                            latLng
                        )
                    )

                    val predictMarker =
                        predictMetaData.markers.firstOrNull { it.captionText == address && it.icon == MarkerIcons.GREEN }
                    if (predictMarker == null) {
                        predictMetaData.markers.add(Marker().apply {
                            setMarkerWithInfoWindow(
                                this@NokMainActivity,
                                latLng = latLng,
                                markerIconColor = MarkerIcons.GREEN,
                                markerText = address,
                                naverMap = naverMap,
                                infoText = "예상 위치"
                            )
                        })
                    }

                    Log.d("homemarkers size", predictMetaData.markers.size.toString())
                }
            }

            // 예측 기능 로딩 완료 알림
            NokHomeViewModel.PredictEvent.PredictDone -> {
                dismissLoadingDialog()
            }

            // 리사이클러뷰 아이템 클릭 이벤트에 따른 bottomSheet, Naver Map 제어
            is NokHomeViewModel.PredictEvent.MapView -> {
                behavior.state = event.behavior
                naverMap?.moveCamera(CameraUpdate.scrollTo(event.coord))
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
        with(predictMetaData.circleOverlay) {
            center = coord
            color = ContextCompat.getColor(this@NokMainActivity, R.color.purple)
            outlineWidth = 5
            outlineColor = ContextCompat.getColor(this@NokMainActivity, R.color.deep_purple)
            radius = 0.0
            isVisible = true
        }

        countDownJob = lifecycleScope.launch {
            var second = 0
            var minute = 0
            while (true) {
                second += 1
                predictMetaData.circleOverlay.radius += averageSpeed
                predictMetaData.circleOverlay.map = naverMap
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
            Log.d("$tag stopGetDementiaLocation", "stopGetDementiaLocation")
            updateLocationJob?.cancelAndJoin()
        }
        naverMap?.locationOverlay?.isVisible = false
        homeViewModel.setUpdateRate(0)
    }

    fun predict() {
        //homeViewModel.setIsPredicted(true)
        isFirstNavigationEvent[HOME] = true
        homeViewModel.eventHomeState(isPredicted = true, isPredictDone = false)
    }

    fun stopPredict() {
        homeViewModel.eventHomeState(isPredicted = false)
    }

    private fun setSafeArea(){
        /*if(navController.currentDestination?.id == R.id.safeAreaFragment){
            navController.navigate(R.id.action_safeAreaFragment_to_settingSafeAreaFragment)
        } else {
            navController.navigate(R.id.action_safeAreaDetailFragment_to_settingSafeAreaFragment)
        }*/
        safeAreaViewModel.setIsSettingSafeAreaStatus(true)
    }

    override fun initView() {
        binding.view = this
        binding.viewModel = homeViewModel
        binding.safeAreaVm = safeAreaViewModel
        homeViewModel.fetchUserInfo()
        initBottomSheet()
        initMap()
        initNavigator()

        binding.setSafeAreaTv.setOnClickListener {
             setSafeArea()
         }

        binding.searchAddressEt.setOnEditorActionListener(EditorInfo.IME_ACTION_DONE) {
            Log.d("et", binding.searchAddressEt.text.toString())
        }

        binding.changeGroupBtn.setOnClickListener {
            val dialog = SelectGroupDialogFragment()

            dialog.show(supportFragmentManager, dialog.tag)
        }
    }

    private fun initMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync { map ->
            map.uiSettings.isZoomControlEnabled = false
            val zoomControlView: ZoomControlView = findViewById(R.id.zoom)
            zoomControlView.map = map
            naverMap = map
        }
    }

    private fun initBottomSheet() {
        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isFitToContents = false
        behavior.halfExpandedRatio = 0.3f
        behavior.setPeekHeight(200, true)
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //Log.d("slide offset", slideOffset.toString())

                if (slideOffset >= 0.5f) {
                    binding.navermapLogo.isVisible = false
                } else {
                    binding.navermapLogo.isVisible = true
                }

                if (navController.currentDestination?.id in listOf(
                        R.id.safeAreaDetailFragment,
                    )
                ) {
                    if (slideOffset >= 0.5f) {
                        binding.setSafeAreaTv.isVisible = false
                    } else {
                        binding.setSafeAreaTv.isVisible = true
                    }

                    if (slideOffset <= 0.2f) {
                        Log.d("뭐ㅓㄴ데", "뭔데")
                        //behavior.isDraggable = false
                    } else {
                        Log.d("뭐ㅓㄴ데", "뭐냐고")
                        //behavior.isDraggable = true
                    }
                } else {
                    if (slideOffset <= 0.3f) {
                        binding.layout.translationY = -slideOffset * bottomSheet.height * 0.5f
                    }
                }
            }
        })
    }

    //viewModel과 binding Adapter로 refactoring ㅇ정
    private fun initNavigator() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("destination", destination.toString())

            if (navController.currentDestination?.id !in listOf(
                    R.id.nokHomeFragment,
                    R.id.meaningfulPlaceDetailFragment
                )
            ) {
                if(isRequireStopHomeFragmentJob) {
                    isRequireStopHomeFragmentJob = false
                    stopHomeFragmentJob()
                }
            }

            if (destination.id !in listOf(
                    R.id.safeAreaFragment,
                    R.id.safeAreaDetailFragment,
                    R.id.settingSafeAreaFragment
                )
            ) {
                isFirstNavigationEvent[SAFE_AREA] = true
                safeAreaViewModel.setIsSafeAreaGroupChanged(true)
            }

            if (destination.id != R.id.locationHistoryFragment) {
                clearLocationFragmentUI()
            }

            if (destination.id !in listOf(
                    R.id.nokSettingFragment,
                    R.id.settingUpdateTimeFragment,
                    R.id.modifyUserInfoFragment,
                R.id.settingSafeAreaFragment
                )
            ) {
                behavior.isDraggable = true
            }

            if (destination.id == R.id.safeAreaFragment) {
                behavior.setPeekHeight(300, true)
            }

            when (destination.id) {
                R.id.nokHomeFragment, R.id.meaningfulPlaceDetailFragment -> {
                    isRequireStopHomeFragmentJob = true
                    homeViewModel.eventNavigate(NokHomeViewModel.NavigateEvent.Home)
                    homeViewModel.eventHomeState()
                }

                R.id.nokSettingFragment, R.id.modifyUserInfoFragment, R.id.settingUpdateTimeFragment -> {
                    homeViewModel.eventNavigate(NokHomeViewModel.NavigateEvent.Setting)
                }

                R.id.safeAreaFragment-> {
                    homeViewModel.eventNavigate(NokHomeViewModel.NavigateEvent.SafeArea)
                }
                R.id.safeAreaDetailFragment -> {
                    homeViewModel.eventNavigate(NokHomeViewModel.NavigateEvent.SafeAreaDetail)
                    behavior.halfExpandedRatio = 0.3f
                }

                R.id.settingSafeAreaFragment -> {
                    homeViewModel.eventNavigate(NokHomeViewModel.NavigateEvent.SafeAreaSetting)
                    behavior.halfExpandedRatio = 0.2f
                }

                R.id.meaningfulPlaceFragment -> {
                    homeViewModel.eventNavigate(NokHomeViewModel.NavigateEvent.MeaningfulPlace)
                }

                R.id.locationHistoryFragment -> {
                    homeViewModel.eventNavigate(NokHomeViewModel.NavigateEvent.LocationHistory)
                }
            }
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

    private fun saveUserKeys() {
        val dementiaKey = getUserKey("dementia")
        homeViewModel.setDementiaKey(dementiaKey)
        locationHistoryViewModel.setDementiaKey(dementiaKey)
        safeAreaViewModel.setDementiaKey(dementiaKey)

        val nokKey = getUserKey("nok")
        homeViewModel.setNokKey(nokKey)
        settingViewModel.setNokKey(nokKey)
    }

    private fun stopHomeFragmentJob() {
        stopGetDementiaLocation()
        homeViewModel.eventHomeState(isPredicted = false)
    }

    private fun clearLocationFragmentUI() {
        locationHistoryViewModel.setIsMultipleSelected(false)
        locationHistoryViewModel.setMaxProgress(0)

        with(locationHistoryMetaData) {
            paths.forEach {
                it.map = null
            }
            //path = null
            markers.forEach {
                it.map = null
            }
        }
    }

    private fun setBottomSheetBehaviorForFirstNavigationEvent(index: Int) {
        if (isFirstNavigationEvent[index]) {
            isFirstNavigationEvent[index] = false

            behavior.state = if (index == HOME) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
    }

    companion object {
        const val LAST_LOCATION = 0
        const val MEANINGFUL_PLACE = 0
        const val HOME = 1
        const val SAFE_AREA = 2
    }
}