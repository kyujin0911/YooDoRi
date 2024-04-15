package kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSettingUpdateTimeBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel

@AndroidEntryPoint
class SettingUpdateTimeFragment() :
    BaseFragment<FragmentSettingUpdateTimeBinding>(R.layout.fragment_setting_update_time) {
    private val viewModel: SettingViewModel by activityViewModels()
    private val timeList = arrayListOf<TimeData>(
        TimeData("1", true),
        TimeData("3", false),
        TimeData("5", false),
        TimeData("10", false),
        TimeData("15", false),
        TimeData("20", false),
        TimeData("30", false),
        TimeData("45", false),
        TimeData("60", false)
    )
    private var timeAdapter: TimeAdapter? = null

    override fun initView() {
        initializeViews()
    }

    override fun initObserver() {
    }

    private fun initializeViews() {
        binding.view = this
        var position = invertTime(viewModel.settingTime.value.toInt())
        binding.updateTimeList.layoutManager = LinearLayoutManager(context)
        timeAdapter = TimeAdapter(timeList, position)
        binding.updateTimeList.adapter = timeAdapter
        timeAdapter?.submitList(timeList.toMutableList()) // ListAdapter를 사용하기 위해 작성
        timeAdapter?.setOnItemClickListener(object : TimeAdapter.OnItemClickListener {
            override fun onItemClick(item: TimeData, position: Int) {
                viewModel.setSettingTime(positionToTime(position))
            }
        })
    }

    private fun invertTime(time: Int): Int {
        return when (time) {
            1 -> 0
            3 -> 1
            5 -> 2
            10 -> 3
            15 -> 4
            20 -> 5
            30 -> 6
            45 -> 7
            60 -> 8
            else -> 0
        }
    }

    private fun positionToTime(position: Int): String{
        return when (position) {
            0 -> "1"
            1 -> "3"
            2 -> "5"
            3 -> "10"
            4 -> "15"
            5 -> "20"
            6 -> "30"
            7 -> "45"
            8 -> "60"
            else -> "1"
        }
    }

    fun onClickBackBtn() {
        findNavController().popBackStack()
    }
}