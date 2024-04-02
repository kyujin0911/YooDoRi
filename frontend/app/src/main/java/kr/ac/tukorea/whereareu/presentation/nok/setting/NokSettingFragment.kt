package kr.ac.tukorea.whereareu.presentation.nok.setting

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentNokSettingBinding
import kr.ac.tukorea.whereareu.presentation.UserInfoViewModel
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class NokSettingFragment: BaseFragment<FragmentNokSettingBinding>(R.layout.fragment_nok_setting) {
    private val viewModel: NokHomeViewModel by activityViewModels()
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()

    override fun initObserver() {
    }
    override fun initView() {
        val spf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
        val otherSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
//        val upTime = requireActivity().getSharedPreferences("UpdateTime", MODE_PRIVATE)

        binding.userNameTv.text = spf.getString("name", "")
        binding.userPhoneNumberTv.text = spf.getString("phone", "")

        binding.otherNameTv.setText(otherSpf.getString("name", ""))
        binding.otherPhoneTv.setText((otherSpf.getString("phone", "")))

        /*binding.testBtn.setOnClickListener {
            val duration = binding.durationEt.text.toString().toLong()
            viewModel.setUpdateDuration(duration * 10000)
        }*/

        binding.updateTimeBtn.setOnClickListener {
            val dialog = SetUpdateTimeDialogFragment{time ->
                viewModel.setUpdateDuration(time.toLong())
                binding.updateTimeTv.text = time
            }
            dialog.show(childFragmentManager, dialog.tag)
        }

        binding.updateUserInfoBtn.setOnClickListener{
            onClickUpdateUserInfo()
            Log.d("UpdateUserInfoBtn","Clicked")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("settingFragment", "onResume")
        val spf = requireActivity().getSharedPreferences("User", MODE_PRIVATE)
        val otherSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
        val key: String = otherSpf.getString("key", null) as String
        userInfoViewModel.getUserInfo(key)

        repeatOnStarted {
            Log.d("Nok Setting Fragment", "repeatOnStarted")
            userInfoViewModel.userInfo.collect{
                Log.d("Nok_Setting_Fragment", "get User Info API")

                val nokName = it.nokInfoRecord.nokName
                val nokPhone = it.nokInfoRecord.nokPhoneNumber
                val dementiaName = it.dementiaInfoRecord.dementiaName
                val dementiaPhone = it.dementiaInfoRecord.dementiaPhoneNumber
                Log.d("Nok_Setting_Fragment", "$nokName, $nokPhone, $dementiaName,$dementiaPhone")
            }
        }

        binding.userNameTv.text = spf.getString("name", "")
        binding.userPhoneNumberTv.text = spf.getString("phone", "")
        binding.otherNameTv.text = otherSpf.getString("name", "")
        binding.otherPhoneTv.text = otherSpf.getString("phone", "")
    }
    fun onClickUpdateUserInfo(){
        findNavController().navigate(R.id.action_nokSettingFragment_to_modifyUserInfoFragment2)
    }

    fun getUserInfo(){

    }
}