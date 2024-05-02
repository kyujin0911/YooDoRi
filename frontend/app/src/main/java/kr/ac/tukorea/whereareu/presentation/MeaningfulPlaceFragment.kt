package kr.ac.tukorea.whereareu.presentation

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulPlaceBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.home.NokHomeViewModel
import kr.ac.tukorea.whereareu.presentation.nok.home.meaningfulAdapter.UserMeaningfulListRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class MeaningfulPlaceFragment :
    BaseFragment<FragmentMeaningfulPlaceBinding>(R.layout.fragment_meaningful_place) {
    private val viewModel: NokHomeViewModel by activityViewModels()

    //    private var naverMap: NaverMap? = null
    private val userMeaningfulListRVA by lazy {
        UserMeaningfulListRVA()
    }

    override fun initObserver() {
        repeatOnStarted {
            viewModel.predictEvent.collect {
                getMeaningfulPlace(it)
            }
        }

    }

    override fun initView() {
        Log.d("Presentation","MeaningfulPlaceFragment")
        binding.bottomSheetLayout.innerRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = UserMeaningfulListRVA()
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun getMeaningfulPlace(event: NokHomeViewModel.PredictEvent) {
        when (event) {
            is NokHomeViewModel.PredictEvent.StartPredictEvent -> {
                viewModel.getMeaningfulPlace()
                initUserMeaningfulPlaceAdapter()
            }

            is NokHomeViewModel.PredictEvent.MeaningFulPlaceEvent -> {
                userMeaningfulListRVA.submitList(event.meaningfulPlaceForList)

                event.meaningfulPlaceForList.forEach { meaningfulPlace ->
                    val latitude = meaningfulPlace.latitude
                    val longitude = meaningfulPlace.longitude
                    val marker = Marker()
                    with(marker) {
                        position = LatLng(latitude, longitude)
                        icon = MarkerIcons.YELLOW
                        captionText = meaningfulPlace.address
                        captionRequestedWidth = 400
//                    map = naverMap
                    }
                }

            }
            else -> {}
        }
    }

    private fun initUserMeaningfulPlaceAdapter(){
        binding.bottomSheetLayout.innerRv.apply {
            adapter = userMeaningfulListRVA
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                ))}
    }
}