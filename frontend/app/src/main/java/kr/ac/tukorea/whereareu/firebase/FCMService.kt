package kr.ac.tukorea.whereareu.firebase

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.ac.tukorea.whereareu.R
import kr.ac.tukorea.whereareu.presentation.nok.NokMainActivity


class FCMService : FirebaseMessagingService() {
//    푸시 알림으로 보낼 수 있는 메세지는 2가지
//    1. Notification: 앱이 실행중(포그라운드)일 떄만 푸시 알림이 옴
//    2. Data: 실행중이거나 백그라운드(앱이 실행중이지 않을때) 알림이 옴

    private val TAG = "FirebaseService"

    //channel 설정
    private val channelId: String = "WhereAreU" // 알림 채널 이름
    private val channelName = "어디U Cnannel"
    private val channelDescription = "어디U를 위한 채널"

    // 싱글톤
    // 잠금화면 알림 표시
    object PushUtils {
        private var mWakeLock: PowerManager.WakeLock? = null

        @SuppressLint("InvalidWakeLockTag")
        fun acquireWakeLock(context: Context) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            mWakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE, "WAKEUP"
            )
            mWakeLock!!.acquire(3000)
        }

        fun releaseWakeLock() {
            if (mWakeLock != null) {
                mWakeLock!!.release()
                mWakeLock = null
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        PushUtils.acquireWakeLock(this)
    }
    override fun onNewToken(token: String) {
        Log.d(TAG, "new Token: $token")

        // 토큰 값을 따로 저장
        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token", token).apply()
        editor.commit()
        Log.i(TAG, "토큰 저장")
    }

    // 포그라운드
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        PushUtils.acquireWakeLock(this)

        Log.d(TAG, "From: " + remoteMessage!!.from)

        // Notification 메시지를 수신할 경우
        // remoteMessage.notification?.body!! 여기에 내용이 저장되있음
        // Log.d(TAG, "Notification Message Body: " + remoteMessage.notification?.body!!)

        //받은 remoteMessage의 값 출력해보기. 데이터메세지 / 알림메세지
        Log.d(TAG, "Message data : ${remoteMessage.data}")
        Log.d(TAG, "Message noti : ${remoteMessage.notification}")

        // 포그라운드 알림 설정
//        remoteMessage.notification?.apply {
//            val intent = Intent(this@FCMService, NokMainActivity::class.java).apply{
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            }
//            val pendingIntent = PendingIntent.getActivity(this@FCMService, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//            val builder = NotificationCompat.Builder(this@FCMService, "WhereAreU")

//                .setContentTitle(title.toString())
//                .setContentText(remoteMessage.data["body"].toString())
//                .setContentText(body.toString())
//                .setContentText(remoteMessage.data["이도영"].toString())
//
//                .setSmallIcon(R.drawable.ic_whereareu_logo)
//                .setContentIntent(pendingIntent)
//                .setGroupSummary(true)
//                .setAutoCancel(true)
//                .setShowWhen(true)      // 잠금화면에서 알림 보여줌
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            Log.d(TAG, "using onMessageReceived_Background")
//
//            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.notify(101, builder.build())
//        }
        // ----여기까지 포그라운드 알림

        if(remoteMessage.data.isNotEmpty()){
            //알림생성
            sendNotification(remoteMessage)
            Log.d(TAG, "title: ${remoteMessage.data["title"].toString()}")
            Log.d(TAG, "body : ${remoteMessage.data["body"].toString()}")
        }else {
            Log.e(TAG, "data가 비어있습니다. 메시지를 수신하지 못했습니다.")
        }


    }

    // 백그라운드 알림 설정
    private fun sendNotification(remoteMessage: RemoteMessage) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 알림 소리
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // 중요도 (HIGH: 상단바 표시 가능)
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)

        }

        // RequestCode, Id를 고유값으로 지정하여 알림이 개별 표시
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        // 일회용 PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임
        val intent = Intent(this, NokMainActivity::class.java)
        //각 key, value 추가
        for(key in remoteMessage.data.keys){
            intent.putExtra(key, remoteMessage.data.getValue(key))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Activity Stack 을 경로만 남김(A-B-C-D-B => A-B)

        //23.05.22 Android 최신버전 대응 (FLAG_MUTABLE, FLAG_IMMUTABLE)
        //PendingIntent.FLAG_MUTABLE은 PendingIntent의 내용을 변경할 수 있도록 허용, PendingIntent.FLAG_IMMUTABLE은 PendingIntent의 내용을 변경할 수 없음
//        val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT)
        val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)

        // 알림 소리
//        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림에 대한 UI 정보, 작업
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 (HIGH: 상단바 표시 가능)
            .setSmallIcon(R.drawable.ic_whereareu_logo) // 아이콘 설정
            .setContentTitle(remoteMessage.data["title"].toString()) // 제목
//            .setContentText(remoteMessage.data["body"].toString()) // 메시지 내용
            .setContentText(remoteMessage.data["이도영"].toString()) // data
            .setGroupSummary(true)

            .setAutoCancel(true) // 알람클릭시 삭제여부
            .setSound(soundUri)  // 알림 소리
            .setContentIntent(pendingIntent) // 알림 실행 시 Intent
            .setShowWhen(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        Log.d(TAG, "using sendNotification_Foreground")

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Head up 알람 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_whereareu_logo)
                .setFullScreenIntent(pendingIntent, true)
            Log.d(TAG, "headUp noti")
        }

        // 백그라운드 알림 보냄
        notificationManager.notify(uniId, notificationBuilder.build())

    }

//  토큰 가져오는 함수
    fun getFirebaseToken() {
        //비동기 방식
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d(TAG, "token=${it}")
        }

		  //동기방식
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d(TAG, "Fetching FCM registration token failed ${task.exception}")
                    return@OnCompleteListener
                }
                var deviceToken = task.result
                Log.e(TAG, "token=${deviceToken}")
            })
    }
}