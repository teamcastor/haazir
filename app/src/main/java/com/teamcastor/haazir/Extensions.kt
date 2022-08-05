package com.teamcastor.haazir

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence

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
