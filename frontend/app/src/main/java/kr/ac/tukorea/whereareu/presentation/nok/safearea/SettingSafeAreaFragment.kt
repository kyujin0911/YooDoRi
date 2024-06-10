package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.activityViewModels
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSettingSafeAreaBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

class SettingSafeAreaFragment :
    BaseFragment<FragmentSettingSafeAreaBinding>(R.layout.fragment_setting_safe_area) {
    private val viewModel: SafeAreaViewModel by activityViewModels()
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

            else -> {}
        }
    }

    override fun initView() {
        initSeekBar()
        cancelSettingSafeArea()
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
        }
    }
}