package kr.ac.tukorea.whereareu.presentation.nok.setting

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentNokSettingBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.network.NetworkManager

@AndroidEntryPoint
class NokSettingFragment : BaseFragment<FragmentNokSettingBinding>(R.layout.fragment_nok_setting) {
    private val viewModel: NokHomeViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()
    private val nokSpf by lazy {
        requireContext().getSharedPreferences("User", MODE_PRIVATE)
    }
    private val dementiaSpf by lazy {
        requireContext().getSharedPreferences("OtherUser", MODE_PRIVATE)
    }

    override fun initObserver() {
    }

    override fun initView() {
        binding.view = this
    }


    //네트워크 연결 안됐을 때 화면 업데이트하는 함수
    private fun updateViewOnNetworkDisconnect() {

        binding.userNameTv.text = nokSpf.getString("name", "")
        binding.userPhoneNumberTv.text = nokSpf.getString("phone", "")

        binding.otherNameTv.text = dementiaSpf.getString("name", "")
        binding.otherPhoneTv.text = (dementiaSpf.getString("phone", ""))
    }

    override fun onResume() {
        super.onResume()

        if (!NetworkManager.checkNetworkState(requireContext())) {
            Log.d("network status", "off")
            updateViewOnNetworkDisconnect()
        } else {
            val key = nokSpf.getString("key", "") ?: ""

            Log.d("settingFragment", "onResume")
            settingViewModel.getUserInfo(key)

            repeatOnStarted {
                settingViewModel.userInfo.collect {
                    Log.d("Nok_Setting_Fragment", "get User Info API")

                    val nokName = it.nokInfoRecord.nokName
                    val nokPhone = it.nokInfoRecord.nokPhoneNumber
                    val dementiaName = it.dementiaInfoRecord.dementiaName
                    val dementiaPhone = it.dementiaInfoRecord.dementiaPhoneNumber

                    saveUserInfo(nokName, nokPhone, dementiaName, dementiaPhone)

                    binding.userNameTv.text = nokName
                    binding.userPhoneNumberTv.text = nokPhone
                    binding.otherNameTv.text = dementiaName
                    binding.otherPhoneTv.text = dementiaPhone
                    binding.updateTimeTv.text = "${it.nokInfoRecord.updateRate.div(60)}분"
                }
            }
        }
    }

    private fun saveUserInfo(
        nokName: String,
        nokPhone: String,
        dementiaName: String,
        dementiaPhone: String
    ) {
        nokSpf.edit {
            putString("name", nokName)
            putString("phone", nokPhone)
            commit()
        }

        dementiaSpf.edit {
            putString("name", dementiaName)
            putString("phone", dementiaPhone)
            commit()
        }
    }

    fun navigateToModifyUserInfo() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_modifyUserInfoFragment)
    }

    fun navigateToModifyUpdateTime() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_settingUpdateTimeFragment)
    }
}