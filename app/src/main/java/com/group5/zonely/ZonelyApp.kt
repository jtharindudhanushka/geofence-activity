package com.group5.zonely

import android.app.Application
import com.group5.zonely.notification.TransitionNotifier
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZonelyApp : Application() {
    
    @Inject
    lateinit var transitionNotifier: TransitionNotifier

    override fun onCreate() {
        super.onCreate()
        transitionNotifier.createNotificationChannel()
    }
}
