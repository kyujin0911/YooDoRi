package kr.ac.tukorea.whereareu.presentation.nok.history

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentLocationHistoryBinding
import kr.ac.tukorea.whereareu.domain.history.LocationHistory
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.history.adapter.LocationHistoryRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class LocationHistoryFragment :
    BaseFragment<FragmentLocationHistoryBinding>(R.layout.fragment_location_history),
    LocationHistoryRVA.OnLoadingListener{
    private val viewModel: LocationHistoryViewModel by activityViewModels()
    private val dialogViewModel: CalendarDialogViewModel by activityViewModels()
    private val locationHistoryRVA by lazy {
        LocationHistoryRVA().apply {
            setOnLoadingListener(this@LocationHistoryFragment)
        }
    }
    //private var tempList = mutableListOf<List<LocationHistoryDto>>()

    override fun initObserver() {
        repeatOnStarted {
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
        }
        repeatOnStarted {
            dialogViewModel.selectedDate.collect{
                Log.d("selected date", it)
            }
        }

        /*repeatOnStarted {
            viewModel.isLoadingComplete.collect{ isLoading ->
                if (isLoading){
                    showLoadingDialog(requireContext())
                } else {
                    delay(200)
                    dismissLoadingDialog()
                }
            }
        }*/
    }

    override fun initView() {
        //showLoadingDialog(requireContext())
        val dialog = CalendarDialogFragment()
        dialog.show(childFragmentManager, dialog.tag)
        binding.viewModel = viewModel
        initLocationHistoryRVA()

        //viewModel.fetchLocationHistory("2024-03-19", "253050")
        binding.next.setOnClickListener {
            binding.seekBar.progress += 1
            viewModel.setProgress(binding.seekBar.progress)
        }
        binding.previous.setOnClickListener {
            binding.seekBar.progress -= 1
            viewModel.setProgress(binding.seekBar.progress)
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

    fun px2dp(px: Int, context: Context): Float {
        return px / ((context.resources.displayMetrics.densityDpi.toFloat()) / DisplayMetrics.DENSITY_DEFAULT)
    }
}