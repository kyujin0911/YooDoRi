package kr.ac.tukorea.whereareu.presentation.nok.setting

import androidx.navigation.fragment.findNavController
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSettingUpdateTimeBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment

class SettingUpdateTimeFragment(val setUpdateTime: (String) -> Unit): BaseDialogFragment<FragmentSettingUpdateTimeBinding>(R.layout.fragment_setting_update_time) {
    //private lateinit var setUpdateTimeListener: SetUpdateTimeListener
    override fun initObserver() {

    }

    override fun initView() {
//        val nokUpdatePhone = requireActivity().getSharedPreferences("UpdateTime", Context.MODE_PRIVATE)
        with(binding) {
            numberPicker.minValue = 0
            numberPicker.maxValue = 8

            numberPicker.displayedValues =
                arrayOf("1", "3", "5", "10", "15", "20", "30", "45", "60")
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