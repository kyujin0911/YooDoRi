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
import kr.ac.tukorea.whereareu.util.network.NetworkManager

@AndroidEntryPoint
class NokSettingFragment : BaseFragment<FragmentNokSettingBinding>(R.layout.fragment_nok_setting) {
    private val viewModel: SettingViewModel by activityViewModels()
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
        binding.viewModel = viewModel
    }

    override fun onResume() {
        super.onResume()
        val key = nokSpf.getString("key", "") ?: ""
        viewModel.getUserInfo(key)
    }



    fun navigateToModifyUserInfo() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_modifyUserInfoFragment)
    }

    fun navigateToModifyUpdateTime() {
        findNavController().navigate(R.id.action_nokSettingFragment_to_settingUpdateTimeFragment)
    }
}