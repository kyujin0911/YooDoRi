package kr.ac.tukorea.whereareu.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
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

    override fun onNewToken(token: String) {
        Log.d(TAG, "new Token: $token")

        // 토큰 값을 따로 저장
        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token", token).apply()
        editor.commit()
        Log.i(TAG, "성공적으로 토큰을 저장함")
    }

    // 포그라운드
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage!!.from)

        // Notification 메시지를 수신할 경우
        // remoteMessage.notification?.body!! 여기에 내용이 저장되있음
        // Log.d(TAG, "Notification Message Body: " + remoteMessage.notification?.body!!)

        //받은 remoteMessage의 값 출력해보기. 데이터메세지 / 알림메세지
        Log.d(TAG, "Message data : ${remoteMessage.data}")
        Log.d(TAG, "Message noti : ${remoteMessage.notification}")

        remoteMessage.notification?.apply {
            val intent = Intent(this@FCMService, NokMainActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(this@FCMService, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val builder = NotificationCompat.Builder(this@FCMService, "WhereAreU")
                .setSmallIcon(R.drawable.ic_whereareu_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(101, builder.build())
        }

        if(remoteMessage.data.isNotEmpty()){
            //알림생성
            sendNotification(remoteMessage)
//            Log.d(TAG, remoteMessage.data["title"].toString())
//            Log.d(TAG, remoteMessage.data["body"].toString())
        }else {
            Log.e(TAG, "data가 비어있습니다. 메시지를 수신하지 못했습니다.")
        }
    }

    /** 알림 생성 메서드 */
    private fun sendNotification(remoteMessage: RemoteMessage) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 알림 소리
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // 중요도 (HIGH: 상단바 표시 가능)
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
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
        //val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT)
        val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)

        // 알림 소리
//        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림에 대한 UI 정보, 작업
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 (HIGH: 상단바 표시 가능)
            .setSmallIcon(R.drawable.ic_logo_test) // 아이콘 설정
            .setContentTitle(remoteMessage.data["title"].toString()) // 제목
            .setContentText(remoteMessage.data["body"].toString()) // 메시지 내용

//            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground))
            .setLargeIcon(convertBitmap())
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(convertBitmap())
                .bigLargeIcon(null))


            .setAutoCancel(false) // 알람클릭시 삭제여부
            .setSound(soundUri)  // 알림 소리
            .setContentIntent(pendingIntent) // 알림 실행 시 Intent

//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Head up 알람 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setFullScreenIntent(pendingIntent, true)
        }

        // 알림 생성
        notificationManager.notify(uniId, notificationBuilder.build())

    }
    // 큰 아이콘 작성
    private fun convertBitmap(): Bitmap{
        val drawable = getDrawable(R.drawable.ic_logo_test)
        val bitmapDrawable = drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap

        return bitmap
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
    private fun getCustomDesign(title: String, message: String): RemoteViews {
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.notification)
        remoteViews.setTextViewText(R.id.notification_title_tv, title)
        remoteViews.setTextViewText(R.id.notification_body_tv, message)
        remoteViews.setImageViewResource(R.id.notification_iv, R.drawable.ic_launcher_foreground)
        return remoteViews
    }
}

///**
//"notificationBuilder" 알림 생성시 여러가지 옵션을 이용해 커스텀 가능.
//setSmallIcon : 작은 아이콘 (필수)
//setContentTitle : 제목 (필수)
//setContentText : 내용 (필수)
//setColor : 알림내 앱 이름 색
//setWhen : 받은 시간 커스텀 ( 기본 시스템에서 제공 )
//setShowWhen : 알림 수신 시간 ( default 값은 true, false시 숨길 수 있습니다 )
//setOnlyAlertOnce : 알림 1회 수신 ( 동일 아이디의 알림을 처음 받았을때만 알린다, 상태바에 알림이 잔존하면 무음 )
//setContentTitle : 제목
//setContentText : 내용
//setFullScreenIntent : 긴급 알림 ( 자세한 설명은 아래에서 설명합니다 )
//setTimeoutAfter : 알림 자동 사라지기 ( 지정한 시간 후 수신된 알림이 사라집니다 )
//setContentIntent : 알림 클릭시 이벤트 ( 지정하지 않으면 클릭했을때 아무 반응이 없고 setAutoCancel 또한 작동하지 않는다 )
//setLargeIcon : 큰 아이콘 ( mipmap 에 있는 아이콘이 아닌 drawable 폴더에 있는 아이콘을 사용해야 합니다. )
//setAutoCancel : 알림 클릭시 삭제 여부 ( true = 클릭시 삭제 , false = 클릭시 미삭제 )
//setPriority : 알림의 중요도를 설정 ( 중요도에 따라 head up 알림으로 설정할 수 있는데 자세한 내용은 밑에서 설명하겠습니다. )
//setVisibility : 잠금 화면내 알림 노출 여부
//Notification.VISIBILITY_PRIVATE : 알림의 기본 정보만 노출 (제목, 타이틀 등등)
//Notification.VISIBILITY_PUBLIC : 알림의 모든 정보 노출
//Notification.VISIBILITY_SECRET : 알림의 모든 정보 비노출
// */