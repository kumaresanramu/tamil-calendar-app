package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TamilCalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install modern splash screen as requested
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Request Notification Permissions on Android 13 (Tiramisu) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TamilCalendarMainScreen(viewModel = viewModel)
            }
        }
    }
}
