package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSettingSafeAreaBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToast

class SettingSafeAreaFragment :
    BaseFragment<FragmentSettingSafeAreaBinding>(R.layout.fragment_setting_safe_area) {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    private val navigator by lazy {
        findNavController()
    }
    private val args: SettingSafeAreaFragmentArgs by navArgs()
    override fun initObserver() {
        repeatOnStarted {
            viewModel.safeAreaEvent.collect { event ->
                handleSafeAreaEvent(event)
            }
        }
    }

    private fun handleSafeAreaEvent(event: SafeAreaViewModel.SafeAreaEvent) {
        when (event) {
            is SafeAreaViewModel.SafeAreaEvent.RadiusChange -> {
                binding.safeAreaRadiusTv.text = "${event.radius}km"
            }

            is SafeAreaViewModel.SafeAreaEvent.FailRegisterSafeArea -> {
                requireContext().showToast(requireContext(), event.message)
            }

            is SafeAreaViewModel.SafeAreaEvent.SuccessRegisterSafeArea -> {
                val currentKey = viewModel.getCurrentGroupKey()
                val action = SettingSafeAreaFragmentDirections.actionSettingSafeAreaFragmentToSafeAreaDetailFragment(currentKey)
                navigator.navigate(action)
            }

            else -> {}
        }
    }

    override fun initView() {
        initSeekBar()
        cancelSettingSafeArea()
        binding.saveBtn.setOnClickListener {
            viewModel.registerSafeArea()
        }
    }

    private fun initSeekBar() {
        val radiusList = listOf("0.5", "1", "1.5", "2", "2.5", "3")
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setSafeAreaRadius(radiusList[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    private fun cancelSettingSafeArea() {
        binding.cancelBtn.setOnClickListener {
            viewModel.setIsSettingSafeAreaStatus(false)
            navigator.popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.setIsSettingSafeAreaStatus(false)
                    navigator.popBackStack()
                }
            }
        )
    }
}