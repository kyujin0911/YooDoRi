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
import kr.ac.tukorea.whereareu.domain.home.GroupedTimeInfo
import kr.ac.tukorea.whereareu.domain.home.PoliceStationInfo
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.login.nok.NokOtpFragmentArgs
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.PoliceStationRVA
import kr.ac.tukorea.whereareu.presentation.nok.home.adapter.TimeInfoRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToastShort
import org.threeten.bp.DayOfWeek

class MeaningfulPlaceDetailFragment :
    BaseFragment<FragmentMeaningfulPlaceDetailBinding>(R.layout.fragment_meaningful_place_detail),
    PoliceStationRVA.PoliceStationRVAClickListener {
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val args: MeaningfulPlaceDetailFragmentArgs by navArgs()
    private val viewModel: NokHomeViewModel by activityViewModels()
    private val policeStationRVA = PoliceStationRVA()
    private val timeInfoRVA = TimeInfoRVA()
    override fun initObserver() {
    }

    override fun initView() {
        Log.d("args meanigfulPlace", args.meaningfulPlace.toString())
        binding.back.setOnClickListener {
            navigator.popBackStack()
        }

        initRVA()
    }

    private fun initRVA(){
        with(binding){
            policeStationRVA.setPoliceStationRVAClickListener(this@MeaningfulPlaceDetailFragment)
            policeRv.adapter = policeStationRVA
            policeStationRVA.submitList(args.meaningfulPlace.policeStationInfo)

            /*timeInfoRv.adapter = timeInfoRVA
            val timeInfo = args.meaningfulPlace.timeInfo.map { info ->
                TimeInfo(convertDayOfWeekInKorean(info.dayOfTheWeek), convertTimeInKorean(info.time))
            }
            timeInfoRVA.submitList(timeInfo)*/
            /*val timeInfo = args.meaningfulPlace.timeInfo
            val temp = timeInfo.map {
                DayOfWeek.valueOf(it.dayOfTheWeek.uppercase())
            }
            val temp2 = temp.sortedBy { it }.map {
                convertDayOfWeekInKorean(it.toString())
            }
            timeInfo.map{
                temp2.forEach { dayOfWeek ->
                    TimeInfo(dayOfWeek, it.time)
                }
            }
            val test = groupTimeInfoList(timeInfo.groupBy { it.dayOfTheWeek })
            Log.d("time group", test.toString())
            timeInfoRv.adapter = timeInfoRVA
            timeInfoRVA.submitList(test)*/
        }
    }

    private fun convertDayOfWeekInKorean(day: String): String {
        return when (day) {
            "MONDAY" -> "월"
            "TUESDAY" -> "화"
            "WEDNESDAY" -> "수"
            "THURSDAY" -> "목"
            "FRIDAY" -> "금"
            "SATURDAY" -> "토"
            "SUNDAY" -> "일"
            else -> "알 수 없음"
        }
    }

    private fun convertTimeInKorean(time: String): String{
        return "${time.substring(0 until 2)}시~${time.substring(2 until 4)}시"
    }

    private fun groupTimeInfoList(timeInfoMap: Map<String, List<TimeInfo>>): List<GroupedTimeInfo>{
        val groupedTimeInfoList = mutableListOf<GroupedTimeInfo>()

        val dayOfWeeks = timeInfoMap.keys
        dayOfWeeks.forEach { dayOfWeek ->
            val korean = convertDayOfWeekInKorean(dayOfWeek)
            val timeList = timeInfoMap[dayOfWeek]?.map { timeInfo ->
                "${timeInfo.time.substring(0 until 2)}시 - ${
                    timeInfo.time.substring(2 until 4)
                }시"
            }
            groupedTimeInfoList.add(GroupedTimeInfo(korean, timeList!!))
        }

        return groupedTimeInfoList
    }

    override fun onClickMapView(policeStationInfo: PoliceStationInfo) {
        viewModel.eventPredict(NokHomeViewModel.PredictEvent.RVAClick(BottomSheetBehavior.STATE_COLLAPSED, policeStationInfo.latLng))
    }

    override fun onClickCopyPhoneNumber(phoneNumber: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", phoneNumber))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requireActivity().showToastShort(requireContext(), "전화번호가 복사되었습니다.")
        }
    }

    override fun onClickCopyAddress(address: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", address))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            requireActivity().showToastShort(requireContext(), "주소가 복사되었습니다.")
        }
    }
}