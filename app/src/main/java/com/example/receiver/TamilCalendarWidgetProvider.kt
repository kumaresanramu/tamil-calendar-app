package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.Reminder
import com.example.ReminderDatabase
import com.example.TamilCalendarHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class TamilCalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("TamilWidget", "Widget onReceive action: ${intent.action}")
        // Refresh widget every hour or on requested update
        if (intent.action == "com.tamilcalendar.WIDGET_REFRESH" || 
            intent.action == Intent.ACTION_TIME_CHANGED || 
            intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, TamilCalendarWidgetProvider::class.java)
            )
            onUpdate(context, manager, ids)
            scheduleMidnightRefresh(context)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        // Run database queries on Dispatchers.IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val options = appWidgetManager.getAppWidgetOptions(widgetId)
                val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

                Log.d("TamilWidget", "Updating widgetId $widgetId: dims ${minWidth}x${minHeight}")

                // Determine layout size
                val layoutId = when {
                    minWidth == 0 || minHeight == 0 -> R.layout.widget_medium
                    minWidth >= 220 && minHeight >= 140 -> R.layout.widget_large
                    minWidth >= 200 -> R.layout.widget_medium
                    else -> R.layout.widget_small
                }

                val views = RemoteViews(context.packageName, layoutId)

                // Today details
                val today = LocalDate.now()
                val todayTamil = TamilCalendarHelper.getTamilDate(today.year, today.monthValue, today.dayOfMonth)

                val currentDay = today.dayOfMonth
                val currentDayName = today.dayOfWeek.name
                val englishMonthYear = "${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.year}"

                // English date number
                views.setTextViewText(R.id.widget_english_date, currentDay.toString())

                // Day name
                views.setTextViewText(R.id.widget_day_name, currentDayName)

                // English month year (exist in medium and large)
                try {
                    views.setTextViewText(R.id.widget_month_year, englishMonthYear)
                } catch (e: Exception) {}

                // Tamil date
                val tamilDateText = "${todayTamil.tamilMonthNameTamil} ${todayTamil.tamilDay}"
                // If it is small, just show "ஆடி 3"
                if (layoutId == R.layout.widget_small) {
                    views.setTextViewText(R.id.widget_tamil_date, tamilDateText)
                } else {
                    views.setTextViewText(R.id.widget_tamil_date, "${todayTamil.tamilMonthNameTamil} ${todayTamil.tamilDay} / ${todayTamil.tamilMonthNameEnglish} ${todayTamil.tamilDay}")
                }

                // Nakshatra
                val nakshatraName = todayTamil.nakshatra.split("/").first().trim() // extract English
                views.setTextViewText(R.id.widget_nakshatra, "⭐ $nakshatraName")

                // Moon phase
                if (layoutId != R.layout.widget_small) {
                    val isPournami = todayTamil.isPournami
                    val isAmavasai = todayTamil.isAmavasai
                    if (isPournami || isAmavasai) {
                        views.setViewVisibility(R.id.widget_moon_text, View.VISIBLE)
                        views.setTextViewText(
                            R.id.widget_moon_text,
                            if (isPournami) "🌕 Pournami Today" else "🌑 Amavasai Today"
                        )
                    } else {
                        views.setViewVisibility(R.id.widget_moon_text, View.GONE)
                    }
                }

                // Rahu / Yama (exist in large only)
                if (layoutId == R.layout.widget_large) {
                    val cal = java.util.Calendar.getInstance()
                    val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                    val (sunrise, sunset) = TamilCalendarHelper.getTodaySunriseSunset(today)

                    val rahuPair = TamilCalendarHelper.calculateRahuKalam(dayOfWeek, sunrise, sunset)
                    val yamaPair = TamilCalendarHelper.calculateYamagandam(dayOfWeek, sunrise, sunset)

                    views.setTextViewText(
                        R.id.widget_rahu_text,
                        "⛔ Rahu: ${TamilCalendarHelper.formatTimeRange(rahuPair.first, rahuPair.second)}"
                    )
                    views.setTextViewText(
                        R.id.widget_yama_text,
                        "⚡ Yama: ${TamilCalendarHelper.formatTimeRange(yamaPair.first, yamaPair.second)}"
                    )
                }

                // Get Reminders from db
                val reminders = ReminderDatabase.getDatabase(context).reminderDao().getActiveReminders()
                val todayReminders = getRemindersForDate(today, reminders)
                val tomorrow = today.plusDays(1)
                val tomorrowReminders = getRemindersForDate(tomorrow, reminders)

                if (layoutId == R.layout.widget_medium) {
                    val reminderText = when {
                        todayReminders.isNotEmpty() -> "🔔 ${todayReminders.first().title}"
                        tomorrowReminders.isNotEmpty() -> "🔔 ${tomorrowReminders.first().title} (Tomorrow)"
                        else -> ""
                    }
                    if (reminderText.isNotEmpty()) {
                        views.setViewVisibility(R.id.widget_reminder, View.VISIBLE)
                        views.setTextViewText(R.id.widget_reminder, reminderText)
                    } else {
                        views.setViewVisibility(R.id.widget_reminder, View.GONE)
                    }
                } else if (layoutId == R.layout.widget_large) {
                    val lines = mutableListOf<String>()
                    for (r in todayReminders.take(2)) {
                        lines.add("🔔 ${r.title} — Today")
                    }
                    for (r in tomorrowReminders.take(2)) {
                        lines.add("🔔 ${r.title} — Tomorrow")
                    }
                    if (lines.isEmpty()) {
                        for (offset in 2..5) {
                            val targetDate = today.plusDays(offset.toLong())
                            val offsetReminders = getRemindersForDate(targetDate, reminders)
                            for (r in offsetReminders.take(1)) {
                                lines.add("🔔 ${r.title} — in $offset days")
                            }
                            if (lines.size >= 2) break
                        }
                    }
                    views.setTextViewText(
                        R.id.widget_reminder,
                        if (lines.isNotEmpty()) lines.joinToString("\n") else "🔔 No upcoming reminders"
                    )
                }

                // Set tap root click intent to open main App
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)

            } catch (e: Exception) {
                Log.e("TamilWidget", "Error updating widgetId: $widgetId", e)
            }
        }
    }

    private fun getRemindersForDate(date: java.time.LocalDate, remindersList: List<Reminder>): List<Reminder> {
        return remindersList.filter { reminder ->
            if (!reminder.isEnabled || reminder.isCompleted) return@filter false
            when (reminder.type) {
                "CUSTOM" -> {
                    val customStr = reminder.customGregorianDate ?: return@filter false
                    val targetDate = try {
                        LocalDate.parse(customStr)
                    } catch (e: Exception) {
                        return@filter false
                    }
                    when (reminder.repeatSetting) {
                        "ONE_TIME" -> targetDate == date
                        "DAILY" -> !date.isBefore(targetDate)
                        "WEEKLY" -> !date.isBefore(targetDate) && java.time.temporal.ChronoUnit.DAYS.between(targetDate, date) % 7 == 0L
                        "MONTHLY" -> !date.isBefore(targetDate) && date.dayOfMonth == targetDate.dayOfMonth
                        "YEARLY" -> !date.isBefore(targetDate) && date.month == targetDate.month && date.dayOfMonth == targetDate.dayOfMonth
                        else -> false
                    }
                }
                "TAMIL" -> {
                    val tMonth = reminder.tamilMonth ?: return@filter false
                    val tDateNum = reminder.tamilDate ?: return@filter false
                    val tDateOfCell = TamilCalendarHelper.getTamilDate(date.year, date.monthValue, date.dayOfMonth)
                    tDateOfCell.tamilMonthIndex == tMonth && tDateOfCell.tamilDay == tDateNum
                }
                "ENGLISH" -> {
                    val targetDay = reminder.englishDayOfMonth ?: return@filter false
                    date.dayOfMonth == targetDay
                }
                "MOON" -> {
                    val moonPhase = reminder.moonPhaseType ?: "POURNAMI"
                    val tDateOfCell = TamilCalendarHelper.getTamilDate(date.year, date.monthValue, date.dayOfMonth)
                    val matchesPhase = when (moonPhase) {
                        "POURNAMI" -> tDateOfCell.isPournami
                        "AMAVASAI" -> tDateOfCell.isAmavasai
                        "EKADASHI" -> tDateOfCell.isEkadashi
                        "PRADOSHAM" -> tDateOfCell.isPradosham
                        else -> false
                    }
                    if (matchesPhase) {
                        if (reminder.tamilMonth != null) {
                            tDateOfCell.tamilMonthIndex == reminder.tamilMonth
                        } else {
                            true
                        }
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    companion object {
        fun scheduleMidnightRefresh(context: Context) {
            try {
                val calendar = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 1)
                }

                val intent = Intent(context, TamilCalendarWidgetProvider::class.java).apply {
                    action = "com.tamilcalendar.WIDGET_REFRESH"
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context, 100, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("TamilWidget", "Scheduled midnight refresh")
            } catch (se: SecurityException) {
                Log.e("TamilWidget", "SecurityException scheduling exact refresh", se)
                // Fallback to non-exact scheduling to prevent crash
                try {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val calendar = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 1)
                    }
                    val intent = Intent(context, TamilCalendarWidgetProvider::class.java).apply {
                        action = "com.tamilcalendar.WIDGET_REFRESH"
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 100, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } catch (e: Exception) {
                    Log.e("TamilWidget", "Error fallback scheduling", e)
                }
            } catch (e: Exception) {
                Log.e("TamilWidget", "Error scheduling refresh", e)
            }
        }

        fun triggerWidgetUpdate(context: Context) {
            try {
                val intent = Intent(context, TamilCalendarWidgetProvider::class.java).apply {
                    action = "com.tamilcalendar.WIDGET_REFRESH"
                }
                context.sendBroadcast(intent)
                Log.d("TamilWidget", "Triggered widget update broadcast")
            } catch (e: Exception) {
                Log.e("TamilWidget", "Error triggering widget update", e)
            }
        }
    }
}
