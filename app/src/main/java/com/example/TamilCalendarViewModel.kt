package com.example

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarCellData(
    val date: LocalDate,
    val englishDate: Int,
    val tamilDate: String,
    val tamilMonthName: String,
    val isTamilMonthStart: Boolean,
    val nakshatraName: String,
    val isPournami: Boolean,
    val isAmavasai: Boolean,
    val isFestival: Boolean,
    val festivalName: String,
    val hasReminder: Boolean,
    val isToday: Boolean
)

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

    private fun getSavedInitialDate(): LocalDate {
        val today = LocalDate.now().withDayOfMonth(1)
        val savedStr = sharedPrefs.getString("last_viewed_month", null)
        return if (savedStr != null) {
            try {
                LocalDate.parse(savedStr)
            } catch (e: Exception) {
                today
            }
        } else {
            today
        }
    }

    // Calendar state: Selected active month date (e.g. first day of Gregorian month). Loaded from preferences
    private val _currentActiveMonthDate = MutableStateFlow<LocalDate>(getSavedInitialDate())
    val currentActiveMonthDate: StateFlow<LocalDate> = _currentActiveMonthDate.asStateFlow()

    private fun persistActiveMonth(date: LocalDate) {
        sharedPrefs.edit().putString("last_viewed_month", date.withDayOfMonth(1).toString()).apply()
    }

    // Screen navigation state: "CALENDAR", "REMINDERS", "SETTINGS"
    private val _activeTab = MutableStateFlow("CALENDAR")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // Settings States
    private val _defaultReminderTime = MutableStateFlow(sharedPrefs.getString("default_reminder_time", "08:00") ?: "08:00")
    val defaultReminderTime = _defaultReminderTime.asStateFlow()

    // Default OFF for Nakshatra toggling as requested in Bug Fix 6
    private val _showNakshatra = MutableStateFlow(sharedPrefs.getBoolean("show_nakshatra", false))
    val showNakshatra = _showNakshatra.asStateFlow()

    private val _useDarkMode = MutableStateFlow(sharedPrefs.getBoolean("use_dark_mode", false))
    val useDarkMode = _useDarkMode.asStateFlow()

    // Permissions feedback States - Bug Fix 2AB
    private val _isNotificationPermissionGranted = MutableStateFlow(true)
    val isNotificationPermissionGranted = _isNotificationPermissionGranted.asStateFlow()

    private val _isExactAlarmPermissionGranted = MutableStateFlow(true)
    val isExactAlarmPermissionGranted = _isExactAlarmPermissionGranted.asStateFlow()

    // Reminder Dates reactive set - Bug Fix 4
    private val _reminderDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val reminderDates: StateFlow<Set<LocalDate>> = _reminderDates.asStateFlow()

    // Calendar grid stable cells - Bug Fix 7
    val calendarCells: StateFlow<List<CalendarCellData>> = combine(
        _currentActiveMonthDate,
        allReminders
    ) { activeMonth, remindersList ->
        val cells = mutableListOf<CalendarCellData>()
        val daysInMonth = activeMonth.lengthOfMonth()
        
        for (day in 1..daysInMonth) {
            val date = activeMonth.withDayOfMonth(day)
            val tDate = TamilCalendarHelper.getTamilDate(date.year, date.monthValue, date.dayOfMonth)
            val isToday = (date == LocalDate.now())
            val hasReminder = hasReminderCheck(date, remindersList)
            val isFestival = tDate.festivalName != null && tDate.festivalName != "Pournami / பௌர்ணமி" && tDate.festivalName != "Amavasai / அமாவாசை"

            cells.add(
                CalendarCellData(
                    date = date,
                    englishDate = day,
                    tamilDate = tDate.tamilDay.toString(),
                    tamilMonthName = tDate.tamilMonthNameTamil,
                    isTamilMonthStart = (tDate.tamilDay == 1),
                    nakshatraName = tDate.nakshatra.split(" / ").lastOrNull() ?: tDate.nakshatra,
                    isPournami = tDate.isPournami,
                    isAmavasai = tDate.isAmavasai,
                    isFestival = isFestival,
                    festivalName = tDate.festivalName ?: "",
                    hasReminder = hasReminder,
                    isToday = isToday
                )
            )
        }
        cells
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Automatically maintain the reactive reminderDates set - Bug Fix 4
        viewModelScope.launch {
            allReminders.collect { remindersList ->
                val dates = mutableSetOf<LocalDate>()
                val today = LocalDate.now()
                val start = today.minusMonths(6)
                val end = today.plusMonths(12)
                
                var curr = start
                while (!curr.isAfter(end)) {
                    if (hasReminderCheck(curr, remindersList)) {
                        dates.add(curr)
                    }
                    curr = curr.plusDays(1)
                }
                _reminderDates.value = dates
            }
        }
    }

    fun updatePermissionStates(context: Context) {
        _isNotificationPermissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        _isExactAlarmPermissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    fun nextMonth() {
        _currentActiveMonthDate.value = _currentActiveMonthDate.value.plusMonths(1)
        persistActiveMonth(_currentActiveMonthDate.value)
    }

    fun prevMonth() {
        _currentActiveMonthDate.value = _currentActiveMonthDate.value.minusMonths(1)
        persistActiveMonth(_currentActiveMonthDate.value)
    }

    fun selectMonth(date: LocalDate) {
        _currentActiveMonthDate.value = date.withDayOfMonth(1)
        persistActiveMonth(_currentActiveMonthDate.value)
    }

    fun jumpMonths(offset: Int) {
        _currentActiveMonthDate.value = _currentActiveMonthDate.value.plusMonths(offset.toLong())
        persistActiveMonth(_currentActiveMonthDate.value)
    }

    fun hasReminderCheck(date: LocalDate, remindersList: List<Reminder>): Boolean {
        for (reminder in remindersList) {
            if (!reminder.isEnabled) continue
            when (reminder.type) {
                "CUSTOM" -> {
                    val customStr = reminder.customGregorianDate ?: continue
                    val targetDate = try {
                        LocalDate.parse(customStr)
                    } catch (e: Exception) {
                        continue
                    }
                    if (reminder.repeatSetting == "ONE_TIME") {
                        if (targetDate == date) return true
                    } else if (reminder.repeatSetting == "DAILY") {
                        if (!date.isBefore(targetDate)) return true
                    } else if (reminder.repeatSetting == "WEEKLY") {
                        if (!date.isBefore(targetDate) && java.time.temporal.ChronoUnit.DAYS.between(targetDate, date) % 7 == 0L) return true
                    } else if (reminder.repeatSetting == "MONTHLY") {
                        if (!date.isBefore(targetDate) && date.dayOfMonth == targetDate.dayOfMonth) return true
                    } else if (reminder.repeatSetting == "YEARLY") {
                        if (!date.isBefore(targetDate) && date.month == targetDate.month && date.dayOfMonth == targetDate.dayOfMonth) return true
                    }
                }
                "TAMIL" -> {
                    val tMonth = reminder.tamilMonth ?: continue
                    val tDateNum = reminder.tamilDate ?: continue
                    val tDateOfCell = TamilCalendarHelper.getTamilDate(date.year, date.monthValue, date.dayOfMonth)
                    if (tDateOfCell.tamilMonthIndex == tMonth && tDateOfCell.tamilDay == tDateNum) return true
                }
                "ENGLISH" -> {
                    val targetDay = reminder.englishDayOfMonth ?: continue
                    if (reminder.repeatSetting == "MONTHLY") {
                        if (date.dayOfMonth == targetDay) return true
                    } else {
                        if (date.dayOfMonth == targetDay) return true
                    }
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
                            if (tDateOfCell.tamilMonthIndex == reminder.tamilMonth) return true
                        } else {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    // Insert or update a reminder
    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch {
            if (reminder.id == 0) {
                dao.insertReminder(reminder)
            } else {
                dao.updateReminder(reminder)
            }
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
        viewModelScope.launch {
            ReminderScheduler.scheduleNextAlarm(getApplication())
        }
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
