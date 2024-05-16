package kr.ac.tukorea.whereareu.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import kr.ac.tukorea.whereareu.presentation.LoadingDialog

abstract class BaseFragment<V : ViewDataBinding>(@LayoutRes val layoutResource: Int) : androidx.fragment.app.Fragment() {

    private var _binding: V? = null
    protected val binding: V get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog
    private var loadingState = false
    abstract fun initObserver()
    abstract fun initView()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            layoutInflater,
            layoutResource,
            null,
            false
        )
        binding.lifecycleOwner = this.viewLifecycleOwner
        return binding.root
    }

    fun showLoadingDialog(context: Context, text: String) {
        if (!loadingState) {
            loadingDialog = LoadingDialog(context, text)
            loadingDialog.show()
            loadingState = true
        }

    }
    fun dismissLoadingDialog() {
        if (loadingState) {
            loadingDialog.dismiss()
            loadingState = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}