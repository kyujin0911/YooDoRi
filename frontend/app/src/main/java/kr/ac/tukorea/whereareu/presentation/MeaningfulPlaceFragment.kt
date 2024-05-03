package kr.ac.tukorea.whereareu.presentation

import android.content.Context.MODE_PRIVATE
import android.media.MediaCodec.MetricsConstants.MODE
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
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
    private lateinit var userMeaningfulListRVA: UserMeaningfulListRVA

    //    private var naverMap: NaverMap? = null

    override fun initObserver() {
    }

    override fun initView() {

        Log.d("Presentation", "MeaningfulPlaceFragment")

        binding.bottomSheetLayout.innerRv.layoutManager = LinearLayoutManager(context)
        userMeaningfulListRVA = UserMeaningfulListRVA()

        binding.bottomSheetLayout.innerRv.adapter = userMeaningfulListRVA
        userMeaningfulListRVA.submitList(userMeaningfulPlaceList.toString())

        val otherSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
        val key = otherSpf.getString("key", "")

        repeatOnStarted {
            viewModel.getMeaningfulPlaces("253050")

            userMeaningfulListRVA.submitList(viewModel.userMeaningfulPlaceList)

            viewModel.userMeaningfulPlaceList.forEach { userMeaningfulPlace ->
                val latitude = userMeaningfulPlace.latitude
                val longitude = userMeaningfulPlace.longitude

//                val marker = Marker()
//                with(marker) {
//                    position = LatLng(latitude, longitude)
//                    icon = MarkerIcons.YELLOW
//                    captionText = userMeaningfulPlace.address
//                    captionRequestedWidth = 400
//                    map = naverMap
            }
        }
    }
}