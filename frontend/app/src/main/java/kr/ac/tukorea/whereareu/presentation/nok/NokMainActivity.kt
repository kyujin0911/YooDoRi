package kr.ac.tukorea.whereareu.presentation.nok

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.ActivityNokMainBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseActivity
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.util.extension.navigationHeight
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.setStatusBarTransparent
import kr.ac.tukorea.whereareu.util.extension.statusBarHeight

@AndroidEntryPoint
class NokMainActivity : BaseActivity<ActivityNokMainBinding>(R.layout.activity_nok_main) {
    private val viewModel: NokHomeViewModel by viewModels()
    private var updateLocationJob: Job? = null
    override fun initView() {
        //상태바 투명 설정
        this.setStatusBarTransparent()
        binding.layout.setPadding(0, 0, 0, this.navigationHeight())
        initNavigator()
    }

    private fun initNavigator() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // 위치 예측 화면 이동 시 bottom nav 가리기
        /*navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.predictLocationFragment) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }*/
    }

    private fun getDementiaLocation() {
        val spf = getSharedPreferences("OtherUser", MODE_PRIVATE)
        val dementiaKey = spf.getString("key", "")
        if (!dementiaKey.isNullOrEmpty()) {
            repeatOnStarted {
                viewModel.updateDuration.collect { duration ->
                    Log.d("duration", duration.toString())
                    if (updateLocationJob == null) {
                        updateLocationJob = lifecycleScope.launch {
                            while (true) {
                                viewModel.getDementiaLocation(dementiaKey)
                                Log.d("duration null test", duration.toString())
                                delay(duration)
                            }
                        }
                    } else {
                        updateLocationJob?.cancelAndJoin()
                        updateLocationJob = lifecycleScope.launch {
                            while (true) {
                                viewModel.getDementiaLocation(dementiaKey)
                                Log.d("duration not null test", duration.toString())
                                delay(duration)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopGetDementiaLocation() {
        lifecycleScope.launch {
            updateLocationJob?.cancelAndJoin()
        }
    }

    override fun initObserver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver, IntentFilter("gps")
        )

        repeatOnStarted {
            viewModel.isPredicted.collect { isPredicted ->
                if (!isPredicted) {
                    getDementiaLocation()
                    binding.bottomNav.visibility = View.VISIBLE
                } else {
                    stopGetDementiaLocation()
                    binding.bottomNav.visibility = View.GONE
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
}