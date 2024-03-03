package kr.ac.tukorea.whereareu.presentation.login.nok

import android.content.Context.MODE_PRIVATE
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.login.request.NokIdentityRequest
import kr.ac.tukorea.whereareu.databinding.FragmentNokOtpBinding
import kr.ac.tukorea.whereareu.databinding.ToastLayoutBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.login.LoginViewModel
import kr.ac.tukorea.whereareu.util.EditTextUtil.hideKeyboard
import kr.ac.tukorea.whereareu.util.EditTextUtil.setOnEditorActionListener
import kr.ac.tukorea.whereareu.util.LoginUtil.repeatOnStarted

@AndroidEntryPoint
class NokOtpFragment : BaseFragment<FragmentNokOtpBinding>(R.layout.fragment_nok_otp) {
    private val viewModel: LoginViewModel by activityViewModels()
    private val args: NokOtpFragmentArgs by navArgs()
    private val navigator by lazy {
        findNavController()
    }

    override fun initObserver() {
        binding.viewModel = viewModel
        repeatOnStarted{
            viewModel.navigateToNokMainEvent.collect {
                // 보호자 정보 저장
                val nokSpf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
                nokSpf.edit {
                    putString("key", it.nokKey)
                    putString("name", args.name)
                    putString("phone", args.phone)
                    commit()
                }

                // 보호대상자 정보 저장
                val dementiaInfo = it.dementiaInfoRecord
                val dementiaSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
                dementiaSpf.edit {
                    putString("name", dementiaInfo.dementiaName)
                    putString("phone", dementiaInfo.dementiaPhoneNumber)
                    putString("key", dementiaInfo.dementiaKey)
                    commit()
                }
                navigator.navigate(R.id.action_nokOtpFragment_to_nokAuthorityPageFragment)
                // 보호자 메인화면으로 이동
                /*val intent = Intent(requireContext(), MainNokActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)*/

            }
        }

        // 에러 메시지 Toast로 출력
        repeatOnStarted {
            viewModel.toastEvent.collect{
                val binding = ToastLayoutBinding.inflate(layoutInflater)
                binding.run{
                    tv.text = it
                    val toast = Toast(requireContext())
                    toast.view = binding.root

                    binding.root.setBackgroundResource(R.drawable.toast_bg)
                    toast.setGravity(Gravity.BOTTOM, 0, 400)
                    toast.show()
                }
            }
        }
    }

    override fun initView() {
        binding.view = this
        with(binding) {
            otpEt.setOnEditorActionListener(EditorInfo.IME_ACTION_DONE) {
                if (validOtp()) {
                    otpTextInputLayout.error = null
                    otpEt.hideKeyboard()
                } else {
                    otpTextInputLayout.error = "6자리의 인증번호를 입력해주세요."
                }
            }
        }
    }

    fun onClickBackBtn() {
        navigator.popBackStack()
    }

    fun onClickInputDone() {
        if (!validOtp()) {
            binding.otpTextInputLayout.error = "6자리의 인증번호를 입력해주세요."
            return
        }

        val key = binding.otpEt.text.toString()
        viewModel.sendNokIdentity(NokIdentityRequest(key, args.name, args.phone))
    }

    private fun validOtp() = !binding.otpEt.text.isNullOrBlank()
            && REGEX_OTP.toRegex().matches(binding.otpEt.text!!)

    companion object {
        private const val REGEX_OTP = "^([0-9]{6})"
    }
}