package kr.ac.tukorea.whereareu.presentation.nok.safearea

import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaGroup
import kr.ac.tukorea.whereareu.databinding.FragmentSafeAreaBinding
import kr.ac.tukorea.whereareu.domain.safearea.SafeArea
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.safearea.adapter.SafeAreaRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class SafeAreaFragment : BaseFragment<FragmentSafeAreaBinding>(R.layout.fragment_safe_area), SafeAreaRVA.SafeRVAClickListener {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val safeAreaRVA: SafeAreaRVA by lazy {
        SafeAreaRVA()
    }
    override fun initObserver() {
        repeatOnStarted {
            viewModel.safeAreaEvent.collect{event ->
                handleSafeAreaEvent(event)
            }
        }
    }
    override fun initView() {
        initSafeAreaRVA()
        showCreateSafeAreaGroupDialog()
        /*viewModel.registerSafeArea(
            RegisterSafeAreaRequest(
                "253050",
                "테스트 그룹",
                "테스트 지역",
                37.4016759,
                126.9341057,
                100
            )
        )*/
        /*binding.tv.setOnClickListener {
            navigator.navigate(R.id.safeAreaDetailFragment)
        }*/

    }

    override fun onResume() {
        super.onResume()
        showLoadingDialog(requireContext(), "안심구역을 불러오고 있습니다.")
        viewModel.fetchSafeAreaAll()
    }

    private fun initSafeAreaRVA(){
        binding.rv.apply {
            adapter = safeAreaRVA
            itemAnimator = null
        }
        safeAreaRVA.setSafeRVAClickListener(this)
    }

    private fun handleSafeAreaEvent(event: SafeAreaViewModel.SafeAreaEvent){
        when(event){
            is SafeAreaViewModel.SafeAreaEvent.FetchSafeArea -> {
                safeAreaRVA.submitList(event.groupList, kotlinx.coroutines.Runnable {
                    dismissLoadingDialog()
                })
            }

            is SafeAreaViewModel.SafeAreaEvent.MapView -> {}
            is SafeAreaViewModel.SafeAreaEvent.SettingSafeArea -> {}
            is SafeAreaViewModel.SafeAreaEvent.RadiusChange -> {
            }
            is SafeAreaViewModel.SafeAreaEvent.CreateSafeAreaGroup -> {
                val newGroup = SafeAreaGroup(event.groupName, "")
                safeAreaRVA.submitList(safeAreaRVA.currentList.toMutableList().apply {
                    add(newGroup)
                })
            }

            else -> {}
        }
    }

    private fun showCreateSafeAreaGroupDialog(){
        binding.createGroupBtn.setOnClickListener {
            val dialog = CreateSafeAreaGroupDialogFragment()
            dialog.show(childFragmentManager, dialog.tag)
        }
    }

    override fun onClickMapView(latLng: LatLng) {
        viewModel.eventSafeArea(SafeAreaViewModel.SafeAreaEvent.MapView(BottomSheetBehavior.STATE_COLLAPSED, latLng))
    }

    override fun onClickInfoView(groupKey: String) {
        val action = SafeAreaFragmentDirections.actionSafeAreaFragmentToSafeAreaDetailFragment(groupKey)
        navigator.navigate(action)
    }
}