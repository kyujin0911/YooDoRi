package kr.ac.tukorea.whereareu.presentation.nok.setting

import android.content.Context.MODE_PRIVATE
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.setting.ModifyUserInfoRequest
import kr.ac.tukorea.whereareu.databinding.FragmentNokModifyUserInfoBinding
import kr.ac.tukorea.whereareu.presentation.SettingViewModel
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.extension.EditTextUtil.hideKeyboard
import kr.ac.tukorea.whereareu.util.extension.EditTextUtil.setOnEditorActionListener
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToastOnView
import kr.ac.tukorea.whereareu.util.extension.statusBarHeight

@AndroidEntryPoint
class ModifyDementiaInfoFragment :
    BaseFragment<FragmentNokModifyUserInfoBinding>(R.layout.fragment_modify_dementia_info) {
    private val viewModel: SettingViewModel by activityViewModels()
    private val navigator by lazy {
        findNavController()
    }

    override fun initObserver() {
        binding.viewModel = viewModel
        repeatOnStarted {
            viewModel.updateOtherUserInfo.collect { userInfo ->
                userInfo.let {
                    Log.d("updateDementiaInfo", userInfo.message)
                }
            }
        }
        repeatOnStarted {
            viewModel.toastEvent.collect{
                if (navigator.currentDestination?.id == R.id.modifyUserInfoFragment) {
                    navigator.popBackStack()
                }
                requireActivity().showToastOnView(requireContext(), it, binding.finishBtn.bottom)
            }
        }
    }

    override fun initView() {
        binding.layout.setPadding(0,requireActivity().statusBarHeight() ,0, 0)
        binding.view = this
        binding.userPhoneEt.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        with(binding) {
            userNameEt.setOnEditorActionListener(EditorInfo.IME_ACTION_NEXT) {
                if (validUserName()) {
                    userNameEt.error = null
                    userNameEt.hideKeyboard()
                } else {
                    userNameEt.error = "최소 2자의 한글을 입력해주세요"
                }
            }
            userPhoneEt.setOnEditorActionListener(EditorInfo.IME_ACTION_NEXT) {
                if (validUserPhone()) {
                    userPhoneEt.error = null
                    userPhoneEt.hideKeyboard()
                } else {
                    userPhoneEt.error = "전화번호 형식이 다릅니다.\n입력 예시) 010-1234-5678"
                }
            }
        }
        binding.finishBtn.setOnClickListener {
            binding.userNameEt.error = if (!validUserName()) "최소 2자의 한글을 입력해주세요" else null
            binding.userPhoneEt.error =
                if (!validUserPhone()) "전화번호 형식이 다릅니다.\n예시) 010-1234-5678" else null

            // 수정한 정보 저장
            val nokSpf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
            nokSpf.edit {
                putString("name", binding.userNameEt.text.toString())
                putString("phone", binding.userPhoneEt.text.toString())
                apply()
            }
            val spf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
            val key = spf.getString("key", "")

            viewModel.sendUpdateUserInfo(
                ModifyUserInfoRequest(
                    0,
                    key ?: "",
                    binding.userNameEt.text.toString().trim()!!,
                    binding.userPhoneEt.text.toString().trim()!!
                )
            )
        }
    }

    fun onClickBackBtn() {
        navigator.popBackStack()
    }

    private fun validUserName() = binding.userNameEt.text.isNullOrBlank()
            || REGEX_NAME.toRegex().matches(binding.userNameEt.text!!)

    private fun validUserPhone() = binding.userPhoneEt.text.isNullOrBlank()
            || REGEX_PHONE.toRegex().matches(binding.userPhoneEt.text!!)

    companion object {
        private const val REGEX_NAME = "^[가-힣]{2,}\n?"
        private const val REGEX_PHONE = "^01([016789])-([0-9]{3,4})-([0-9]{4})"
    }

}