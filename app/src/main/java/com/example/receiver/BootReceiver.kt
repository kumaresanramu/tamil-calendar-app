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
        }
    }
}
