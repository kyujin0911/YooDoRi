package kr.ac.tukorea.whereareu.util.extension

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kr.ac.tukorea.whereareu.R

fun Context.hasLocationPermission(): Boolean{
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

fun Context.getUserKey(user: String): String{
    val spf = when(user){
        "nok" -> {
            this.getSharedPreferences("User", MODE_PRIVATE)
        }
        else -> {
            this.getSharedPreferences("OtherUser", MODE_PRIVATE)
        }
    }
    return spf.getString("key", "") ?: ""
}

fun ImageView.setRingtoneImage(context: Context, ringtone: Int){
    val drawableId = when(ringtone){
        0 -> R.drawable.ic_bell_off_24
        1 -> R.drawable.ic_vibrate_24
        2 -> R.drawable.ic_bell_24
        else -> 0
    }
    this.setImageDrawable(ContextCompat.getDrawable(context, drawableId))
}

fun TextView.setRingtoneText(ringtone: Int){
    val ringtoneText = when(ringtone){
        0 -> "무음"
        1 -> "진동"
        2 -> "벨소리"
        else -> "알 수 없음"
    }
    this.text = ringtoneText
}