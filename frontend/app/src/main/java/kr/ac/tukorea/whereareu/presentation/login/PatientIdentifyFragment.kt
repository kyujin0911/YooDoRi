package kr.ac.tukorea.whereareu.presentation.login

import android.content.Context.MODE_PRIVATE
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.inputmethod.EditorInfo
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.login.request.DementiaIdentityRequest
import kr.ac.tukorea.whereareu.databinding.FragmentPatientIdentifyBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.EditTextUtil.hideKeyboard
import kr.ac.tukorea.whereareu.util.EditTextUtil.setOnEditorActionListener
import kr.ac.tukorea.whereareu.util.EditTextUtil.showKeyboard
import kr.ac.tukorea.whereareu.util.LoginUtil.repeatOnStarted

class PatientIdentifyFragment :
    BaseFragment<FragmentPatientIdentifyBinding>(R.layout.fragment_patient_identify) {
    private val viewModel: LoginViewModel by activityViewModels()
    private lateinit var navigator: NavController
    override fun initObserver() {
        binding.viewModel = viewModel
        repeatOnStarted {
            viewModel.dementiaKeyFlow.collect {
                if(it != "000000") {
                    val spf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
                    spf.edit {
                        putString("name", binding.nameEt.text.toString().trim())
                        putString("phone", binding.phoneNumberEt.text.toString().trim())
                        putBoolean("isDementia", true)
                        commit()
                    }
                    navigator.navigate(R.id.action_patientIdentifyFragment_to_patientOtpFragment)
                }
            }
        }
    }

    override fun initView() {
        navigator = findNavController()
        binding.view = this
        binding.phoneNumberEt.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        with(binding) {
            nameEt.setOnEditorActionListener(EditorInfo.IME_ACTION_NEXT) {
                if (validName()) {
                    nameTextInputLayout.error = null
                    phoneNumberEt.showKeyboard()
                } else {
                    nameTextInputLayout.error = "최소 2자의 한글을 입력해주세요"
                }
            }
            phoneNumberEt.setOnEditorActionListener(EditorInfo.IME_ACTION_NEXT) {
                if (validPhone()) {
                    phoneNumberTextInputLayout.error = null
                    phoneNumberEt.hideKeyboard()
                } else {
                    phoneNumberTextInputLayout.error = "전화번호 형식이 다릅니다.\n입력 예시) 010-1234-5678"
                }
            }
        }
    }

    fun onClickBackBtn() {
        navigator.popBackStack()
    }

    fun onClickInputDone() {
        binding.nameTextInputLayout.error = if (!validName()) "최소 2자의 한글을 입력해주세요" else null


        if (!validPhone()) {
            binding.phoneNumberTextInputLayout.error = "전화번호 형식이 다릅니다.\n예시) 010-1234-5678"
            return
        }

        viewModel.sendDementiaIdentity(
            DementiaIdentityRequest(binding.nameEt.text.toString().trim(), binding.phoneNumberEt.text.toString().trim())
        )
    }

    private fun validName() = !binding.nameEt.text.isNullOrBlank()
            && REGEX_NAME.toRegex().matches(binding.nameEt.text!!)

    private fun validPhone() = !binding.phoneNumberEt.text.isNullOrBlank()
            && REGEX_PHONE.toRegex().matches(binding.phoneNumberEt.text!!)

    companion object {
        private const val REGEX_NAME = "^[가-힣]{2,}\n?$"
        private const val REGEX_PHONE = "^01([016789])-([0-9]{3,4})-([0-9]{4})"
    }
}