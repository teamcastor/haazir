package com.teamcastor.haazir

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.teamcastor.haazir.data.model.AppViewModel

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "GeoFenceBroadCastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent?.geofenceTransition

        // Test that the reported transition was of interest.
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER ->
                AppViewModel.geofenceStatusChanged(true)
            Geofence.GEOFENCE_TRANSITION_EXIT ->
                AppViewModel.geofenceStatusChanged(false)
            else -> {
                Log.e(TAG, "Invalid type transition $geofenceTransition")
                AppViewModel.geofenceStatusChanged(null)
            }
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Creating and sending notification
            val notificationManager = ContextCompat.getSystemService(
                context, NotificationManager::class.java
            ) as NotificationManager
            notificationManager.sendGeofenceNotification(context, geofenceTransition)
        } else {
            Log.e(TAG, "Invalid type transition $geofenceTransition")
        }
    }
}
