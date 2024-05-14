package kr.ac.tukorea.whereareu.presentation.nok.history

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentLocationHistoryBinding
import kr.ac.tukorea.whereareu.domain.history.LocationHistory
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.history.adapter.LocationHistoryRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted
import kr.ac.tukorea.whereareu.util.extension.showToastShort
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Month

@AndroidEntryPoint
class LocationHistoryFragment :
    BaseFragment<FragmentLocationHistoryBinding>(R.layout.fragment_location_history),
    LocationHistoryRVA.OnLoadingListener{
    private val viewModel: LocationHistoryViewModel by activityViewModels()
    private val dialogViewModel: CalendarDialogViewModel by viewModels()
    private val locationHistoryRVA by lazy {
        LocationHistoryRVA().apply {
            setOnLoadingListener(this@LocationHistoryFragment)
        }
    }
    //private var tempList = mutableListOf<List<LocationHistoryDto>>()

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
            viewModel.locationHistoryEvent.collect{event ->
                when(event){
                    LocationHistoryViewModel.LocationHistoryEvent.FetchFail -> {
                        dismissLoadingDialog()
                        requireActivity().showToastShort(requireContext(), "선택한 날짜에 해당하는 위치 기록이\n존재하지 않습니다.")
                    }
                    is LocationHistoryViewModel.LocationHistoryEvent.FetchSuccess -> {
                        syncSeekBarWithLocationHistory(event.locationHistory)
                        locationHistoryRVA.submitList(event.locationHistory, kotlinx.coroutines.Runnable {
                            viewModel.setIstLoading(false)
                        })
                    }
                }
            }
        }

        repeatOnStarted {
            dialogViewModel.selectedDate.collect{date ->
                if (date == LocalDate.MIN){
                    binding.dateTv.text = "날짜를 선택해주세요."
                    return@collect
                }

                val year = date.year
                val month = date.month.value.toString()
                val day = date.dayOfMonth
                val dayOfWeek = translateDayOfWeekInKorean(date.dayOfWeek)
                Log.d("dayofweek", dayOfWeek)
                binding.dateTv.text = "${year}년 ${month}월 ${day}일, $dayOfWeek"
                //"2024-03-19"

                viewModel.fetchLocationHistory(date.toString())
                showLoadingDialog(requireContext())
            }
        }

        repeatOnStarted {
            viewModel.isLoadingComplete.collect{ isLoading ->
                if (!isLoading){
                    delay(200)
                    dismissLoadingDialog()
                }
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

        binding.calendarBtn.setOnClickListener {
            dialog.show(childFragmentManager, dialog.tag)
        }
    }

    private fun syncSeekBarWithLocationHistory(locationHistoryList: List<LocationHistory>) {
        val smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun getHorizontalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }
        }
        val display = this.requireContext().resources?.displayMetrics
        val deviceWidth = display?.widthPixels
        val deviceHeight = display?.heightPixels

        Log.d("device width", deviceWidth!!.times(0.05).toString())
        Log.d("time info x", binding.timeInfoTitleTv.right.toString())

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("progress", progress.toString())
                try {
                    val locationInfo = locationHistoryList[progress]
                    Log.d("seek bar location info", locationInfo.toString())
                    viewModel.setProgress(progress)
                    //smoothScroller.targetPosition = progress
                    /*binding.rv.layoutManager = LinearLayoutManager(requireContext()).apply {
                        orientation = LinearLayoutManager.HORIZONTAL
                    }*/
                    //(binding.rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(progress, binding.rv.get(progress).width)
                    //binding.rv.layoutManager?.startSmoothScroll(smoothScroller)
                    binding.rv.layoutManager?.scrollToPosition(progress)
                    Log.d("layout position", binding.rv.findViewHolderForLayoutPosition(progress)?.itemView?.width.toString())
                    Log.d("adapter position", binding.rv.computeHorizontalScrollOffset().toString())
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
        binding.rv.apply {
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
        viewModel.setIstLoading(false)
    }


    override fun onStop() {
        super.onStop()
        dialogViewModel.setSelectedDate(LocalDate.MIN)
        viewModel.setMaxProgress(0)
    }

    private fun translateDayOfWeekInKorean(dayOfWeek: DayOfWeek): String{
        return when(dayOfWeek){
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            DayOfWeek.SUNDAY -> "일요일"
        }
    }

    fun px2dp(px: Int, context: Context): Float {
        return px / ((context.resources.displayMetrics.densityDpi.toFloat()) / DisplayMetrics.DENSITY_DEFAULT)
    }
}