package com.example.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.ReminderDatabase
import com.example.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.random.Random

class ReminderReceiver : BroadcastReceiver() {
    private val TAG = "ReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive action = $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, 
            "TamilCalendar::ReminderWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L)

        if ("com.tamilcalendar.DELETE_REMINDER" == action) {
            val reminderId = intent.getIntExtra("REMINDER_ID", -1)
            Log.d(TAG, "DELETE_REMINDER action received for ID $reminderId")
            if (reminderId != -1) {
                val db = ReminderDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        db.reminderDao().deleteCompleted(reminderId)
                        Log.d(TAG, "Successfully deleted completed reminder $reminderId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting completed reminder", e)
                    } finally {
                        ReminderScheduler.scheduleNextAlarm(context)
                        if (wakeLock != null && wakeLock.isHeld) {
                            wakeLock.release()
                        }
                    }
                }
            } else {
                if (wakeLock != null && wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
            return
        }

        if ("com.tamilcalendar.REMINDER_ALARM" == action) {
            val db = ReminderDatabase.getDatabase(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = db.reminderDao().getActiveReminders()
                    val now = LocalDateTime.now()
                    
                    for (reminder in activeReminders) {
                        val triggerTime = ReminderScheduler.calculateNextTrigger(reminder, now.minusMinutes(10))
                        if (triggerTime != null) {
                            val isDue = triggerTime.toLocalDate() == now.toLocalDate() && 
                                        Math.abs(triggerTime.hour - now.hour) <= 1
                            
                            if (isDue) {
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

                                val notification = NotificationCompat.Builder(context, "tamil_calendar_reminders")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(reminder.title)
                                    .setContentText(reminder.description.ifEmpty { "Tamil Calendar Reminder" })
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                    .setAutoCancel(true)
                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                    .setContentIntent(pendingIntent)
                                    .build()

                                try {
                                    NotificationManagerCompat.from(context).notify(reminder.id + Random.nextInt(100000), notification)
                                    
                                    // Handle marking completed immediately for ONCE / ONE_TIME reminders
                                    val isOnce = reminder.repeatType == "ONCE" || reminder.repeatSetting == "ONE_TIME"
                                    if (isOnce) {
                                        db.reminderDao().markAsCompleted(reminder.id)
                                        val deleteTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                                        scheduleReminderDeletion(context, reminder.id, deleteTime)
                                    }
                                } catch (se: SecurityException) {
                                    Log.e(TAG, "SecurityException: no POST_NOTIFICATIONS permission", se)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error matching due reminders", e)
                } finally {
                    ReminderScheduler.scheduleNextAlarm(context)
                    if (wakeLock != null && wakeLock.isHeld) {
                        wakeLock.release()
                    }
                }
            }
        } else {
            if (wakeLock != null && wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun scheduleReminderDeletion(context: Context, reminderId: Int, deleteTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.tamilcalendar.DELETE_REMINDER"
            putExtra("REMINDER_ID", reminderId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            120000 + reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, deleteTime, pendingIntent)
                return
            }
        }
        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, deleteTime, pendingIntent)
    }
}
