package kr.ac.tukorea.whereareu.presentation.nok

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.firebase.FCMService
import kr.ac.tukorea.whereareu.databinding.ActivityNokMainBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseActivity
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel
import kr.ac.tukorea.whereareu.util.extension.navigationHeight
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.setStatusBarTransparent

@AndroidEntryPoint
class NokMainActivity : BaseActivity<ActivityNokMainBinding>(R.layout.activity_nok_main) {
    private val homeViewModel: NokHomeViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private var updateLocationJob: Job? = null
    override fun initView() {
        //상태바 투명 설정
        this.setStatusBarTransparent()
        binding.layout.setPadding(0, 0, 0, this.navigationHeight())
        initNavigator()
        homeViewModel.fetchUserInfo()
    }

    private fun initNavigator() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)
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

    companion object{
        private const val CHANNEL_NAME = "Where are U Test"
        private const val CHANNEL_DESCRIPTION = "어디U 테스트 채널"
        private const val CHANNEL_ID = "채널 ID"
    }
}