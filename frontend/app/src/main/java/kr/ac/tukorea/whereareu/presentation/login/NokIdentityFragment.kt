package kr.ac.tukorea.whereareu.presentation.login

import android.util.Log
import android.view.inputmethod.EditorInfo
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
        private lateinit var viewModel: LoginViewModel
        //private val viewModel: LoginViewModel by viewModels()

    override fun initObserver() {
        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        binding.viewModel = viewModel

        /*with(viewModel) {
            testError.observe(this@NokIdentityFragment) {
                Log.d("test error", it)
            }
            testSuccess.observe(this@NokIdentityFragment){
                if(it == "success"){
                    findNavController().navigate(R.id.action_nokIdentityFragment_to_nokOtpFragment)
                }
            }
        }*/
    }

    override fun initView() {
        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]
        binding.view = this
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
        binding.nameTextInputLayout.error = if(!validName()) "최소 2자의 한글을 입력해주세요" else null

        if (!validPhone()){
            binding.phoneNumberTextInputLayout.error = "전화번호 형식이 다릅니다.\n예시) 01012345678"
            return
        }

        val action = NokIdentityFragmentDirections.actionNokIdentityFragmentToNokOtpFragment(
            binding.nameEt.text.toString(), binding.phoneNumberEt.text.toString())
        findNavController().navigate(action)
        //viewModel.sendNokIdentity()
    }

    private fun validName() = !binding.nameEt.text.isNullOrBlank()
            && REGEX_NAME.toRegex().matches(binding.nameEt.text!!)


    private fun validPhone() = !binding.phoneNumberEt.text.isNullOrBlank()
            && REGEX_PHONE.toRegex().matches(binding.phoneNumberEt.text!!)

    companion object {
        private const val REGEX_NAME = "^[가-힣]{2,}\$"
        private const val REGEX_PHONE = "^01([016789])([0-9]{4})([0-9]{4})"
    }
}