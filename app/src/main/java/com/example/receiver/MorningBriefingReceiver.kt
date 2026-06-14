package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.ReminderDatabase
import com.example.ReminderScheduler
import com.example.TamilCalendarHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MorningBriefingReceiver : BroadcastReceiver() {
    private val TAG = "MorningBriefing"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive action = $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TamilCalendar::MorningBriefingWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L)

        if ("com.tamilcalendar.MORNING_BRIEFING" == action) {
            val prefs = context.getSharedPreferences("tamil_calendar_settings", Context.MODE_PRIVATE)
            val briefingEnabled = prefs.getBoolean("morning_briefing_enabled", false)

            if (!briefingEnabled) {
                Log.d(TAG, "Morning briefing is disabled, skipping.")
                if (wakeLock != null && wakeLock.isHeld) wakeLock.release()
                return
            }

            val includeFestivals = prefs.getBoolean("include_festivals", true)
            val includeAuspicious = prefs.getBoolean("include_nakshatra", true)
            val includeInauspicious = prefs.getBoolean("include_rahu_kalam", true)
            val includeReminders = prefs.getBoolean("include_reminders", true)
            val includeTomorrow = prefs.getBoolean("include_tomorrow", true)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val todayLocalDate = LocalDate.now()
                    val tDate = TamilCalendarHelper.getTamilDate(
                        todayLocalDate.year,
                        todayLocalDate.monthValue,
                        todayLocalDate.dayOfMonth
                    )

                    val englishDate = todayLocalDate.dayOfMonth
                    val month = todayLocalDate.format(DateTimeFormatter.ofPattern("MMMM"))
                    val year = todayLocalDate.year
                    val dayName = todayLocalDate.format(DateTimeFormatter.ofPattern("EEEE"))

                    val sb = StringBuilder()
                    sb.append("📅 ").append(englishDate).append(" ").append(month).append(" ").append(year).append(" | ")
                    sb.append(tDate.tamilMonthNameEnglish).append(" ").append(tDate.tamilDay).append("\n")

                    if (includeAuspicious) {
                        val nakshatraName = tDate.nakshatra.split(" / ").lastOrNull() ?: tDate.nakshatra
                        sb.append("⭐ Nakshatra: ").append(nakshatraName).append("\n")
                    }

                    if (includeFestivals) {
                        if (tDate.isPournami) {
                            sb.append("🌕 Pournami — Full Moon\n")
                        }
                        if (tDate.isAmavasai) {
                            sb.append("🌑 Amavasai — New Moon\n")
                        }
                        val isFestival = tDate.festivalName != null && tDate.festivalName != "Pournami / பௌர்ணமி" && tDate.festivalName != "Amavasai / அமாவாசை"
                        if (isFestival && !tDate.festivalName.isNullOrEmpty()) {
                            val nameOnly = tDate.festivalName.split(" / ").first()
                            sb.append("🎉 Festival: ").append(nameOnly).append("\n")
                        }
                    }

                    if (includeInauspicious) {
                        val dayOfWeek = todayLocalDate.atStartOfDay(ZoneId.systemDefault()).let {
                            val c = java.util.Calendar.getInstance()
                            c.timeInMillis = it.toInstant().toEpochMilli()
                            c.get(java.util.Calendar.DAY_OF_WEEK)
                        }
                        val sunriseSunset = TamilCalendarHelper.getTodaySunriseSunset(todayLocalDate)
                        val rahu = TamilCalendarHelper.calculateRahuKalam(dayOfWeek, sunriseSunset.first, sunriseSunset.second)
                        val yama = TamilCalendarHelper.calculateYamagandam(dayOfWeek, sunriseSunset.first, sunriseSunset.second)

                        sb.append("🔴 Rahu Kalam: ").append(TamilCalendarHelper.formatTimeMillis(rahu.first)).append(" – ").append(TamilCalendarHelper.formatTimeMillis(rahu.second)).append("\n")
                        sb.append("🟡 Yamagandam: ").append(TamilCalendarHelper.formatTimeMillis(yama.first)).append(" – ").append(TamilCalendarHelper.formatTimeMillis(yama.second)).append("\n")
                    }

                    val db = ReminderDatabase.getDatabase(context)
                    val remindersList = db.reminderDao().getActiveReminders()

                    if (includeReminders) {
                        val todayReminders = remindersList.filter { r ->
                            val trigger = ReminderScheduler.calculateNextTrigger(r, todayLocalDate.atStartOfDay())
                            trigger?.toLocalDate() == todayLocalDate
                        }
                        if (todayReminders.isNotEmpty()) {
                            sb.append("🔔 ").append(todayReminders.size).append(" reminder(s) today\n")
                            todayReminders.take(2).forEach {
                                sb.append("   • ").append(it.title).append("\n")
                            }
                        }
                    }

                    if (includeTomorrow) {
                        val tomorrowLocalDate = todayLocalDate.plusDays(1)
                        val tomorrowReminders = remindersList.filter { r ->
                            val trigger = ReminderScheduler.calculateNextTrigger(r, tomorrowLocalDate.atStartOfDay())
                            trigger?.toLocalDate() == tomorrowLocalDate
                        }
                        if (tomorrowReminders.isNotEmpty()) {
                            sb.append("📌 Tomorrow: ").append(tomorrowReminders.first().title).append("\n")
                        }
                    }

                    createNotificationChannel(context)

                    val openIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        8888,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notificationText = sb.toString()
                    val notification = NotificationCompat.Builder(context, "tamil_calendar_briefing")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("🌅 Good Morning! $dayName")
                        .setContentText("Tap to read your complete morning calendar overview.")
                        .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText).setSummaryText("Tamil Calendar"))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    try {
                        NotificationManagerCompat.from(context).notify(202688, notification)
                        Log.d(TAG, "Morning briefing notification dispatched successfully!")
                    } catch (se: SecurityException) {
                        Log.e(TAG, "no POST_NOTIFICATIONS permission", se)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error compiling morning briefing", e)
                } finally {
                    val savedTime = prefs.getString("briefing_time", "07:00") ?: "07:00"
                    scheduleBriefing(context, savedTime)
                    if (wakeLock != null && wakeLock.isHeld) wakeLock.release()
                }
            }
        } else {
            if (wakeLock != null && wakeLock.isHeld) wakeLock.release()
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tamil Calendar Daily Briefing"
            val descriptionText = "Push notifications delivering compiled daily morning summaries."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("tamil_calendar_briefing", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun scheduleBriefing(context: Context, timeStr: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, MorningBriefingReceiver::class.java).apply {
                action = "com.tamilcalendar.MORNING_BRIEFING"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val parts = timeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    return
                }
            }
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Log.d("MorningBriefing", "Scheduled exact morning briefing alarm for ${calendar.time}")
        }

        fun cancelBriefing(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, MorningBriefingReceiver::class.java).apply {
                action = "com.tamilcalendar.MORNING_BRIEFING"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d("MorningBriefing", "Cancelled scheduled morning briefing alarm")
            }
        }
    }
}
