package kr.ac.tukorea.whereareu.presentation.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.data.model.login.request.UserLoginRequest
import kr.ac.tukorea.whereareu.presentation.dementia.DementiaMainActivity
import kr.ac.tukorea.whereareu.presentation.nok.NokMainActivity
import kr.ac.tukorea.whereareu.util.extension.repeatOnStarted

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {

        val spf: SharedPreferences = getSharedPreferences("User", MODE_PRIVATE)
        val key = spf.getString("key", "") as String
        val isDementia = spf.getBoolean("isDementia", true)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        enableEdgeToEdge()

        if (isDementia) {
            viewModel.sendUserLogin(UserLoginRequest(key, "1"))
        } else {
            viewModel.sendUserLogin(UserLoginRequest(key, "0"))
        }

        repeatOnStarted {
            viewModel.userLoginSuccess.collect { success ->
                val intent = if (success) {
                    if (isDementia) {
                        Intent(this@SplashActivity, DementiaMainActivity::class.java)
                    } else {
                        Intent(this@SplashActivity, NokMainActivity::class.java)
                    }
                } else {
                    Intent(this@SplashActivity, LoginActivity::class.java)
                }
                delay(1000)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}