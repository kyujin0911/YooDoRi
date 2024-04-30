package kr.ac.tukorea.whereareu.presentation.dementia.setting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.flow.EmptyFlow.collect
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.home.LocationInfo
import kr.ac.tukorea.whereareu.databinding.FragmentDementiaSettingBinding
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.location.LocationService

@AndroidEntryPoint
class DementiaSettingFragment : BaseFragment<FragmentDementiaSettingBinding>(R.layout.fragment_dementia_setting) {
    private val settingViewModel: SettingViewModel by activityViewModels()

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getSerializableExtra("postInfo", LocationInfo::class.java)
            } else {
                intent?.getSerializableExtra("postInfo") as LocationInfo
            }
            Log.d("info", info.toString())
            binding.postInfoTv.text = "서버에 보낸 정보: " + info.toString()
        }
    }
    override fun initObserver() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            mMessageReceiver, IntentFilter("gps")
        )
    }
    override fun initView() {
        val dementiaSpf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
        val nokSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)

        binding.userNameTv.text = dementiaSpf.getString("name", "")
        binding.userPhoneNumberTv.text = dementiaSpf.getString("phone","")

        binding.otherNameTv.text = nokSpf.getString("name", "")
        binding.otherPhoneTv.text = nokSpf.getString("phone", "")

        binding.updateUserInfoLayout.setOnClickListener {
            onUpdateDementiaInfoLayoutClicked()
        }


        binding.startBtn.setOnClickListener {
            Intent(requireActivity().applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_START
                requireActivity().startService(this)
            }
        }

        binding.stopBtn.setOnClickListener {
            Intent(requireActivity().applicationContext, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                requireActivity().startService(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val dementiaSpf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
        val nokSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
        val nokKey: String = nokSpf.getString("key", "") as String

        settingViewModel.getUserInfo(nokKey)

        repeatOnStarted {
            settingViewModel.userInfo.collect{
                val dementiaName = it.dementiaInfoRecord.dementiaName
                val dementiaPhone = it.dementiaInfoRecord.dementiaPhoneNumber
                val nokName = it.nokInfoRecord.nokName
                val nokPhone = it.nokInfoRecord.nokPhoneNumber

                dementiaSpf.edit {
                    putString("name", dementiaName)
                    putString("phone", dementiaPhone)
                    commit()
                }
                nokSpf.edit{
                    putString("name", nokName)
                    putString("phone", nokPhone)
                    commit()
                }

                binding.userNameTv.text = dementiaName
                binding.userPhoneNumberTv.text = dementiaPhone
                binding.otherNameTv.text = nokName
                binding.otherPhoneTv.text = nokPhone
            }
        }
    }

    private fun onUpdateDementiaInfoLayoutClicked() {
        findNavController().navigate(R.id.action_dementiaSettingFragment_to_modifyDementiaInfoFragment)
    }
}