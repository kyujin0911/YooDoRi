package kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime

import androidx.navigation.fragment.findNavController
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSettingUpdateTimeBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment

class SettingUpdateTimeFragment(val setUpdateTime: (String) -> Unit): BaseDialogFragment<FragmentSettingUpdateTimeBinding>(R.layout.fragment_setting_update_time) {
    //private lateinit var setUpdateTimeListener: SetUpdateTimeListener
    override fun initObserver() {

    }

    override fun initView() {
        with(binding) {
            
        }


        binding.finishBtn.setOnClickListener {
            with(binding) {
                val selectedValueIndex = numberPicker.value
                val selectedValue = numberPicker.displayedValues[selectedValueIndex]
                setUpdateTime(selectedValue)

            }
            dismiss()
        }
    }
    interface SetUpdateTimeListener{
        fun setUpdateTime(time: String)
    }
    fun onClickBackBtn() {
        findNavController().popBackStack()
    }
}