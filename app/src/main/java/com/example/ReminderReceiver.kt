package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class ReminderReceiver : BroadcastReceiver() {
    private val TAG = "ReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive action = $action")

        if (Intent.ACTION_BOOT_COMPLETED == action || "android.intent.action.QUICKBOOT_POWERON" == action) {
            // Re-schedule alarm on boot
            ReminderScheduler.scheduleNextAlarm(context)
            return
        }

        if ("com.example.ACTION_TRIGGER_NOTIFICATION" == action) {
            val db = ReminderDatabase.getDatabase(context)
            
            // Acquire wake lock or run in IO coroutine
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = db.reminderDao().getActiveReminders()
                    val now = LocalDateTime.now()
                    
                    // Allow small threshold around current time (within 10 minutes)
                    for (reminder in activeReminders) {
                        val triggerTime = ReminderScheduler.calculateNextTrigger(reminder, now.minusMinutes(10))
                        if (triggerTime != null) {
                            // Check if the calculated trigger time matches "now" (broadly close, e.g. same day and approximate hour)
                            val isDue = triggerTime.toLocalDate() == now.toLocalDate() && 
                                        Math.abs(triggerTime.getHour() - now.getHour()) <= 1
                            
                            if (isDue) {
                                showNotification(context, reminder)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error matching due reminders", e)
                } finally {
                    // Reschedule next alarm
                    ReminderScheduler.scheduleNextAlarm(context)
                }
            }
        }
    }

    private fun showNotification(context: Context, reminder: Reminder) {
        val channelId = "tamil_calendar_reminders_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel starting on Android 8.0 Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tamil Calendar Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "System notifications for Tamil Calendar events and custom reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tapping notification opens MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_REMINDER_ID", reminder.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminder.id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Safe fallback system drawable
            .setContentTitle(reminder.title)
            .setContentText(reminder.description.ifEmpty { "Tamil Calendar Reminder" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reminder.id + Random.nextInt(100000), notification)
    }
}
