package kr.ac.tukorea.whereareu.data.api.nok

import android.util.Log
import kr.ac.tukorea.whereareu.presentation.nok.NokMainActivity

class FCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // token을 서버로 전송
        Log.d("FCMService", "NewToken : $token")
        NokMainActivity.uploadToken(token)
    }

    fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
    }
}