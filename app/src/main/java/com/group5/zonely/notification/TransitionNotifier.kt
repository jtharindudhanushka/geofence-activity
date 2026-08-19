package com.group5.zonely.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.group5.zonely.MainActivity
import com.group5.zonely.R
import com.group5.zonely.domain.model.TransitionType
import com.group5.zonely.domain.repository.GeofenceZoneRepository
import com.group5.zonely.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransitionNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val zoneRepository: GeofenceZoneRepository,
    private val settingsRepository: SettingsRepository
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        const val CHANNEL_ID = "geofence_transitions"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun showTransitionNotification(zoneId: String, transition: TransitionType) {
        val settings = settingsRepository.settings.first()
        if (!settings.notificationsEnabled) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val zone = zoneRepository.getZone(zoneId) ?: return
        val time = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date())

        val title: String
        val body: String
        val icon: Int

        when (transition) {
            TransitionType.ENTER -> {
                title = context.getString(R.string.notification_entered_title, zone.name)
                body = context.getString(R.string.notification_entered_body, zone.name, time)
                icon = android.R.drawable.ic_dialog_map // Placeholder icon
            }
            TransitionType.EXIT -> {
                title = context.getString(R.string.notification_left_title, zone.name)
                body = context.getString(R.string.notification_left_body, zone.name, time)
                icon = android.R.drawable.ic_dialog_map // Placeholder icon
            }
            else -> return // S0 only handles ENTER and EXIT
        }

        // Deep link to History
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "history")
        }
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .apply {
                if (settings.vibrationEnabled) {
                    setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                }
            }
            .build()

        // Distinct ID per zone to avoid overwriting
        notificationManager.notify(zoneId.hashCode(), notification)
    }
}
