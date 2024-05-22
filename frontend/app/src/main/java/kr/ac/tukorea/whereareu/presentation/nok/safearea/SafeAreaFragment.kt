package kr.ac.tukorea.whereareu.presentation.nok.safearea

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.safearea.RegisterSafeAreaRequest
import kr.ac.tukorea.whereareu.databinding.FragmentSafeAreaBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment

@AndroidEntryPoint
class SafeAreaFragment : BaseFragment<FragmentSafeAreaBinding>(R.layout.fragment_safe_area) {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    private val navigator: NavController by lazy {
        findNavController()
    }
    override fun initObserver() {

    }

    override fun initView() {
        viewModel.registerSafeArea(
            RegisterSafeAreaRequest(
                "253050",
                "테스트 그룹",
                "테스트 지역",
                37.4016759,
                126.9341057,
                100
            )
        )

        binding.tv.setOnClickListener {
            navigator.navigate(R.id.safeAreaDetailFragment)
        }

        viewModel.fetchSafeArea()
    }
}