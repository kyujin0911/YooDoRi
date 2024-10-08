package kr.ac.tukorea.whereareu.presentation.nok.history

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentLocationHistoryBinding
import kr.ac.tukorea.whereareu.domain.history.LocationHistory
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.history.adapter.LocationHistoryRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

@AndroidEntryPoint
class LocationHistoryFragment :
    BaseFragment<FragmentLocationHistoryBinding>(R.layout.fragment_location_history),
    LocationHistoryRVA.OnLoadingListener {
    private val viewModel: LocationHistoryViewModel by activityViewModels()
    private val dialogViewModel: CalendarDialogViewModel by viewModels()
    private val locationHistoryRVA by lazy {
        LocationHistoryRVA().apply {
            setOnLoadingListener(this@LocationHistoryFragment)
        }
    }
    //private var tempList = mutableListOf<List<LocationHistoryDto>>()

    @SuppressLint("SetTextI18n")
    override fun initObserver() {
        /*repeatOnStarted {
            viewModel.locationHistory.collect { list ->
                Log.d("locationHistroy", "collect")
                syncSeekBarWithLocationHistory(list)
                locationHistoryRVA.submitList(list, kotlinx.coroutines.Runnable {
                    viewModel.setIstLoading(false)
                })
                /*val li = list.chunked(list.size/3)
                li.forEach{
                    tempList.add(it)
                }
                locationHistoryRVA.submitList(li[0])
                tempList.drop(0)*/
            }
        }*/

        repeatOnStarted {
            viewModel.locationHistoryEvent.collect { event ->
                Log.d("locationHistoryEvent", event.toString())
                when (event) {
                    LocationHistoryViewModel.LocationHistoryEvent.FetchFail -> {
                        dismissLoadingDialog()
                        /*requireActivity().showToastShort(
                            requireContext(),
                            "선택한 날짜에 해당하는 위치 기록이\n존재하지 않습니다."
                        )*/
                        Toast.makeText(requireContext(), "선택한 날짜에 해당하는 위치 기록이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        viewModel.setIsMultipleSelected(false)
                    }

                    is LocationHistoryViewModel.LocationHistoryEvent.FetchSuccessSingle -> {
                        syncSeekBarWithSingleLocationHistory(event.locationHistory)
                        locationHistoryRVA.submitList(
                            event.locationHistory,
                            kotlinx.coroutines.Runnable {
                                viewModel.setIsLoading(false)
                            })
                    }

                    is LocationHistoryViewModel.LocationHistoryEvent.FetchSuccessMultiple -> {
                        syncSeekBarWithMultipleLocationHistory(event.locationHistory[0], event.locationHistory[1])
                    }
                    else -> {}
                }
            }
        }

        repeatOnStarted {
            dialogViewModel.selectedDates.collect { dates ->
                Log.d("daes", dates.toString())
                if (dates.isEmpty() || dates[0] == LocalDate.MIN) {
                    binding.dateTv.text = "날짜를 선택해주세요."
                    return@collect
                }

                if (dates.size < 2) {
                    viewModel.setIsMultipleSelected(false)
                    binding.dateTv.text = getDateText(dates[0])
                    //"2024-03-19"
                    viewModel.fetchSingleLocationHistory(dates[0].toString())

                } else {
                    binding.dateTv.text = "위치 기록 비교"
                    viewModel.setIsMultipleSelected(true)
                    viewModel.fetchMultipleLocationHistory(dates[0].toString(), dates[1].toString())
                    binding.dateInfoTv.text = dates[0].toString()
                    binding.dateInfoTv2.text = dates[1].toString()
                }
                showLoadingDialog(requireActivity(), "위치 기록을 조회 중입니다...")
            }
        }

        repeatOnStarted {
            viewModel.isLoadingComplete.collect { isLoading ->
                if (!isLoading) {
                    delay(200)
                    dismissLoadingDialog()
                }
            }
        }

        repeatOnStarted {
            viewModel.isMultipleSelected.collect{
                Log.d("isMultip", it.toString())
            }
        }
    }

    override fun initView() {
        val dialog = CalendarDialogFragment()
        //dialog.show(childFragmentManager, dialog.tag)
        binding.viewModel = viewModel
        initLocationHistoryRVA()

        binding.next.setOnClickListener {
            binding.seekBar.progress += 1
            viewModel.setProgress(binding.seekBar.progress)
        }

        binding.previous.setOnClickListener {
            binding.seekBar.progress -= 1
            viewModel.setProgress(binding.seekBar.progress)
        }

        binding.next2.setOnClickListener {
            binding.seekBar2.progress += 1
            viewModel.setProgress2(binding.seekBar2.progress)
        }

        binding.previous2.setOnClickListener {
            binding.seekBar2.progress -= 1
            viewModel.setProgress2(binding.seekBar2.progress)
        }

        binding.calendarBtn.setOnClickListener {
            dialog.show(childFragmentManager, dialog.tag)
        }
    }

    private fun syncSeekBarWithSingleLocationHistory(locationHistoryList: List<LocationHistory>) {
        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun getHorizontalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }
        }
        val display = this.requireContext().resources?.displayMetrics
        val deviceWidth = display?.widthPixels
        val deviceHeight = display?.heightPixels

        //Log.d("device width", deviceWidth!!.times(0.05).toString())
        //Log.d("time info x", binding.timeInfoTitleTv.right.toString())

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("progress", progress.toString())
                try {
                    val locationInfo = locationHistoryList[progress]
                    //Log.d("seek bar location info", locationInfo.toString())
                    viewModel.setProgress(progress)
                    //smoothScroller.targetPosition = progress
                    /*binding.rv.layoutManager = LinearLayoutManager(requireContext()).apply {
                        orientation = LinearLayoutManager.HORIZONTAL
                    }*/
                    //(binding.rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(progress, binding.rv.get(progress).width)
                    //binding.rv.layoutManager?.startSmoothScroll(smoothScroller)
                    binding.rv.layoutManager?.scrollToPosition(progress)
                    //Log.d( "layout position", binding.rv.findViewHolderForLayoutPosition(progress)?.itemView?.width.toString() )
                    //Log.d("adapter position", binding.rv.computeHorizontalScrollOffset().toString())
                } catch (e: Exception) {
                   // Log.d("IndexOutOfBoundsException", e.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

    private fun syncSeekBarWithMultipleLocationHistory(
        locationHistoryList: List<LocationHistory>,
        locationHistoryList2: List<LocationHistory>
    ) {
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("progress", progress.toString())
                try {
                    val locationInfo = locationHistoryList[progress]
                    viewModel.setProgress(progress)
                } catch (e: Exception) {
                    Log.d("IndexOutOfBoundsException", e.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.seekBar2.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("progress", progress.toString())
                try {
                    val locationInfo = locationHistoryList2[progress]
                    viewModel.setProgress2(progress)
                } catch (e: Exception) {
                    Log.d("IndexOutOfBoundsException", e.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initLocationHistoryRVA() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.rv.apply {
            setLayoutManager(layoutManager)
            adapter = locationHistoryRVA
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL
                )
            )
            itemAnimator = null
            setOnTouchListener({ v, event -> true })
        }


        /*binding.rv.addOnScrollListener(object : OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!binding.rv.canScrollHorizontally(1)){
                    Log.d("dsd", "dsd")
                    locationHistoryRVA.submitList(locationHistoryRVA.currentList.toMutableList().apply { addAll(tempList[0])})
                    tempList.drop(0)
                }
            }
        })*/
    }

    override fun onLoading() {
        Log.d("set is loading", "sds")
        viewModel.setIsLoading(false)
    }

    private fun translateDayOfWeekInKorean(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            DayOfWeek.SUNDAY -> "일요일"
        }
    }

    private fun getDateText(date: LocalDate): String {
        val year = date.year
        val month = date.month.value.toString()
        val day = date.dayOfMonth
        val dayOfWeek = translateDayOfWeekInKorean(date.dayOfWeek)

        return "${year}년 ${month}월 ${day}일, $dayOfWeek"
    }

    fun px2dp(px: Int, context: Context): Float {
        return px / ((context.resources.displayMetrics.densityDpi.toFloat()) / DisplayMetrics.DENSITY_DEFAULT)
    }
}