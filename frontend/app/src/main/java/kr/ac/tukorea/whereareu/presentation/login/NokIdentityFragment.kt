package kr.ac.tukorea.whereareu.presentation.login

import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentNokIdentityBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.login.EditTextUtil.hideKeyboard
import kr.ac.tukorea.whereareu.presentation.login.EditTextUtil.setOnEditorActionListener
import kr.ac.tukorea.whereareu.presentation.login.EditTextUtil.showKeyboard

class NokIdentityFragment :
    BaseFragment<FragmentNokIdentityBinding>(R.layout.fragment_nok_identity) {
        private val viewModel: LoginViewModel by activityViewModels()

    override fun initObserver() {
        binding.viewModel = viewModel
    }

    override fun initView() {
        Log.d("nokbackstack", findNavController().currentBackStackEntry.toString())
        binding.view = this
        binding.phoneNumberEt.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        with(binding) {
            nameEt.setOnEditorActionListener(EditorInfo.IME_ACTION_NEXT){
                if(validName()){
                    nameTextInputLayout.error = null
                    phoneNumberEt.showKeyboard()
                }
                else{
                    nameTextInputLayout.error = "최소 2자의 한글을 입력해주세요"
                }
            }
            phoneNumberEt.setOnEditorActionListener(EditorInfo.IME_ACTION_DONE){
                if(validPhone()){
                    phoneNumberTextInputLayout.error = null
                    phoneNumberEt.hideKeyboard()
                } else {
                    phoneNumberTextInputLayout.error = "전화번호 형식이 다릅니다.\n입력 예시) 01012345678"
                }
            }
        }
    }

    fun onClickBackBtn() {
        findNavController().popBackStack()
    }

    fun onClickInputDone() {
        if(!validName()){
            binding.nameTextInputLayout.error = "최소 2자의 한글을 입력해주세요"
            return
        }

        if (!validPhone()){
            binding.phoneNumberTextInputLayout.error = "전화번호 형식이 다릅니다.\n예시) 010-1234-5678"
            return
        }

        val action = NokIdentityFragmentDirections.actionNokIdentityFragmentToNokOtpFragment(
            binding.nameEt.text.toString().trim(), binding.phoneNumberEt.text.toString())
        findNavController().navigate(action)
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