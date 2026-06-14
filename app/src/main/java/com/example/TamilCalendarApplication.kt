package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class TamilCalendarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Register Global Exception Handler to capture and recover from crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("TamilCalendarApp", "FATAL CRASH on thread ${thread.name}", throwable)
                
                // Persist the crash stack trace
                val prefs = getSharedPreferences("crash_logs", Context.MODE_PRIVATE)
                val trace = Log.getStackTraceString(throwable)
                prefs.edit().putString("last_crash_trace", trace).commit()
                
                // Attempt to display a local push notification (high priority)
                showCrashNotification(throwable)
            } catch (e: Exception) {
                Log.e("TamilCalendarApp", "Error executing crash recovery steps", e)
            } finally {
                // Terminate JVM safely
                System.exit(1)
            }
        }
        
        // Create standard notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
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
            
            val systemChannel = NotificationChannel(
                "tamil_calendar_system",
                "Tamil Calendar Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "System notification channel for calendar crash alerts"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(reminderChannel)
            manager?.createNotificationChannel(systemChannel)
        }
    }

    private fun showCrashNotification(throwable: Throwable) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("SHOW_CRASH_LOG", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                911,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, "tamil_calendar_system")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Tamil Calendar Error")
                .setContentText("Application recovered from an error. Tap to see details.")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Error:\n${throwable.localizedMessage}\n\nStacktrace:\n${Log.getStackTraceString(throwable).take(1000)}..."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(9911, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
