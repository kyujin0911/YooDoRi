package kr.ac.tukorea.whereareu.presentation.nok.setting

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentNokSettingBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment

@AndroidEntryPoint
class NokSettingFragment : BaseFragment<FragmentNokSettingBinding>(R.layout.fragment_nok_setting) {
    private val viewModel: SettingViewModel by activityViewModels()
    override fun initObserver() {
    }

    override fun initView() {
        binding.view = this
        binding.viewModel = viewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchUserInfo()
    }



    fun navigateToModifyUserInfo() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_modifyUserInfoFragment)
    }

    fun navigateToModifyUpdateTime() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_settingUpdateTimeFragment)
    }
}