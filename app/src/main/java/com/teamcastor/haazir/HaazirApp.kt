package com.teamcastor.haazir

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class HaazirApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )

    }
}