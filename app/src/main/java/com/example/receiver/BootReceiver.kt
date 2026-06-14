package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.ReminderScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Boot completed action received: $action")
        if (Intent.ACTION_BOOT_COMPLETED == action || "android.intent.action.QUICKBOOT_POWERON" == action) {
            ReminderScheduler.scheduleNextAlarm(context)

            // Re-schedule Morning Briefing on boot if enabled
            val prefs = context.getSharedPreferences("tamil_calendar_prefs", Context.MODE_PRIVATE)
            val briefingEnabled = prefs.getBoolean("morning_briefing_enabled", false)
            if (briefingEnabled) {
                val bTime = prefs.getString("briefing_time", "07:00") ?: "07:00"
                MorningBriefingReceiver.scheduleBriefing(context, bTime)
            }
        }
    }
}
