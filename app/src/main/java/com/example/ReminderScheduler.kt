package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"
    private const val ALARM_REQUEST_CODE = 4242

    // Calculates the next trigger LocalDateTime for a single Reminder
    fun calculateNextTrigger(reminder: Reminder, baseDateTime: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        val defaultTime = try {
            val parts = reminder.reminderTime.split(":")
            LocalTime.of(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            LocalTime.of(8, 0)
        }

        val baseDate = baseDateTime.toLocalDate()
        val baseTime = baseDateTime.toLocalTime()

        when (reminder.type) {
            "TAMIL" -> {
                val tMonth = reminder.tamilMonth ?: 1
                val tDate = reminder.tamilDate ?: 1
                var targetYear = baseDate.year

                // Try to find the date in the current Gregorian year
                var gDate = TamilCalendarHelper.findGregorianDate(targetYear, tMonth, tDate)
                var alertDate = gDate?.minusDays(reminder.remindBeforeDays.toLong())
                var alertDateTime = alertDate?.let { LocalDateTime.of(it, defaultTime) }

                // If not found or if the calculated alert time has already passed, check the next year
                if (alertDateTime == null || alertDateTime.isBefore(baseDateTime)) {
                    targetYear += 1
                    gDate = TamilCalendarHelper.findGregorianDate(targetYear, tMonth, tDate)
                    alertDate = gDate?.minusDays(reminder.remindBeforeDays.toLong())
                    alertDateTime = alertDate?.let { LocalDateTime.of(it, defaultTime) }
                }
                return alertDateTime
            }

            "ENGLISH" -> {
                val targetDay = reminder.englishDayOfMonth ?: 1
                if (reminder.repeatSetting == "MONTHLY") {
                    var testYear = baseDate.year
                    var testMonth = baseDate.monthValue
                    
                    for (i in 0..12) {
                        val maxDay = LocalDate.of(testYear, testMonth, 1).lengthOfMonth()
                        val actualDay = min(targetDay, maxDay)
                        val gDate = LocalDate.of(testYear, testMonth, actualDay)
                        val alertDate = gDate.minusDays(reminder.remindBeforeDays.toLong())
                        val alertDateTime = LocalDateTime.of(alertDate, defaultTime)
                        
                        if (alertDateTime.isAfter(baseDateTime)) {
                            return alertDateTime
                        }
                        
                        testMonth++
                        if (testMonth > 12) {
                            testMonth = 1
                            testYear++
                        }
                    }
                } else {
                    // One-time or Yearly (based on the current or next year)
                    var targetYear = baseDate.year
                    // Set to English month when reminder was created or current month
                    val targetMonth = baseDate.monthValue 
                    
                    val maxDay = LocalDate.of(targetYear, targetMonth, 1).lengthOfMonth()
                    val actualDay = min(targetDay, maxDay)
                    var gDate = LocalDate.of(targetYear, targetMonth, actualDay)
                    var alertDate = gDate.minusDays(reminder.remindBeforeDays.toLong())
                    var alertDateTime = LocalDateTime.of(alertDate, defaultTime)

                    if (alertDateTime.isBefore(baseDateTime)) {
                        targetYear++
                        val nextMax = LocalDate.of(targetYear, targetMonth, 1).lengthOfMonth()
                        val nextActual = min(targetDay, nextMax)
                        gDate = LocalDate.of(targetYear, targetMonth, nextActual)
                        alertDate = gDate.minusDays(reminder.remindBeforeDays.toLong())
                        alertDateTime = LocalDateTime.of(alertDate, defaultTime)
                    }
                    return alertDateTime
                }
            }

            "MOON" -> {
                val moonPhase = reminder.moonPhaseType ?: "POURNAMI"
                var testDate = baseDate
                
                // Search looking ahead starting from today
                for (i in 0..60) {
                    val matchDate = TamilCalendarHelper.findNextMoonPhaseDate(testDate, moonPhase, reminder.remindBeforeDays)
                    val alertDateTime = LocalDateTime.of(matchDate, defaultTime)
                    
                    // Filter by specific Tamil Month if specified (e.g. Karthikai Deepam in Month 8, Diwali in Month 7)
                    if (reminder.tamilMonth != null) {
                        // The actual moon phase occurrence date (not the alert date)
                        val actualMoonPhaseDate = matchDate.plusDays(reminder.remindBeforeDays.toLong())
                        val tDate = TamilCalendarHelper.getTamilDate(
                            actualMoonPhaseDate.year, 
                            actualMoonPhaseDate.monthValue, 
                            actualMoonPhaseDate.dayOfMonth
                        )
                        if (tDate.tamilMonthIndex != reminder.tamilMonth) {
                            testDate = testDate.plusDays(1)
                            continue
                        }
                    }

                    if (alertDateTime.isAfter(baseDateTime)) {
                        return alertDateTime
                    }
                    testDate = testDate.plusDays(1)
                }
            }

            "CUSTOM" -> {
                val customStr = reminder.customGregorianDate ?: return null
                val targetDate = try {
                    LocalDate.parse(customStr, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    baseDate
                }
                
                val alertDate = targetDate.minusDays(reminder.remindBeforeDays.toLong())
                var alertDateTime = LocalDateTime.of(alertDate, defaultTime)

                if (alertDateTime.isBefore(baseDateTime)) {
                    when (reminder.repeatSetting) {
                        "DAILY" -> {
                            while (alertDateTime.isBefore(baseDateTime)) {
                                alertDateTime = alertDateTime.plusDays(1)
                            }
                        }
                        "WEEKLY" -> {
                            while (alertDateTime.isBefore(baseDateTime)) {
                                alertDateTime = alertDateTime.plusWeeks(1)
                            }
                        }
                        "MONTHLY" -> {
                            while (alertDateTime.isBefore(baseDateTime)) {
                                alertDateTime = alertDateTime.plusMonths(1)
                            }
                        }
                        "YEARLY" -> {
                            while (alertDateTime.isBefore(baseDateTime)) {
                                alertDateTime = alertDateTime.plusYears(1)
                            }
                        }
                        else -> return null // Expired, cannot repeat
                    }
                }
                return alertDateTime
            }
        }
        return null
    }

    // Schedules the next chronological alarm from all active reminders
    fun scheduleNextAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val database = ReminderDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            val activeReminders = database.reminderDao().getActiveReminders()
            if (activeReminders.isEmpty()) {
                Log.d(TAG, "No active reminders found. Canceling alarm.")
                cancelAllAlarms(context, alarmManager)
                return@launch
            }

            var earliestDateTime: LocalDateTime? = null
            var earliestReminders = mutableListOf<Reminder>()

            val now = LocalDateTime.now()

            for (reminder in activeReminders) {
                val nextTrigger = calculateNextTrigger(reminder, now) ?: continue
                if (earliestDateTime == null || nextTrigger.isBefore(earliestDateTime)) {
                    earliestDateTime = nextTrigger
                    earliestReminders.clear()
                    earliestReminders.add(reminder)
                } else if (nextTrigger == earliestDateTime) {
                    earliestReminders.add(reminder)
                }
            }

            val targetTime = earliestDateTime
            if (targetTime == null) {
                Log.d(TAG, "No prospective trigger times in future.")
                cancelAllAlarms(context, alarmManager)
                return@launch
            }

            val triggerMs = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            Log.d(TAG, "Scheduling next alarm for $targetTime ($triggerMs ms) for reminders: ${earliestReminders.map { it.title }}")

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.ACTION_TRIGGER_NOTIFICATION"
                putExtra("TRIGGER_TIME_EPOCH", triggerMs)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            }
        }
    }

    private fun cancelAllAlarms(context: Context, alarmManager: AlarmManager) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.ACTION_TRIGGER_NOTIFICATION"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
