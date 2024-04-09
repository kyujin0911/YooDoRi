package kr.ac.tukorea.whereareu.presentation

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import kr.ac.tukorea.whereareu.databinding.DialogLoadingBinding

class LoadingDialog(context: Context): Dialog(context) {
    private lateinit var binding: DialogLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogLoadingBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable())
        window!!.setDimAmount(0.5f)
        setContentView(binding.root)
    }

}