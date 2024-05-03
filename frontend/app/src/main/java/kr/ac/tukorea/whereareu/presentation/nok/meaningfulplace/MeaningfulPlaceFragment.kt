package kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulPlaceBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.nok.meaningfulplace.adapter.UserMeaningfulListRVA
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class MeaningfulPlaceFragment :
    BaseFragment<FragmentMeaningfulPlaceBinding>(R.layout.fragment_meaningful_place) {
    private val viewModel: MeaningfulPlaceViewModel by activityViewModels()
    private lateinit var userMeaningfulListRVA: UserMeaningfulListRVA

    //    private var naverMap: NaverMap? = null

    override fun initObserver() {
        repeatOnStarted {
            viewModel.userMeaningfulPlaceList.collect {
                userMeaningfulListRVA.submitList(it)
                Log.d("MeaningfulPlaceFragment", "collect")
            }
        }
    }

    override fun initView() {
        Log.d("Presentation", "MeaningfulPlaceFragment")

        initUserMeaningfulListRVA()
        getUserMeaningfulPlaces()
    }

    private fun initUserMeaningfulListRVA(){
        binding.bottomSheetLayout.innerRv.layoutManager = LinearLayoutManager(context)
        userMeaningfulListRVA = UserMeaningfulListRVA()

        binding.bottomSheetLayout.innerRv.adapter = userMeaningfulListRVA
    }

    private fun getUserMeaningfulPlaces(){
        val otherSpf = requireActivity().getSharedPreferences("OtherUser", MODE_PRIVATE)
        val key = otherSpf.getString("key", "")

        viewModel.getMeaningfulPlaces("253050")
    }
}