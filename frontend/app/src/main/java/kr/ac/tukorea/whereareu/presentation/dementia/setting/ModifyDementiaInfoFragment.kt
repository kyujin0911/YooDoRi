package kr.ac.tukorea.whereareu.presentation.dementia.setting

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentModifyDementiaInfoBinding
import kr.ac.tukorea.whereareu.presentation.SettingViewModel
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment

@AndroidEntryPoint
class ModifyDementiaInfoFragment :
    BaseFragment<FragmentModifyDementiaInfoBinding>(R.layout.fragment_modify_dementia_info) {
    private val viewModel: SettingViewModel by activityViewModels()
    private val navigator by lazy {
        findNavController()
    }

    override fun initObserver() {
    }

    override fun initView() {
    }

    fun onClickBackBtn(){
        navigator.popBackStack()
    }
}