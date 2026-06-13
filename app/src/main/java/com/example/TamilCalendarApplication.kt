package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build

class TamilCalendarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "tamil_calendar_reminders",
                "Tamil Calendar Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for Tamil Calendar events"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
