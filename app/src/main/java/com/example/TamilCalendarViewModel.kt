package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TamilCalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ReminderDatabase.getDatabase(application)
    private val dao = db.reminderDao()
    private val sharedPrefs = application.getSharedPreferences("tamil_calendar_settings", Context.MODE_PRIVATE)

    // Flow of all reminders
    val allReminders: StateFlow<List<Reminder>> = dao.getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Calendar state: Selected active month date (e.g. first day of Gregorian month)
    private val _currentActiveMonthDate = MutableStateFlow<LocalDate>(LocalDate.now().withDayOfMonth(1))
    val currentActiveMonthDate: StateFlow<LocalDate> = _currentActiveMonthDate.asStateFlow()

    // Screen navigation state: "CALENDAR", "REMINDERS", "SETTINGS"
    private val _activeTab = MutableStateFlow("CALENDAR")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // Settings States
    private val _defaultReminderTime = MutableStateFlow(sharedPrefs.getString("default_reminder_time", "08:00") ?: "08:00")
    val defaultReminderTime = _defaultReminderTime.asStateFlow()

    private val _useTamilNumerals = MutableStateFlow(sharedPrefs.getBoolean("use_tamil_numerals", true))
    val useTamilNumerals = _useTamilNumerals.asStateFlow()

    private val _showNakshatra = MutableStateFlow(sharedPrefs.getBoolean("show_nakshatra", true))
    val showNakshatra = _showNakshatra.asStateFlow()

    private val _useDarkMode = MutableStateFlow(sharedPrefs.getBoolean("use_dark_mode", false))
    val useDarkMode = _useDarkMode.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    fun nextMonth() {
        _currentActiveMonthDate.value = _currentActiveMonthDate.value.plusMonths(1)
    }

    fun prevMonth() {
        _currentActiveMonthDate.value = _currentActiveMonthDate.value.minusMonths(1)
    }

    fun selectMonth(date: LocalDate) {
        _currentActiveMonthDate.value = date.withDayOfMonth(1)
    }

    // Insert or update a reminder
    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            if (reminder.id == 0) {
                dao.insertReminder(reminder)
            } else {
                dao.updateReminder(reminder)
            }
            // Trigger background alarm rescheduled
            ReminderScheduler.scheduleNextAlarm(getApplication())
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            dao.deleteReminder(reminder)
            ReminderScheduler.scheduleNextAlarm(getApplication())
        }
    }

    fun deleteReminderById(id: Int) {
        viewModelScope.launch {
            dao.deleteReminderById(id)
            ReminderScheduler.scheduleNextAlarm(getApplication())
        }
    }

    fun toggleReminderEnabled(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            dao.updateReminder(updated)
            ReminderScheduler.scheduleNextAlarm(getApplication())
        }
    }

    // Update settings
    fun setDefaultReminderTime(time: String) {
        _defaultReminderTime.value = time
        sharedPrefs.edit().putString("default_reminder_time", time).apply()
        // Reschedule alarms because time format changed
        viewModelScope.launch {
            ReminderScheduler.scheduleNextAlarm(getApplication())
        }
    }

    fun setUseTamilNumerals(value: Boolean) {
        _useTamilNumerals.value = value
        sharedPrefs.edit().putBoolean("use_tamil_numerals", value).apply()
    }

    fun setShowNakshatra(value: Boolean) {
        _showNakshatra.value = value
        sharedPrefs.edit().putBoolean("show_nakshatra", value).apply()
    }

    fun setUseDarkMode(value: Boolean) {
        _useDarkMode.value = value
        sharedPrefs.edit().putBoolean("use_dark_mode", value).apply()
    }
}
