package kr.ac.tukorea.whereareu.presentation.nok.safearea

import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSafeAreaDetailBinding
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter.SafeAreaRVA

@AndroidEntryPoint
class SafeAreaDetailFragment: BaseFragment<FragmentSafeAreaDetailBinding>(R.layout.fragment_safe_area_detail) {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val safeAreaRVA: SafeAreaRVA by lazy {
        SafeAreaRVA()
    }
    override fun initObserver() {

    }

    override fun initView() {
        binding.backBtn.setOnClickListener {
            navigator.popBackStack()
        }

    }
}