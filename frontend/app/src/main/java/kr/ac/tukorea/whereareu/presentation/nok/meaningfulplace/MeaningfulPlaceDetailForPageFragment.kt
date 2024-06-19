package kr.ac.tukorea.whereareu.presentation.nok.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.nok.home.TimeInfo
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulPlaceDetailBinding
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulPlaceDetailForPageBinding
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.PoliceStationRVA
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.TimeInfoRVA
import kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.MeaningfulPlaceViewModel
import kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter.PoliceStationRVAForPage
import kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter.TimeInfoRVAForPage
import kr.ac.tukorea.whereareu.util.extension.showToastShort
import java.util.Calendar
import java.util.Date

class MeaningfulPlaceDetailForPageFragment :
    BaseFragment<FragmentMeaningfulPlaceDetailForPageBinding>(R.layout.fragment_meaningful_place_detail_for_page),
    PoliceStationRVAForPage.PoliceStationRVAForPageClickListener {
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val args: MeaningfulPlaceDetailForPageFragmentArgs by navArgs()
    private val viewModel: MeaningfulPlaceViewModel by activityViewModels()
    private val policeStationRVAForPage = PoliceStationRVAForPage()
    private val timeInfoRVAForPage = TimeInfoRVAForPage()
    override fun initObserver() {
    }

    override fun initView() {
        Log.d("args meanigfulPlaceForPage", args.meaningfulPlaceForPage.toString())
        binding.backBtn.setOnClickListener {
            navigator.popBackStack()
        }
        binding.addressTv.text = args.meaningfulPlaceForPage.address

        val timeInfoList =
            groupTimeInfoList(args.meaningfulPlaceForPage.timeInfo.groupBy { it.dayOfTheWeek })
        Log.d("timeInfoList", timeInfoList.toString())

        initRVA()
        initRadioGroup()
    }

    private fun initRVA() {
        with(binding) {
            policeStationRVAForPage.setPoliceStationRVAForPageClickListener(this@MeaningfulPlaceDetailForPageFragment)
            policeRv.adapter = policeStationRVAForPage
            policeStationRVAForPage.submitList(args.meaningfulPlaceForPage.policeStationInfo)
            args.meaningfulPlaceForPage.timeInfo
        }
    }

    private fun initRadioGroup() {
        val timeInfoList =
            groupTimeInfoList(args.meaningfulPlaceForPage.timeInfo.groupBy { it.dayOfTheWeek })
        val dayOfWeek = getCurrentWeek()
        Log.d("dayOfWeek", dayOfWeek.toString())
        binding.radioGroup.check(dayOfWeek)
        binding.timeInfoTv.text = getTimeInfoOfDayOfWeek(timeInfoList, dayOfWeek)
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            binding.timeInfoTv.text = getTimeInfoOfDayOfWeek(timeInfoList, checkedId)
        }
    }

    private fun getTimeInfoOfDayOfWeek(
        groupedTimeInfo: List<GroupedTimeInfo>,
        checkedId: Int
    ): String {
        val dayOfWeek = when (checkedId) {
            R.id.monday -> "Monday"
            R.id.tuesday -> "Tuesday"
            R.id.wednesday -> "Wednesday"
            R.id.thursday -> "Thursday"
            R.id.friday -> "Friday"
            R.id.saturday -> "Saturday"
            R.id.sunday -> "Sunday"
            else -> ""
        }

        val text = StringBuilder()
        val timeInfoList = groupedTimeInfo.filter { it.dayOfTheWeek == dayOfWeek }
        return if (timeInfoList.isEmpty()) {
            "방문 기록 없음"
        } else {
            val last = timeInfoList.first().timeList.last()
            timeInfoList.first().timeList.forEach {time ->
                if(time == last){
                    text.append(time)
                } else {
                    text.append("$time,  ")
                }
            }
            "시간  $text"
        }
    }

    private fun getCurrentWeek(): Int {
        val currentDate = Date()
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTime(currentDate)
        return when(calendar.get(Calendar.DAY_OF_WEEK)){
            1 -> R.id.sunday
            2 -> R.id.monday
            3 -> R.id.tuesday
            4 -> R.id.wednesday
            5 -> R.id.thursday
            6 -> R.id.friday
            7 -> R.id.saturday
            else -> 0
        }
    }

    private fun convertDayOfWeekInKorean(day: String): String {
        return when (day) {
            "Monday" -> "월"
            "Tuesday" -> "화"
            "Wednesday" -> "수"
            "Thursday" -> "목"
            "Friday" -> "금"
            "Saturday" -> "토"
            "Sunday" -> "일"
            else -> "알 수 없음"
        }
    }

    private fun convertTimeInKorean(time: String): String {
        return "${time.substring(0 until 2)}시~${time.substring(2 until 4)}시"
    }

    private fun groupTimeInfoList(timeInfoMap: Map<String, List<TimeInfo>>): List<GroupedTimeInfo> {
        val groupedTimeInfoList = mutableListOf<GroupedTimeInfo>()

        val dayOfWeeks = timeInfoMap.keys
        dayOfWeeks.forEach { dayOfWeek ->
            //val korean = convertDayOfWeekInKorean(dayOfWeek)
            val timeList = timeInfoMap[dayOfWeek]?.map { timeInfo ->
                "${timeInfo.time.substring(0 until 2)}시 - ${
                    timeInfo.time.substring(2 until 4)
                }시"
            }
            groupedTimeInfoList.add(GroupedTimeInfo(dayOfWeek, timeList!!))
        }
        return groupedTimeInfoList
    }

    override fun onClickMapView(policeStationInfo: PoliceStationInfo) {
        viewModel.eventMeaningful(
            MeaningfulPlaceViewModel.MeaningfulEvent.MapView(
                BottomSheetBehavior.STATE_COLLAPSED,
                policeStationInfo.latLng
            )
        )
    }

    override fun onClickCopyPhoneNumber(phoneNumber: String) {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", phoneNumber))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requireActivity().showToastShort(requireContext(), "전화번호가 복사되었습니다.")
        }
    }

    override fun onClickCopyAddress(address: String) {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", address))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requireActivity().showToastShort(requireContext(), "주소가 복사되었습니다.")
        }
    }
}