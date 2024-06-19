package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavArgs
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSafeAreaDetailBinding
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter.SafeAreaDetailRVA
import kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter.SafeAreaRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class SafeAreaDetailFragment: BaseFragment<FragmentSafeAreaDetailBinding>(R.layout.fragment_safe_area_detail) {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val safeAreaRVA: SafeAreaDetailRVA by lazy {
        SafeAreaDetailRVA()
    }
    private val args: SafeAreaDetailFragmentArgs by navArgs()
    override fun initObserver() {
        repeatOnStarted {
            viewModel.safeAreaEvent.collect{
                when(it){
                    is SafeAreaViewModel.SafeAreaEvent.FetchSafeAreaGroup -> {
                        Log.d("test", it.toString())
                        safeAreaRVA.submitList(it.safeAreas)
                    }
                    else -> {}
                }
            }
        }
    }

    override fun initView() {
        binding.viewModel = viewModel
        initRVA()
        viewModel.fetchSafeAreaGroup(args.groupKey)
        binding.backBtn.setOnClickListener {
            viewModel.eventSafeArea(SafeAreaViewModel.SafeAreaEvent.ExitDetailFragment)
            navigator.popBackStack()
        }

    }

    private fun initRVA(){
        binding.rv.apply {
            adapter = safeAreaRVA
            itemAnimator = null
        }
    }
}