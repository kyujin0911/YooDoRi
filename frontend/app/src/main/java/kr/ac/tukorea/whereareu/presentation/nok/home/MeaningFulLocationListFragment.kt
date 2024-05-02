package kr.ac.tukorea.whereareu.presentation.nok.home

import android.util.Log
import androidx.fragment.app.activityViewModels
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.FragmentMeaningfulListBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseBottomSheetDialogFragment

class MeaningFulLocationListFragment: BaseBottomSheetDialogFragment<FragmentMeaningfulListBinding>(R.layout.fragment_meaningful_list) {
    private val viewModel: NokHomeViewModel by activityViewModels()
    override fun initObserver() {

    }

    override fun initView() {

    }
}