package kr.ac.tukorea.whereareu.presentation.nok.safearea

import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.databinding.DialogSearchAddressBinding
import kr.ac.tukorea.whereareu.presentation.base.BaseDialogFragment
import kr.ac.tukorea.whereareu.presentation.base.BaseFragment
import kr.ac.tukorea.whereareu.util.extension.navigationHeight
import kr.ac.tukorea.whereareu.util.extension.setStatusBarTransparent
import kr.ac.tukorea.whereareu.util.extension.statusBarHeight

class SearchAddressDialogFragment: BaseDialogFragment<DialogSearchAddressBinding>(R.layout.dialog_search_address) {
    override fun initObserver() {

    }

    override fun initView() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}