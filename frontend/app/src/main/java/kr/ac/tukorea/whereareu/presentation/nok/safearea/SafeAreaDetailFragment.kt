package kr.ac.tukorea.whereareu.presentation.nok.safearea

import androidx.fragment.app.activityViewModels
import androidx.navigation.NavArgs
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
    private val args: SafeAreaDetailFragmentArgs by navArgs()
    override fun initObserver() {

    }

    override fun initView() {
        viewModel.fetchSafeAreaGroup(args.groupKey)
        binding.backBtn.setOnClickListener {
            navigator.popBackStack()
        }

    }
}