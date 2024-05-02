package kr.ac.tukorea.whereareu.presentation.nok

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.ActivityNokMainBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseActivity
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class NokMainActivity : BaseActivity<ActivityNokMainBinding>(R.layout.activity_nok_main),
    OnMapReadyCallback {
    private val homeViewModel: NokHomeViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private var updateLocationJob: Job? = null
    private var naverMap: NaverMap? = null
    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    override fun initView() {
        //상태바 투명 설정
        binding.viewModel = homeViewModel
        initMap()
        //this.setStatusBarTransparent()
        //binding.layout.setPadding(0, 0, 0, this.navigationHeight())
        initNavigator()
        homeViewModel.fetchUserInfo()
        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.peekHeight = 20
        behavior.isFitToContents = false
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

    private fun initNavigator() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            Log.d("current destination", R.id.settingTab.toString())
            Log.d("destination", destination.id.toString())
            var event: NokHomeViewModel.NavigateEvent? = null
            when (destination.id) {
                R.id.nokHomeFragment -> {
                    event = NokHomeViewModel.NavigateEvent.Home
                }

                R.id.nokSettingFragment -> {
                    behavior.isDraggable = false
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    event = NokHomeViewModel.NavigateEvent.Setting
                }

                R.id.safeAreaFragment -> {
                   event = NokHomeViewModel.NavigateEvent.SafeArea
                }

                R.id.meaningfulPlaceFragment -> {
                    event = NokHomeViewModel.NavigateEvent.MeaningfulPlace
                }

                R.id.locationHistoryFragment -> {
                    event = NokHomeViewModel.NavigateEvent.LocationHistory
                }
                else -> event = NokHomeViewModel.NavigateEvent.Home
            }
            homeViewModel.eventNavigate(event)
        }
    }

    private fun saveUserKeys() {
        val dementiaKey = getSharedPreferences("OtherUser", MODE_PRIVATE)
            .getString("key", "")
        if (!dementiaKey.isNullOrEmpty()) {
            homeViewModel.setDementiaKey(dementiaKey)
        }

        val nokKey = getSharedPreferences("User", MODE_PRIVATE)
            .getString("key", "")
        if (!nokKey.isNullOrEmpty()) {
            homeViewModel.setNokKey(nokKey)
            settingViewModel.setNokKey(nokKey)
        }
    }

    private fun makeUpdateLocationJob(duration: Long): Job{
        return lifecycleScope.launch {
            while (true){
                homeViewModel.getDementiaLocation()
                delay(duration)
            }
        }
    }

    private fun stopGetDementiaLocation() {
        lifecycleScope.launch {
            updateLocationJob?.cancelAndJoin()
        }
    }

    override fun initObserver() {
        saveUserKeys()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver, IntentFilter("gps")
        )

        // 실행중인 coroutine이 없으면 새로운 job을 생성해서 실행
        repeatOnStarted {
            settingViewModel.updateRate.collect { duration ->
                if (duration == "0"){
                    return@collect
                }
                Log.d("duration", duration.toString())
                updateLocationJob = if (updateLocationJob == null) {
                    makeUpdateLocationJob(duration.toLong().times(60*1000))
                }

                // 실행중인 coroutine이 있으면 job을 취소하고 duration에 맞게 재시작
                else {
                    updateLocationJob?.cancelAndJoin()
                    makeUpdateLocationJob(duration.toLong().times(60*1000))
                }
            }
        }

        repeatOnStarted {
            homeViewModel.isPredicted.collect { isPredicted ->
                if (isPredicted) {
                    stopGetDementiaLocation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "destroy")
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
}