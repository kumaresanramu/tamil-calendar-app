package com.example

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TamilCalendarViewModel by viewModels()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install modern splash screen as requested
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Dynamic requested permissions check and configuration
        requestAllPermissions()

        enableEdgeToEdge()
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true // true = dark icons on light header (which matches our white calendar background)

        setContent {
            MyApplicationTheme {
                TamilCalendarMainScreen(viewModel = viewModel)
            }
        }
    }

    private fun requestAllPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        
        // Notification permission for Android 13 (Tiramisu) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
        
        // Exact Alarm permission check for Android 12 (S) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val intent = Intent().apply {
                            action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        }
                        startActivity(intent)
                    } catch (ex: Exception) {
                        // Safe catch
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePermissionStates(this)
        // Also fire next alarm reschedule to pick up any exact alarm permission grant
        ReminderScheduler.scheduleNextAlarm(this)
    }
}
