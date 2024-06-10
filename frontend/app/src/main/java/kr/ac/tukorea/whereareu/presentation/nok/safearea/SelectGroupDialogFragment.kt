package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.forEach
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.fragment.app.activityViewModels
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.safearea.SafeAreaGroup
import kr.ac.tukorea.whereareu.databinding.DialogSelectGroupBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment

class SelectGroupDialogFragment: BaseDialogFragment<DialogSelectGroupBinding>(R.layout.dialog_select_group) {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    private var defaultGroupId = 0
    override fun initObserver() {

    }

    override fun initView() {
        val list = viewModel.getSafeAreaGroupList()
        addRadioButtons(list)
        //binding.radioGroup.check(defaultGroupId)
        binding.doneBtn.setOnClickListener {
            val checkedButton = view?.findViewById<RadioButton>(binding.radioGroup.checkedRadioButtonId)!!
            viewModel.setSelectedSafeAreaGroup(checkedButton.text.toString())
            dismiss()
        }

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun addRadioButtons(list: List<SafeAreaGroup>) {
        list.forEach {
            val radioButton = RadioButton(requireContext())
            radioButton.text = it.groupName
            val params = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 4, 0, 4)
            radioButton.id = View.generateViewId()
            binding.radioGroup.addView(radioButton)
            radioButton.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow))
            if(it.groupName == "기본 그룹")
            {
                defaultGroupId = View.generateViewId()
                radioButton.id = defaultGroupId
            }
        }
    }
}