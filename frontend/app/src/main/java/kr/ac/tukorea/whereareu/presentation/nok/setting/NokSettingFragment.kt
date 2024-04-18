package kr.ac.tukorea.whereareu.presentation.nok.setting

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.core.content.edit
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.setting.UpdateRateRequest
import kr.ac.tukorea.whereareu.databinding.FragmentNokSettingBinding
import kr.ac.tukorea.whereareu.presentation.SettingViewModel
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime.SettingUpdateTimeFragment
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class NokSettingFragment : BaseFragment<FragmentNokSettingBinding>(R.layout.fragment_nok_setting) {
    private val viewModel: NokHomeViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()
    private val nokSpf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
    private val otherSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
    private val key: String = nokSpf.getString("key", "") as String
    private val otherKey : String = otherSpf.getString("key", "") as String

    override fun initObserver() {
    }

    override fun initView() {
        binding.userNameTv.text = nokSpf.getString("name", "")
        binding.userPhoneNumberTv.text = nokSpf.getString("phone", "")

        binding.otherNameTv.setText(otherSpf.getString("name", ""))
        binding.otherPhoneTv.setText((otherSpf.getString("phone", "")))

        binding.updateTimeLayout.setOnClickListener {
            onUpdateSettingTime()
        }
        binding.updateUserInfoLayout.setOnClickListener {
            onUpdateUserInfoLayoutClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("settingFragment", "onResume")
        settingViewModel.getUserInfo(key)

        repeatOnStarted {
            val updateRate = settingViewModel.settingTime.value.toInt() * 60
            Log.d("SettingFragment","$updateRate")
            settingViewModel.sendUpdateTime(
                UpdateRateRequest(otherKey?:"", 0, updateRate))

            settingViewModel.userInfo.collect {
                Log.d("Nok_Setting_Fragment", "get User Info API")
                Log.d("Nok_Setting_Fragment", "$otherKey")

                val nokName = it.nokInfoRecord.nokName
                val nokPhone = it.nokInfoRecord.nokPhoneNumber
                val dementiaName = it.dementiaInfoRecord.dementiaName
                val dementiaPhone = it.dementiaInfoRecord.dementiaPhoneNumber

                nokSpf.edit{
                    putString("name", nokName)
                    putString("phone", nokPhone)
                    commit()
                }
                binding.userNameTv.text = nokName
                binding.userPhoneNumberTv.text = nokPhone
                binding.otherNameTv.text = dementiaName
                binding.otherPhoneTv.text = dementiaPhone
            }
        }

        binding.updateTimeTv.text = "${settingViewModel.settingTime.value}분"
    }

    private fun onUpdateUserInfoLayoutClicked() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_modifyUserInfoFragment)
    }

    private fun onUpdateSettingTime() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_settingUpdateTimeFragment)
    }
}