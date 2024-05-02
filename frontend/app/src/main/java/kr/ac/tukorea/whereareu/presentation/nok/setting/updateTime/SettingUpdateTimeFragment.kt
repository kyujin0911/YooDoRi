package kr.ac.tukorea.whereareu.presentation.nok.setting.updateTime

import android.content.Context.MODE_PRIVATE
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentSettingUpdateTimeBinding
import kr.ac.tukorea.whereareu.presentation.nok.setting.SettingViewModel
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToastShort
import kr.ac.tukorea.whereareu.util.extension.statusBarHeight

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
    private lateinit var timeAdapter: TimeAdapter

    override fun initView() {
        binding.layout.setPadding(0, requireActivity().statusBarHeight(), 0, 0)
        initializeViews()

        binding.backBtn.setOnClickListener {
            onClickBackBtn()
        }
    }

    override fun initObserver() {
        repeatOnStarted {
            viewModel.toastEvent.collect{
                requireActivity().showToastShort(requireContext(), it)
                findNavController().popBackStack()
            }
        }
    }

    private fun initializeViews() {
        var position = invertTime(viewModel.updateRate.value.toInt())
        binding.updateTimeList.layoutManager = LinearLayoutManager(context)

        timeAdapter = TimeAdapter(timeList, position)
        binding.updateTimeList.adapter = timeAdapter
        timeAdapter.submitList(timeList.toMutableList()) // ListAdapter를 사용하기 위해 작성
        timeAdapter.setOnItemClickListener(object : TimeAdapter.OnItemClickListener {
            override fun onItemClick(item: TimeData, position: Int) {
                viewModel.setUpdateRate(positionToTime(position))
                /*viewModel.sendUpdateTime(
                    UpdateRateRequest(key, 0, item.title.toInt() * 60)
                )*/
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

    private fun positionToTime(position: Int): String {
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

    private fun onClickBackBtn() {
        val key =
            requireActivity().getSharedPreferences("User", MODE_PRIVATE).getString("key", "") ?: ""
        viewModel.sendUpdateTime(key, 0)
    }
}