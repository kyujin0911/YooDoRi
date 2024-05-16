package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulPlaceDetailBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.presentation.login.nok.NokOtpFragmentArgs

class MeaningfulPlaceDetailFragment: BaseFragment<FragmentMeaningfulPlaceDetailBinding>(R.layout.fragment_meaningful_place_detail) {
    private val navigator: NavController by lazy {
        findNavController()
    }
    private val args: MeaningfulPlaceDetailFragmentArgs by navArgs()
    override fun initObserver() {

    }

    override fun initView() {
        Log.d("args meanigfulPlace", args.meaningfulPlace.toString())
        binding.back.setOnClickListener {
            navigator.popBackStack()
        }
    }
}