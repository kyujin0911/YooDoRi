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
    private val viewModel: NokMeaningfulPlaceViewModel by activityViewModels()
    private lateinit var  userMeaningfulListRVA: UserMeaningfulListRVA

    //    private var naverMap: NaverMap? = null

    override fun initObserver() {

    }

    override fun initView() {
        Log.d("Presentation","MeaningfulPlaceFragment")
//        binding.bottomSheetLayout.innerRv.layoutManager = LinearLayoutManager(context)
        userMeaningfulListRVA = UserMeaningfulListRVA()
    }

    private fun getMeaningfulPlace() {

        when (event) {
            is NokHomeViewModel.PredictEvent.StartPredictEvent -> {
                viewModel.getUserMeaningfulPlace()
                initUserMeaningfulPlaceAdapter()
            }

            is NokHomeViewModel.PredictEvent.MeaningFulPlaceEvent -> {
                binding.root

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