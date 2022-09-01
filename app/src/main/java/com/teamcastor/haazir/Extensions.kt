package com.teamcastor.haazir

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.Geofence
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "GeofenceChannel"

fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, "Channel1", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

// extension function
fun NotificationManager.sendGeofenceNotification(context: Context, transition: Int) {

    // Opening the notification
    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    var message = ""
    message = if (transition == Geofence.GEOFENCE_TRANSITION_ENTER)
        "You have entered geofence"
    else
        "You have exited geofence"

    // Building the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_outline_location_on_24)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(contentPendingIntent)
        .build()

    this.notify(NOTIFICATION_ID, builder)
}

fun Long.toTimeIST(): String {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val time = Date(this)
    return timeFormat.format(time)
}

fun Long.toYear(): String {
    val dateFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    val year = Date(this)
    return dateFormat.format(year)
}

fun Long.toMonth(): String {
    val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
    val month = Date(this)
    return dateFormat.format(month)
}

fun Long.toDate(): String {
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    val date = Date(this)
    return dateFormat.format(date)
}

fun Long.toHumanDate(): String {
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val date = Date(this)
    return dateFormat.format(date)
}

fun Long.toDay(): String {
    val dateFormat = SimpleDateFormat("E", Locale.getDefault())
    val date = Date(this)
    return dateFormat.format(date)
}

fun Long.toDurationHM(): String {
    return this.toDuration(DurationUnit.MILLISECONDS).toComponents { hours, minutes, _, _ ->
        "%2dh %2dm".format(hours, minutes)
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap =
    Bitmap.createBitmap(
        this,
        0,
        0,
        width,
        height,
        Matrix().apply { postRotate(degrees) },
        true
    )
fun EditText.attachLiveData(owner: LifecycleOwner, liveData: MutableLiveData<String?>) {
    watch { liveData.value = text.toString() }
    liveData.observe(owner, Observer { value ->
        if (value == text.toString()) {
            return@Observer
        }
        setText(value)
    })
}

private fun EditText.watch(afterTextChanged: Editable.() -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            s.afterTextChanged()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
    })
}

fun Rect.scale(factor: Float) {
    val oldWidth = width()
    val oldHeight = height()
    val rectCenterX = left + oldWidth / 2
    val rectCenterY = top + oldHeight / 2
    val newWidth = oldWidth * factor
    val newHeight = oldHeight * factor
    left = (rectCenterX - newWidth / 2).toInt()
    right = (rectCenterX + newWidth / 2).toInt()
    top = (rectCenterY - newHeight / 2).toInt()
    bottom = (rectCenterY + newHeight / 2).toInt()
}

fun Bitmap.flipH(): Bitmap {
    val matrix = Matrix()
    matrix.postScale(-1f, 1f, this.width / 2f, this.height / 2f)
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}
