package kr.ac.tukorea.whereareu.presentation.nok.safearea

import androidx.fragment.app.activityViewModels
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.DialogCreateSafeAreaGroupBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToast
import kr.ac.tukorea.whereareu.util.extension.showToastShort

class CreateSafeAreaGroupDialogFragment: BaseDialogFragment<DialogCreateSafeAreaGroupBinding>(R.layout.dialog_create_safe_area_group) {
    private val viewModel: SafeAreaViewModel by activityViewModels()
    override fun initObserver() {
        repeatOnStarted {
            viewModel.safeAreaEvent.collect{
                when(it){
                    is SafeAreaViewModel.SafeAreaEvent.CreateSafeAreaGroup -> {
                        dismiss()
                    }
                    else -> {}
                }
            }
        }
    }

    override fun initView() {
        binding.cancelBtn.setOnClickListener {
            dismiss()
        }

        binding.doneBtn.setOnClickListener {
            val groupName = binding.groupNameEt.text.toString()
            if(groupName.isEmpty()){
                requireContext().showToast(requireContext(), "그룹 이름을 입력해주세요.")
            } else {
                viewModel.registerSafeAreaGroup(binding.groupNameEt.text.toString())
            }
        }
    }
}