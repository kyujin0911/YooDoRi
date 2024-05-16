package kr.ac.tukorea.whereareu.presentation.base

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kr.ac.tukorea.whereareu.presentation.LoadingDialog

abstract class BaseActivity<V : ViewDataBinding>(@LayoutRes val layoutResource: Int) :
    AppCompatActivity() {

    private var _binding: V? = null
    protected val binding: V get() = _binding!!
    abstract fun initView()
    protected abstract fun initObserver()

    private lateinit var loadingDialog: LoadingDialog
    private var loadingState = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, layoutResource)
        binding.lifecycleOwner = this
        enableEdgeToEdge()
        setContentView(binding.root)
        initObserver()
        initView()
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

}