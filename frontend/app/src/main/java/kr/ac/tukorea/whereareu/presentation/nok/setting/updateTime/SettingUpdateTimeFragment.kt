package kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSettingUpdateTimeBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment

@AndroidEntryPoint
class SettingUpdateTimeFragment() :
    BaseFragment<FragmentSettingUpdateTimeBinding>(R.layout.fragment_setting_update_time) {
    private val timeList = arrayListOf<TimeData>(
        TimeData("1", true),
        TimeData("3", true),
        TimeData("5", false),
        TimeData("10", false),
        TimeData("15", false),
        TimeData("20", false),
        TimeData("30", false),
        TimeData("45", false),
        TimeData("60", false)
    )
    private lateinit var timeAdapter: TimeAdapter

    override fun initView() {
        initializeViews()
    }

    override fun initObserver() {
        // Observer initialization, if any
    }

    private fun initializeViews() {
        binding.updateTimeList.layoutManager = LinearLayoutManager(context)
        timeAdapter = TimeAdapter(timeList)
        binding.updateTimeList.adapter = timeAdapter
    }


//    override fun initObserver() {
//
//    }
//
//    override fun initView() {
//
//    }

    fun onClickBackBtn() {
        findNavController().popBackStack()
    }
}