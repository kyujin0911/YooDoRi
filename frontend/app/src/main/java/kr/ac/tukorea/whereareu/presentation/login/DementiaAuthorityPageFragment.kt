package kr.ac.tukorea.whereareu.presentation.login

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentDementiaAuthorityPageBinding
import kr.ac.tukorea.whereareu.databinding.FragmentNokAuthorityPageBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.dementia.MainDementiaActivity
import kr.ac.tukorea.whereareu.presentation.nok.MainNokActivity

class DementiaAuthorityPageFragment :
    BaseFragment<FragmentDementiaAuthorityPageBinding>(R.layout.fragment_dementia_authority_page) {
    private val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 456
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    override fun initObserver() {

    }

    override fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        } else {
            checkAndRequestLocationPermissions()
        }
        goMainActivity()
    }

    private fun goMainActivity() {
        binding.finishBtn.setOnClickListener {
            val intent = Intent(requireContext(), MainDementiaActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }
    }

    private fun checkBackGroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            return true
        }

    }

    private fun requestBackGroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    //startLocationService()
                } else {
                    // Handle the case where the user denies the foreground service permission
                }
            }

            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    checkAndRequestLocationPermissions()
                } else {
                    // Handle the case where the user denies the location permission
                }
            }
        }
    }

    private fun checkAndRequestLocationPermissions() {
        if (checkLocationPermission()) {
            Log.d("checkLocationPermission", "true")
            if (checkBackGroundLocationPermission()) {
                //startLocationService()
                Log.d("checkBackGroundLocationPermission", "true")
            } else {
                Log.d("checkBackGroundLocationPermission", "false")
                requestBackGroundLocationPermission()
            }

        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission() {
        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // make your action here
                checkAndRequestLocationPermissions()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                // permission denied permanently
            }

            else -> {
                requestNotificationPermission.launch(permission)
            }
        }
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) // make your action here
                checkAndRequestLocationPermissions()
        }
}