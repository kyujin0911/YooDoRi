package kr.ac.tukorea.whereareu.util.getUserInfo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GetUserInfoService: Service()  {

    @Inject
    lateinit var repository: UserInfoRepositoryImpl

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val dementiaKey = getDementiaKey()
        if (!getDementiaKey().isNullOrBlank())
            postUserInfo(dementiaKey.toString())
    }

    private fun postUserInfo(dementiaKey: String){

    }

    private fun getDementiaKey(): String?{
        val dementiaKeySpf = applicationContext.getSharedPreferences("User", MODE_PRIVATE)
        return dementiaKeySpf.getString("key", "")
    }

}