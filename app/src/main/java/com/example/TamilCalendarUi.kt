package com.example

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusChanged
import android.app.DatePickerDialog
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Traditional Theme Color Palettes
val SaffronRed = Color(0xFFC0392B)
val GoldenTurmeric = Color(0xFFF1C40F)
val SoftPournamiYellow = Color(0xFFFEF9E7)
val DarkAmavasaiBg = Color(0xFF34495E)
val LightDayBg = Color(0xFFF8F9FA)
val DeepNavy = Color(0xFF1E272C)

object FestivalSignificanceProvider {
    data class FestivalDescription(
        val nameTamil: String,
        val nameEnglish: String,
        val descriptionTamil: String,
        val descriptionEnglish: String,
        val colorAccent: Color = Color(0xFFC0392B)
    )

    val festivalDescriptions = mapOf(
        "Thai Pongal / தைப்பொங்கல்" to FestivalDescription(
            nameTamil = "தைப்பொங்கல்",
            nameEnglish = "Thai Pongal",
            descriptionTamil = "தைப்பொங்கல் என்பது தமிழர்களால் அறுவடைத் திருநாளாகக் கொண்டாடப்படும் முக்கிய பண்டிகையாகும். இயற்கைக்கும் சூரிய பகவானுக்கும் நன்றி செலுத்தி, புதுப்பானையில் பொங்கலிட்டு மங்களகரமாகக் கொண்டாடப்படுகிறது.",
            descriptionEnglish = "Thai Pongal is a major multi-day harvest festival celebrated by Tamils. Dedicated to the Sun God (Surya) and nature, it marks hope, gratitude, and prosperity as families boil the season's first rice in a clay pot until it overflows.",
            colorAccent = Color(0xFFD35400)
        ),
        "Tamil New Year / தமிழ்ப்புத்தாண்டு" to FestivalDescription(
            nameTamil = "தமிழ்ப்புத்தாண்டு",
            nameEnglish = "Tamil New Year (Puthandu)",
            descriptionTamil = "சித்திரை மாதத்தின் முதல் நாள் தமிழ் புத்தாண்டாகக் கொண்டாடப்படுகிறது. இத்தினத்தில் புதிய திட்டங்களைத் தொடங்குவது, பஞ்சாங்கம் வாசிப்பது மற்றும் வேப்பம்பூ பச்சடி செய்து உண்பது பாரம்பரியமாகும்.",
            descriptionEnglish = "Puthandu marks the first day of the Tamil month Chithirai and the start of the traditional Tamil New Year. Celebrated with colorful kolams, family prayers, visits to temples, and eating 'Mangai Pachadi' symbolizing life's diverse experiences.",
            colorAccent = Color(0xFFE67E22)
        ),
        "Aadi 1 / ஆடி மாதப்பிறப்பு" to FestivalDescription(
            nameTamil = "ஆடி மாதப்பிறப்பு",
            nameEnglish = "Aadi Pandigai (Aadi 1)",
            descriptionTamil = "ஆடி மாதத்தின் முதல் நாள் தட்சிணாயன புண்ணிய காலத்தின் தொடக்கத்தைக் குறிக்கிறது. இது உழவர்களுக்கும் ஆன்மீக வழிபாட்டிற்கும் உகந்த மாதமாகும்.",
            descriptionEnglish = "Aadi 1 marks the beginning of the holy Tamil month of Aadi and the transition to Dakshinayana (southerly movement of the sun). It is celebrated with special prayers, preparation of Aadi Koozh, and traditional sweets.",
            colorAccent = Color(0xFF27AE60)
        ),
        "Aadi Perukku / ஆடிப்பெருக்கு" to FestivalDescription(
            nameTamil = "ஆடிப்பெருக்கு",
            nameEnglish = "Aadi Perukku (Aadi 18)",
            descriptionTamil = "ஆடி மாதம் 18 ஆம் நாள் கொண்டாடப்படும் ஆடிப்பெருக்கு, நதிகளுக்கு நன்றி செலுத்தும் விழாவாகும். குறிப்பாக காவிரி ஆற்றங்கரை பகுதிகளில் மக்கள் ஒன்று கூடி வழிபாடுகள் நடத்துவர்.",
            descriptionEnglish = "Aadi Perukku is a unique water festival celebrated on the 18th day of the Tamil month of Aadi. It honors rivers (especially Kaveri) for nurturing agriculture and life, where families gather near waterways to offer prayers and share picnic meals.",
            colorAccent = Color(0xFF2980B9)
        ),
        "Karthikai Deepam / கார்த்திகை தீபம்" to FestivalDescription(
            nameTamil = "கார்த்திகை தீபம்",
            nameEnglish = "Karthikai Deepam",
            descriptionTamil = "கார்த்திகை மாத பௌர்ணமி நாளில் கொண்டாடப்படும் விளக்குகளின் திருவிழாவாகும். திருவண்ணாமலையில் மஹா தீபம் ஏற்றப்படுவது இதன் சிகர நிகழ்ச்சியாகும். இல்லங்களில் அகல் விளக்குகள் ஏற்றி வழிபடுவர்.",
            descriptionEnglish = "An ancient festival of lights celebrated under the full moon in the Tamil month of Karthikai. Houses and temples are decorated with thousands of clay oil lamps (agal vilakkus) symbolizing the triumph of inner wisdom over darkness.",
            colorAccent = Color(0xFFF1C40F)
        ),
        "Diwali / தீபாவளி" to FestivalDescription(
            nameTamil = "தீபாவளி",
            nameEnglish = "Diwali",
            descriptionTamil = "நாடு முழுவதும் கொண்டாடப்படும் ஒளிகளின் திருவிழாவாகும். நரகாசுரனை வதம் செய்த மகிழ்ச்சியை வெளிப்படுத்தும் விதமாகவும், தீமையிலிருந்து நன்மை வென்றதைக் குறிக்கவும் பட்டாசுகள் வெடித்து புத்தாடை உடுத்தி கொண்டாடப்படுகிறது.",
            descriptionEnglish = "Diwali is the festival of lights celebrating the spiritual victory of light over darkness, good over evil, and knowledge over ignorance. Tamils observe it with morning oil-baths (Ganga Snanam), new apparel, fireworks, and sharing delicious sweets.",
            colorAccent = Color(0xFF9B59B6)
        ),
        "Pournami / பௌர்ணமி" to FestivalDescription(
            nameTamil = "பௌர்ணமி",
            nameEnglish = "Pournami (Full Moon Day)",
            descriptionTamil = "ஒவ்வொரு மாதமும் சந்திரன் முழுமையாக விளங்கும் நாள் பௌர்ணமி ஆகும். இது அன்னை பராசக்தி மற்றும் முருகப்பெருமானை வழிபடவும், கிரிவலம் வரவும் மிகவும் உகந்த நாளாகக் கருதப்படுகிறது.",
            descriptionEnglish = "The auspicious full moon day in the Hindu lunar calendar. It is traditionally dedicated to special worship in Amman and Murugan temples, fasting, and spiritual reflection, such as the Giri Valam (circumambulation) at Arunachala hills.",
            colorAccent = Color(0xFFD35400)
        ),
        "Amavasai / அமாவாசை" to FestivalDescription(
            nameTamil = "அமாவாசை",
            nameEnglish = "Amavasai (New Moon Day)",
            descriptionTamil = "சந்திரன் மறைந்து காணப்படும் நாள் அமாவாசை ஆகும். இது முன்னோர்களுக்கு தர்ப்பணம் கொடுக்கவும், பித்ருக்களின் ஆசியைப் பெறவும் உகந்த நாளாகக் கடைப்பிடிக்கப்படுகிறது.",
            descriptionEnglish = "The new moon day in the Hindu lunar calendar, an exceptionally sacred occasion dedicated to ancestral veneration. Tamils perform Tarpanam (prayers/food offerings) to retrieve the blessings of deceased parents and forefathers.",
            colorAccent = Color(0xFF34495E)
        )
    )
}

@Composable
fun TamilCalendarMainScreen(viewModel: TamilCalendarViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val useDarkMode by viewModel.useDarkMode.collectAsStateWithLifecycle()
    val isExactAlarmGranted by viewModel.isExactAlarmPermissionGranted.collectAsStateWithLifecycle()
    val isNotificationGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showExactPermissionDialog by remember { mutableStateOf(false) }

    // Logic for Exact Alarm Check Dialog - Bug Fix 2A
    LaunchedEffect(isExactAlarmGranted) {
        if (!isExactAlarmGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            showExactPermissionDialog = true
        }
    }

    if (showExactPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showExactPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("To send reminders on time, please allow exact alarms in settings.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExactPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Customize ColorScheme based on Clean Minimalism setting
    val appColors = if (useDarkMode) {
        darkColorScheme(
            primary = Color(0xFF90CAF9),
            secondary = Color(0xFFB39DDB),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF005FB0),
            secondary = Color(0xFF6750A4),
            background = Color(0xFFF3F4F9),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F)
        )
    }

    MaterialTheme(colorScheme = appColors) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(width = 0.5.dp, color = Color(0xFFE2E8F0))
                ) {
                    NavigationBarItem(
                        selected = activeTab == "CALENDAR",
                        onClick = { viewModel.setActiveTab("CALENDAR") },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                        label = { Text("நாட்காட்டி", modifier = Modifier.testTag("tab_calendar_label"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_calendar"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001D35),
                            selectedTextColor = Color(0xFF001D35),
                            indicatorColor = Color(0xFFD3E3FD),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "REMINDERS",
                        onClick = { viewModel.setActiveTab("REMINDERS") },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Reminders") },
                        label = { Text("நினைவூட்டல்", modifier = Modifier.testTag("tab_reminders_label"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_reminders"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001D35),
                            selectedTextColor = Color(0xFF001D35),
                            indicatorColor = Color(0xFFD3E3FD),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "SETTINGS",
                        onClick = { viewModel.setActiveTab("SETTINGS") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("அமைப்புகள்", modifier = Modifier.testTag("tab_settings_label"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_settings"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001D35),
                            selectedTextColor = Color(0xFF001D35),
                            indicatorColor = Color(0xFFD3E3FD),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            },
            contentWindowInsets = WindowInsets(0) // Fix 8 - Avoid bottom bar overlap
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Persistent Banner for Notification permission check - Bug Fix 2B
                if (!isNotificationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD32F2F))
                            .clickable {
                                try {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Notifications blocked. Tap here to enable in Settings.",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    when (activeTab) {
                        "CALENDAR" -> CalendarTabScreen(viewModel)
                        "REMINDERS" -> RemindersTabScreen(viewModel)
                        "SETTINGS" -> SettingsTabScreen(viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarTabScreen(viewModel: TamilCalendarViewModel) {
    val activeMonthDate by viewModel.currentActiveMonthDate.collectAsStateWithLifecycle()
    val showNakshatra by viewModel.showNakshatra.collectAsStateWithLifecycle()
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val reminderDates by viewModel.reminderDates.collectAsStateWithLifecycle()
    val calendarCellsState by viewModel.calendarCells.collectAsStateWithLifecycle()

    var selectedDayDetails by remember { mutableStateOf<TamilDate?>(null) }
    var touchXOffset by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    var showMonthPicker by remember { mutableStateOf(false) }

    // Dynamic month calculations
    val firstDayOfWeek = activeMonthDate.withDayOfMonth(1).dayOfWeek.value % 7 // 0 for Sunday

    // Scan all dates in the month to see which Tamil Months are present
    val tamilMonthsInView = remember(activeMonthDate) {
        val list = mutableSetOf<String>()
        val englishTamilNames = mutableSetOf<String>()
        val daysInMonth = activeMonthDate.lengthOfMonth()
        var searchDate = activeMonthDate.withDayOfMonth(1)
        for (i in 1..daysInMonth) {
            val tDate = TamilCalendarHelper.getTamilDate(searchDate.year, searchDate.monthValue, searchDate.dayOfMonth)
            list.add(tDate.tamilMonthNameTamil)
            englishTamilNames.add(tDate.tamilMonthNameEnglish)
            searchDate = searchDate.plusDays(1)
        }
        val tamilJoined = list.joinToString(" - ")
        val englishJoined = englishTamilNames.joinToString(" / ")
        "$tamilJoined / $englishJoined"
    }

    val midMonthTamilDate = remember(activeMonthDate) {
        TamilCalendarHelper.getTamilDate(activeMonthDate.year, activeMonthDate.monthValue, 15)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(activeMonthDate) {
                    detectDragGestures(
                        onDragEnd = {
                            if (touchXOffset > 150f) {
                                viewModel.prevMonth()
                            } else if (touchXOffset < -150f) {
                                viewModel.nextMonth()
                            }
                            touchXOffset = 0f
                        },
                        onDrag = { _, dragAmount ->
                            touchXOffset += dragAmount.x
                        }
                    )
                }
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App top header - Tappable Custom Month/Year Picker - Bug Fix 10B
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { showMonthPicker = true }
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = midMonthTamilDate.tamilYearName.uppercase() + " YEAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4),
                        letterSpacing = 1.sp,
                        modifier = Modifier.testTag("tamil_year_name")
                    )
                    
                    // Month changing arrow buttons with Long press - Bug Fix 10C
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = { viewModel.prevMonth() },
                                    onLongClick = {
                                        viewModel.jumpMonths(-6)
                                        Toast.makeText(context, "Jumped back 6 months", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                .padding(8.dp)
                                .testTag("prev_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Previous Month",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF1C1B1F)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = { viewModel.nextMonth() },
                                    onLongClick = {
                                        viewModel.jumpMonths(6)
                                        Toast.makeText(context, "Jumped forward 6 months", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                .padding(8.dp)
                                .testTag("next_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                                contentDescription = "Next Month",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF1C1B1F)
                            )
                        }
                    }
                }
                
                // Tappable Header with Month / Year Picker launcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tamilMonthNameEng = midMonthTamilDate.tamilMonthNameEnglish
                    val tamilMonthNameTam = midMonthTamilDate.tamilMonthNameTamil
                    
                    Text(
                        text = "$tamilMonthNameTam / $tamilMonthNameEng ▼ " + activeMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F),
                        modifier = Modifier.testTag("english_month_header")
                    )
                }
                
                Text(
                    text = tamilMonthsInView,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.testTag("tamil_month_header")
                )
            }

            // Mini Month Indicator (Surrounding Month Chips) - Bug Fix 10D
            MiniMonthChipsIndicator(
                activeDate = activeMonthDate,
                onChipClicked = { selectedMonth ->
                    viewModel.selectMonth(selectedMonth)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weekdays Header Row Outside Grid (Header row height: 28dp, size 10sp, color grey)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                for (i in 0..6) {
                    Text(
                        text = days[i],
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar Grid Layout with horizontal sliding navigation transition - Fix 7 & Fix 10
            AnimatedContent(
                targetState = activeMonthDate,
                transitionSpec = {
                    if (targetState.isAfter(initialState)) {
                        slideInHorizontally { width -> width }.togetherWith(slideOutHorizontally { width -> -width })
                    } else {
                        slideInHorizontally { width -> -width }.togetherWith(slideOutHorizontally { width -> width })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { targetMonthDate ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    val daysInMonth = targetMonthDate.lengthOfMonth()
                    val gridFirstDay = targetMonthDate.withDayOfMonth(1).dayOfWeek.value % 7
                    val totalCells = gridFirstDay + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.5.dp)
                        ) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - gridFirstDay + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellData = calendarCellsState.getOrNull(dayNumber - 1)
                                    val cellDate = targetMonthDate.withDayOfMonth(dayNumber)
                                    if (cellData != null) {
                                        key(targetMonthDate, dayNumber) {
                                            CalendarCell(
                                                cellData = cellData,
                                                showNakshatra = showNakshatra,
                                                onClick = {
                                                    val tDate = TamilCalendarHelper.getTamilDate(
                                                        cellDate.year,
                                                        cellDate.monthValue,
                                                        cellDate.dayOfMonth
                                                    )
                                                    selectedDayDetails = tDate
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("day_${dayNumber}")
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.weight(1f).height(58.dp))
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .background(Color.White)
                                            .border(0.5.dp, Color(0xFFE0E0E0))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Legend Row - Bug Fix 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌕 Yellow = Pournami  |  🌑 Dark = Amavasai  |  🔴 Red = Festival",
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Floating today button - Bug Fix 10A
        val today = LocalDate.now()
        val isCurrentMonth = activeMonthDate.monthValue == today.monthValue && activeMonthDate.year == today.year
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = !isCurrentMonth,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.selectMonth(LocalDate.now())
                    },
                    containerColor = Color(0xFF1565C0),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Today",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Day Details Dialog popup
    selectedDayDetails?.let { tDate ->
        DayDetailsDialog(
            tamilDate = tDate,
            showNakshatra = showNakshatra,
            reminders = reminders.filter { reminder ->
                val eventTrigger = ReminderScheduler.calculateNextTrigger(
                    reminder, 
                    LocalDate.of(tDate.englishYear, tDate.englishMonth, tDate.englishDay).atStartOfDay()
                )
                eventTrigger?.toLocalDate() == LocalDate.of(tDate.englishYear, tDate.englishMonth, tDate.englishDay)
            },
            onDismiss = { selectedDayDetails = null }
        )
    }

    // Month/Year Picker Dialog popup - Bug Fix 10B
    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentDate = activeMonthDate,
            onDismiss = { showMonthPicker = false },
            onConfirmDate = { selectedDate ->
                viewModel.selectMonth(selectedDate)
                showMonthPicker = false
            }
        )
    }
}

@Composable
fun MiniMonthChipsIndicator(
    activeDate: LocalDate,
    onChipClicked: (LocalDate) -> Unit
) {
    val surroundingMonths = remember(activeDate) {
        listOf(
            activeDate.minusMonths(2),
            activeDate.minusMonths(1),
            activeDate,
            activeDate.plusMonths(1),
            activeDate.plusMonths(2)
        )
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(surroundingMonths) { date ->
            val isActive = date.monthValue == activeDate.monthValue && date.year == activeDate.year
            val label = date.format(DateTimeFormatter.ofPattern("MMM"))
            val dotPrefix = if (isActive) "● " else ""
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isActive) Color(0xFF1565C0) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isActive) Color(0xFF1565C0) else Color(0xFFBDBDBD),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onChipClicked(date) }
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$dotPrefix$label",
                    fontSize = 11.sp,
                    color = if (isActive) Color.White else Color(0xFF757575),
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

fun abbreviateNakshatra(name: String): String {
    return when (name.lowercase()) {
        "ashwini" -> "Ashwi"
        "bharani" -> "Bhar"
        "krittika" -> "Krit"
        "rohini" -> "Rohin"
        "mrigashira" -> "Mriga"
        "ardra" -> "Ardra"
        "punarvasu" -> "Punav"
        "pushya" -> "Push"
        "ashlesha" -> "Ashl"
        "magha" -> "Magha"
        "purva phalguni" -> "PPhag"
        "uttara phalguni" -> "UPhag"
        "hasta" -> "Hasta"
        "chitra" -> "Chit"
        "swati" -> "Swati"
        "vishakha" -> "Visha"
        "anuradha" -> "Anura"
        "jyeshtha" -> "Jyesh"
        "mula" -> "Mula"
        "purva ashadha" -> "PAsha"
        "uttara ashadha" -> "UAsha"
        "shravana" -> "Shrav"
        "dhanishtha" -> "Dhan"
        "shatabhisha" -> "Shata"
        "purva bhadrapada" -> "PBhad"
        "uttara bhadrapada" -> "UBhad"
        "revati" -> "Revat"
        else -> name.take(5)
    }
}

@Composable
fun CalendarCell(
    cellData: CalendarCellData,
    showNakshatra: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colors of Pournami & Amavasai & Festival - Bug Fix 3
    val cellBackgroundColor = when {
        cellData.isPournami -> Color(0xFFFFF9C4) // Soft golden yellow
        cellData.isAmavasai -> Color(0xFF1A237E) // Deep dark blue
        cellData.isFestival -> Color(0xFFFFEBEE) // Very light pink
        else -> Color.White
    }

    val englishDateColor = when {
        cellData.isToday -> Color.White
        cellData.isPournami -> Color(0xFFE65100) // Deep orange
        cellData.isAmavasai -> Color(0xFFFFFFFF) // White
        cellData.isFestival -> Color(0xFFD32F2F) // Deep Red
        else -> Color(0xFF1C1B1F)
    }

    val tamilDateColor = when {
        cellData.isToday -> Color.White
        cellData.isAmavasai -> Color(0xFFB0BEC5) // Light grey
        else -> Color(0xFF6D4C41) // Warm brown
    }

    val cellBorderColor = when {
        cellData.isPournami -> Color(0xFFFFB300) // Amber
        cellData.isAmavasai -> Color(0xFF3949AB) // Indigo
        else -> Color(0xFFE0E0E0)
    }

    val cellBorderWidth = if (cellData.isPournami || cellData.isAmavasai) 1.5.dp else 0.5.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(cellBackgroundColor)
            .border(cellBorderWidth, cellBorderColor)
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        // LAYER 1 — TOP LEFT: English date
        if (cellData.isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 1.dp, top = 1.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1565C0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cellData.englishDate.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = cellData.englishDate.toString(),
                fontSize = if (showNakshatra && cellData.nakshatraName.isNotEmpty()) 13.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                color = englishDateColor,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 1.dp)
            )
        }

        // LAYER 2 & 4 — TOP RIGHT: Moon icon or Festival dot
        if (cellData.isPournami || cellData.isAmavasai) {
            Text(
                text = if (cellData.isPournami) "🌕" else "🌑",
                fontSize = if (showNakshatra && cellData.nakshatraName.isNotEmpty()) 8.sp else 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 1.dp, top = 1.dp)
            )
        }

        // LAYER 3 — CENTER: Nakshatra name, abbreviated to max 5 chars
        if (showNakshatra && cellData.nakshatraName.isNotEmpty()) {
            Text(
                text = abbreviateNakshatra(cellData.nakshatraName),
                fontSize = 6.sp,
                color = Color(0xFF7B1FA2),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp)
            )
        }

        // LAYER 4 — BOTTOM LEFT: Reminder bell icon
        if (cellData.hasReminder) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF1565C0),
                modifier = Modifier
                    .size(if (showNakshatra && cellData.nakshatraName.isNotEmpty()) 8.dp else 10.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, bottom = 2.dp)
            )
        }

        // LAYER 5 — BOTTOM RIGHT: Tamil date (Arabic number)
        Text(
            text = cellData.tamilDate,
            fontSize = if (showNakshatra && cellData.nakshatraName.isNotEmpty()) 8.sp else 10.sp,
            color = tamilDateColor,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 2.dp, bottom = 1.dp)
        )
    }
}

@Composable
fun DayDetailsDialog(
    tamilDate: TamilDate,
    showNakshatra: Boolean,
    reminders: List<Reminder>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("day_details_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "நாள் விபரம் / Day Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_details_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                if (tamilDate.festivalName != null) {
                    val festivalDesc = FestivalSignificanceProvider.festivalDescriptions[tamilDate.festivalName]
                    val accentColor = festivalDesc?.colorAccent ?: SaffronRed
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("festival_significance_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star, 
                                    contentDescription = "Festival Spotlight", 
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = festivalDesc?.nameEnglish ?: tamilDate.festivalName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.testTag("festival_title_english")
                                )
                            }
                            
                            if (festivalDesc != null) {
                                Text(
                                    text = festivalDesc.nameTamil,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor.copy(alpha = 0.85f),
                                    modifier = Modifier.testTag("festival_title_tamil")
                                )
                                
                                Text(
                                    text = festivalDesc.descriptionTamil,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.testTag("festival_desc_tamil")
                                )
                                
                                Text(
                                    text = festivalDesc.descriptionEnglish,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.testTag("festival_desc_english")
                                )
                            }
                        }
                    }
                }

                InfoRow(label = "English Date", value = "${tamilDate.englishDay}-${tamilDate.englishMonth}-${tamilDate.englishYear}")
                InfoRow(label = "Tamil Month", value = "${tamilDate.tamilMonthNameTamil} / ${tamilDate.tamilMonthNameEnglish}")
                InfoRow(label = "Tamil Date", value = "${tamilDate.tamilDay}") // No numerals
                
                InfoRow(label = "Tamil Year", value = tamilDate.tamilYearName)

                val paksha = if (tamilDate.tithi <= 15) "வளர்பிறை / Shukla Paksha (Waxing)" else "தேய்பிறை / Krishna Paksha (Waning)"
                val tithiName = when (tamilDate.tithi) {
                    1, 16 -> "பிரதமை / Prathama"
                    2, 17 -> "துவிதியை / Dwitiya"
                    3, 18 -> "திருதியை / Tritiya"
                    4, 19 -> "சதுர்த்தி / Chaturthi"
                    5, 20 -> "பஞ்சமி / Panchami"
                    6, 21 -> "சஷ்டி / Shashti"
                    7, 22 -> "சப்தமி / Saptami"
                    8, 23 -> "அஷ்டமி / Ashtami"
                    9, 24 -> "நவமி / Navami"
                    10, 25 -> "தசமி / Dashami"
                    11, 26 -> "ஏகாதசி / Ekadashi"
                    12, 27 -> "துவாதசி / Dwadashi"
                    13, 28 -> "திரயோதசி / Trayodashi"
                    14, 29 -> "சதுர்தசி / Chaturdashi"
                    15 -> "பௌர்ணமி / Pournami (Full Moon)"
                    30 -> "அமாவாசை / Amavasai (New Moon)"
                    else -> "திதி / Tithi"
                }
                
                InfoRow(label = "Tithi (திதி)", value = tithiName)
                InfoRow(label = "Paksha (பக்ஷம்)", value = paksha)

                if (showNakshatra) {
                    InfoRow(label = "Nakshatra (நட்சத்திரம்)", value = tamilDate.nakshatra)
                }

                if (reminders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Saved Reminders for Today:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (r in reminders) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Alert",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${r.title} @ ${r.reminderTime}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(asymmetricPaddingForEndValue(label))
        )
    }
}

private fun asymmetricPaddingForEndValue(label: String) = if (label.length > 15) PaddingValues(start = 24.dp) else PaddingValues(start = 8.dp)

// Month and Year Selector Dialog - Bug Fix 10B
@Composable
fun MonthYearPickerDialog(
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirmDate: (LocalDate) -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentDate.year) }
    var selectedMonthIndex by remember { mutableStateOf(currentDate.monthValue) } // 1-12

    val englishMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val tamilMonthsUnderneath = listOf(
        "மார்கழி/தை", "தை/மாசி", "மாசி/பங்குனி", "பங்குனி/சித்திரை", "சித்திரை/வைகாசி", "வைகாசி/ஆனி",
        "ஆனி/ஆடி", "ஆடி/ஆவணி", "ஆவணி/புரட்டாசி", "புரட்டாசி/ஐப்பசி", "ஐப்பசி/கார்த்திகை", "கார்த்திகை/மார்கழி"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Month & Year",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (selectedYear > 2020) selectedYear-- },
                        enabled = selectedYear > 2020
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev Year")
                    }

                    Text(
                        text = selectedYear.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { if (selectedYear < 2035) selectedYear++ },
                        enabled = selectedYear < 2035
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Year")
                    }
                }

                // Month Grid (3 columns x 4 rows)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rows = 4
                    val cols = 3
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until cols) {
                                val monthIdx = r * 3 + c + 1
                                val isSelected = monthIdx == selectedMonthIndex
                                val isTodayMonth = monthIdx == LocalDate.now().monthValue && selectedYear == LocalDate.now().year
                                
                                val modifierWithBorder = if (isTodayMonth && !isSelected) {
                                    Modifier.border(1.5.dp, Color(0xFF1565C0), RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF1565C0) else Color.Transparent)
                                        .then(modifierWithBorder)
                                        .clickable { selectedMonthIndex = monthIdx }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = englishMonths[monthIdx - 1],
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = tamilMonthsUnderneath[monthIdx - 1],
                                            fontSize = 7.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val pickedDate = LocalDate.of(selectedYear, selectedMonthIndex, 1)
                            onConfirmDate(pickedDate)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text("Go", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersTabScreen(viewModel: TamilCalendarViewModel) {
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var completedExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val defaultReminderTime by viewModel.defaultReminderTime.collectAsStateWithLifecycle()

    val customCount = reminders.count { !it.isSystemReminder && !it.isCompleted }

    val nowMs = System.currentTimeMillis()
    val deletesInHours = { compAt: Long ->
        val diffMs = (compAt + 24 * 60 * 60 * 1000L) - nowMs
        val hours = (diffMs / (60 * 60 * 1000L)).coerceAtLeast(0L)
        if (hours > 0) "$hours hrs" else "1 hr"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { 
                        if (customCount < 15) {
                            showAddDialog = true
                        } else {
                            showLimitDialog = true
                        }
                    },
                    containerColor = if (customCount < 15) Color(0xFFFF6B00) else Color(0xFFBDBDBD),
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_reminder_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "நினைவூட்டல்கள் / Reminders",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // My Reminders limit counter badge/label
                Text(
                    text = "My Reminders: $customCount / 15",
                    fontSize = 12.sp,
                    color = when {
                        customCount >= 15 -> Color(0xFFD32F2F) // red
                        customCount >= 12 -> Color(0xFFE65100) // orange
                        else -> Color(0xFF757575)        // grey
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val systemReminders = reminders.filter { it.isSystemReminder }
                val customReminders = reminders.filter { !it.isSystemReminder && !it.isCompleted }
                val completedReminders = reminders.filter { !it.isSystemReminder && it.isCompleted }

                if (systemReminders.isEmpty() && customReminders.isEmpty() && completedReminders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "நினைவூட்டல்கள் எதுவும் இல்லை.\nஉруவாக்க '+' பொத்தானை அழுத்தவும்.\nNo Reminders Scheduled.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (systemReminders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "System Reminders",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(systemReminders, key = { "sys_${it.id}" }) { r ->
                                ReminderItemRow(
                                    reminder = r,
                                    onToggle = { viewModel.toggleReminderEnabled(r) },
                                    onDelete = { /* No-op for system */ },
                                    onEdit = { editingReminder = r }
                                )
                            }
                        }

                        if (customReminders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "My Reminders",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                            items(customReminders, key = { "custom_${it.id}" }) { r ->
                                ReminderItemRow(
                                    reminder = r,
                                    onToggle = { viewModel.toggleReminderEnabled(r) },
                                    onDelete = { viewModel.deleteReminder(r) },
                                    onEdit = { editingReminder = r }
                                )
                            }
                        }

                        if (completedReminders.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { completedExpanded = !completedExpanded }
                                        .padding(top = 16.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recently Completed (auto-deletes in 24hrs)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Icon(
                                        imageVector = if (completedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand/Collapse Completed",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (completedExpanded) {
                                items(completedReminders, key = { "comp_${it.id}" }) { r ->
                                    CompletedReminderItemRow(
                                        reminder = r,
                                        deletesIn = deletesInHours(r.completedAt ?: System.currentTimeMillis()),
                                        onDelete = { viewModel.deleteReminder(r) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLimitDialog) {
            AlertDialog(
                onDismissRequest = { showLimitDialog = false },
                title = { Text("Reminder Limit Reached") },
                text = { 
                    Text(
                        "You can add maximum 15 reminders. " +
                        "Please delete an existing reminder " +
                        "to add a new one."
                    ) 
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showLimitDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        if (showAddDialog) {
            AddEditReminderDialog(
                reminder = null,
                onDismiss = { showAddDialog = false },
                onSave = { reminder ->
                    viewModel.saveReminder(reminder)
                    showAddDialog = false
                    Toast.makeText(context, "Reminder saved successfully! 🔔", Toast.LENGTH_SHORT).show()
                },
                defaultReminderTime = defaultReminderTime
            )
        }

        editingReminder?.let { r ->
            AddEditReminderDialog(
                reminder = r,
                onDismiss = { editingReminder = null },
                onSave = { reminder ->
                    viewModel.saveReminder(reminder)
                    editingReminder = null
                    Toast.makeText(context, "Reminder saved successfully! 🔔", Toast.LENGTH_SHORT).show()
                },
                defaultReminderTime = defaultReminderTime
            )
        }
    }
}

@Composable
fun ReminderItemRow(
    reminder: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val containerColor = if (reminder.isSystemReminder) {
        if (reminder.isEnabled) Color(0xFFE8EAF6) else Color(0xFFF5F5F5)
    } else {
        if (reminder.isEnabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFECEFF1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("reminder_item_${reminder.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (reminder.isSystemReminder) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "System Default",
                            tint = Color(0xFF7986CB),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (reminder.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val subLabel = when (reminder.type) {
                    "TAMIL" -> "Tamil: Month ${reminder.tamilMonth}, Day ${reminder.tamilDate}"
                    "ENGLISH" -> "English: Day ${reminder.englishDayOfMonth ?: "Every month"}"
                    "MOON" -> "Moon: ${reminder.moonPhaseType} in Tamil Month: ${reminder.tamilMonth ?: "All"}"
                    else -> "Date: ${reminder.customGregorianDate}"
                }
                
                Text(
                    text = "$subLabel @ ${reminder.reminderTime}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("toggle_switch_${reminder.id}")
                )
                if (!reminder.isSystemReminder) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_btn_${reminder.id}")) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// Reusable character counter composable - Bug Fix 9
@Composable
fun CharacterCounter(current: Int, max: Int) {
    val color = when {
        current >= max -> Color(0xFFD32F2F)
        current >= (max * 0.8).toInt() -> Color(0xFFE65100)
        else -> Color(0xFF9E9E9E)
    }
    Text(
        text = "$current / $max",
        fontSize = 12.sp,
        color = color,
        modifier = Modifier.fillMaxWidth().testTag("char_counter_$max"),
        textAlign = TextAlign.End
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int, T) -> Unit,
    modifier: Modifier = Modifier,
    widthDp: androidx.compose.ui.unit.Dp = 75.dp,
    labelProvider: (T) -> String = { it.toString() }
) {
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    LaunchedEffect(selectedIndex) {
        if (!lazyListState.isScrollInProgress) {
            lazyListState.scrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val centerIndex = lazyListState.firstVisibleItemIndex
            if (centerIndex in items.indices && centerIndex != selectedIndex) {
                onItemSelected(centerIndex, items[centerIndex])
            }
        }
    }

    Box(
        modifier = modifier
            .height(115.dp)
            .width(widthDp)
            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Selection highlight center bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color(0xFFFFEB3B).copy(alpha = 0.15f))
                .border(width = 1.dp, color = Color(0xFFFFB300))
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 39.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex
                Text(
                    text = labelProvider(item),
                    fontSize = if (isSelected) 14.sp else 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onItemSelected(index, item) },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    infiniteScroll: Boolean = false
) {
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)

    LaunchedEffect(selectedIndex) {
        if (!lazyListState.isScrollInProgress) {
            lazyListState.scrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val centerIndex = lazyListState.firstVisibleItemIndex
            if (centerIndex in items.indices && centerIndex != selectedIndex) {
                onSelected(centerIndex)
            }
        }
    }

    Box(
        modifier = modifier
            .height(115.dp)
            .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF475569), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color(0xFFFFEB3B).copy(alpha = 0.15f))
                .border(width = 1.dp, color = Color(0xFFFFB300))
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 39.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex
                Text(
                    text = item,
                    fontSize = if (isSelected) 16.sp else 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { onSelected(index) },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CompletedReminderItemRow(
    reminder: Reminder,
    deletesIn: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("completed_reminder_item_${reminder.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Gray.copy(alpha = 0.5f),
                        style = TextStyle(textDecoration = TextDecoration.LineThrough),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Completed ✓",
                            color = Color(0xFF2E7D32),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Deletes in $deletesIn",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("completed_delete_btn_${reminder.id}")) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditReminderDialog(
    reminder: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    defaultReminderTime: String = "08:00"
) {
    val isSystem = reminder?.isSystemReminder == true

    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }
    var type by remember { mutableStateOf(reminder?.type ?: "CUSTOM") } // "TAMIL", "ENGLISH", "MOON", "CUSTOM"
    
    // Tamil attributes
    var tamilMonth by remember { mutableStateOf(reminder?.tamilMonth ?: 1) }
    var tamilDateVal by remember { mutableStateOf(reminder?.tamilDate ?: 1) }

    // English attributes
    var englishDayOfMonth by remember { mutableStateOf(reminder?.englishDayOfMonth ?: 1) }
    var englishMonthOfReminder by remember { mutableStateOf(1) } // Default month
    var selectedYear by remember { mutableStateOf<Int?>(2026) } // Default year

    // Moon attributes
    var moonPhaseType by remember { mutableStateOf(reminder?.moonPhaseType ?: "POURNAMI") }
    var moonTamilMonthFilter by remember { mutableStateOf<Int?>(reminder?.tamilMonth) } // Optional month filter

    // Custom attributes
    var customGregorianDate by remember { mutableStateOf(reminder?.customGregorianDate ?: LocalDate.now().toString()) }

    var reminderTime by remember { mutableStateOf(reminder?.reminderTime ?: defaultReminderTime) }
    var repeatSetting by remember { mutableStateOf(reminder?.repeatSetting ?: "ONE_TIME") }
    var remindBeforeDays by remember { mutableStateOf(reminder?.remindBeforeDays ?: 0) }

    // Hour, Minute, AM/PM states
    val initialTimeParts = reminderTime.split(":")
    val initialHour24 = initialTimeParts.getOrNull(0)?.toIntOrNull() ?: 8
    val initialMinute = initialTimeParts.getOrNull(1)?.toIntOrNull() ?: 0
    
    var selectedHour12 by remember {
        mutableStateOf(
            if (initialHour24 == 0) 12 else if (initialHour24 > 12) initialHour24 - 12 else initialHour24
        )
    }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var selectedAmPm by remember {
        mutableStateOf(
            if (initialHour24 >= 12) "PM" else "AM"
        )
    }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Error states
    var titleErrorText by remember { mutableStateOf<String?>(null) }
    val titleFocusRequester = remember { FocusRequester() }

    // Validation state check for Save button
    val isTitleValid = isSystem || title.trim().length >= 3

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Sync hour/min back to reminderTime whenever picker selections change
    LaunchedEffect(selectedHour12, selectedMinute, selectedAmPm) {
        val hour24 = when {
            selectedAmPm == "AM" && selectedHour12 == 12 -> 0
            selectedAmPm == "AM" -> selectedHour12
            selectedAmPm == "PM" && selectedHour12 == 12 -> 12
            else -> selectedHour12 + 12
        }
        reminderTime = String.format("%02d:%02d", hour24, selectedMinute)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .imePadding()
                .testTag("add_edit_reminder_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Header Title Label and separator
                item {
                    Column {
                        Text(
                            text = if (reminder == null) "புதிய நினைவூட்டல் / Create Reminder" else "தொகு / Edit Reminder",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFFE2E8F0))
                    }
                }

                // 2. Four-button Reminder Type Toolbar
                item {
                    if (isSystem) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "System Default Holiday (${type})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5)
                                )
                            }
                        }
                    } else {
                        Column {
                            Text("நினைவூட்டல் வகை / Reminder Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("CUSTOM" to "Custom", "TAMIL" to "Tamil", "ENGLISH" to "English", "MOON" to "Moon").forEach { (key, label) ->
                                    Button(
                                        onClick = { type = key },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (type == key) SaffronRed else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (type == key) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("type_tab_${key.lowercase()}"),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                                    ) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Title Input Field
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("நினைவூட்டல் தலைப்பு / Title Name:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { newVal ->
                                if (newVal.length <= 50) {
                                    title = newVal
                                    titleErrorText = if (newVal.trim().length < 3) "Title must be at least 3 characters" else null
                                }
                            },
                            enabled = !isSystem,
                            label = { Text("Title") },
                            placeholder = { Text("e.g. Rent, Fasting, Pongal...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .focusRequester(titleFocusRequester)
                                .bringIntoViewRequester(bringIntoViewRequester)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        coroutineScope.launch {
                                            bringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                }
                                .testTag("dialog_title_input"),
                            isError = titleErrorText != null,
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (titleErrorText != null) {
                                Text(
                                    text = titleErrorText!!,
                                    fontSize = 11.sp,
                                    color = Color.Red,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            CharacterCounter(current = title.length, max = 50)
                        }
                    }
                }

                // 4. Description Field
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("விளக்கம் / Description (Opotional):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { newVal -> if (newVal.length <= 200) description = newVal },
                            enabled = !isSystem,
                            label = { Text("Description") },
                            placeholder = { Text("Add extra notes here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = 120.dp),
                            minLines = 2,
                            maxLines = 4,
                            singleLine = false
                        )
                        CharacterCounter(current = description.length, max = 200)
                    }
                }

                // 5. Dynamic Date Selection Form Elements
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("தேதி தேர்வு / Date Selection:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        
                        if (isSystem) {
                            // Read-only metadata summary card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F9)),
                                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFCFD8DC), shape = RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val textSummary = when (type) {
                                        "TAMIL" -> "Occurs yearly in Tamil Month ${reminder?.tamilMonth} on Day ${reminder?.tamilDate}"
                                        "MOON" -> "Occurs on ${reminder?.moonPhaseType} in Tamil Month: ${reminder?.tamilMonth ?: "All"}"
                                        else -> "Locked Metadata"
                                    }
                                    Text(textSummary, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                }
                            }
                        } else {
                            // Render based on custom types
                            when (type) {
                                "ENGLISH" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // 2 Scroll Pickers: Day and Month
                                        Text("Select Day and Month of Year:", fontSize = 11.sp, color = Color.Gray)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Day (1-31)", fontSize = 10.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                WheelPicker(
                                                    items = (1..31).toList(),
                                                    selectedIndex = (1..31).indexOf(englishDayOfMonth).coerceAtLeast(0),
                                                    onItemSelected = { _, d -> englishDayOfMonth = d }
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Month", fontSize = 10.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                WheelPicker(
                                                    items = (1..12).toList(),
                                                    selectedIndex = (1..12).indexOf(englishMonthOfReminder).coerceAtLeast(0),
                                                    onItemSelected = { _, m -> englishMonthOfReminder = m },
                                                    labelProvider = { java.time.Month.of(it).name.take(3).lowercase().replaceFirstChar { it.uppercase() } }
                                                )
                                            }
                                        }

                                        // Year selections chips: None, 2026, 2027
                                        Text("Year Target:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf("None", "2026", "2027").forEach { yStr ->
                                                val isSelected = when (yStr) {
                                                    "None" -> selectedYear == null
                                                    else -> selectedYear == yStr.toInt()
                                                }
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        selectedYear = if (yStr == "None") null else yStr.toInt()
                                                    },
                                                    label = { Text(yStr) },
                                                    modifier = Modifier.testTag("year_chip_$yStr")
                                                )
                                            }
                                        }

                                        // Repeat toggle choices
                                        Text("Occurrence Mode:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf("MONTHLY" to "Same day every month", "ONE_TIME" to "One-time specific date").forEach { (mode, label) ->
                                                val isSelected = repeatSetting == mode
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = {
                                                        repeatSetting = mode
                                                        if (mode == "MONTHLY") {
                                                            selectedYear = null
                                                        } else {
                                                            selectedYear = 2026
                                                        }
                                                    },
                                                    label = { Text(label, fontSize = 11.sp) },
                                                    modifier = Modifier.testTag("repeat_chip_$mode")
                                                )
                                            }
                                        }

                                        // Custom formatted trigger text helper
                                        val customStrDate = selectedYear?.let { y ->
                                            try {
                                                LocalDate.of(y, englishMonthOfReminder, englishDayOfMonth).toString()
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        customGregorianDate = customStrDate ?: LocalDate.now().toString()
                                    }
                                }

                                "TAMIL" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // 2 Scroll Pickers: Tamil Month and Tamil Day
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Tamil Month Scroll Picker (Plain text)
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.3f)) {
                                                Text("Tamil Month", fontSize = 10.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                WheelPicker(
                                                    items = (1..12).toList(),
                                                    selectedIndex = (1..12).indexOf(tamilMonth).coerceAtLeast(0),
                                                    widthDp = 150.dp,
                                                    onItemSelected = { _, m -> tamilMonth = m },
                                                    labelProvider = { TamilCalendarHelper.tamilMonthsEnglish[it - 1] }
                                                )
                                            }

                                            // Tamil Date Scroll Picker (Words)
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                                Text("Tamil Day", fontSize = 10.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                WheelPicker(
                                                    items = (1..32).toList(),
                                                    selectedIndex = (1..32).indexOf(tamilDateVal).coerceAtLeast(0),
                                                    widthDp = 100.dp,
                                                    onItemSelected = { _, d -> tamilDateVal = d },
                                                    labelProvider = { "Day $it" }
                                                )
                                            }
                                        }

                                        // Auto-calculated English conversion date Display card
                                        val calculatedDate = TamilCalendarHelper.findGregorianDate(2026, tamilMonth, tamilDateVal)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, Color(0xFFFFD54F), shape = RoundedCornerShape(8.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Auto-calculated English Date (for 2026):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                if (calculatedDate != null) {
                                                    Text(
                                                        text = calculatedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF37474F)
                                                    )
                                                } else {
                                                    Text("Loading correct date mapping...", fontSize = 13.sp, color = Color.Gray)
                                                }
                                            }
                                        }

                                        // Remind Days Before selection
                                        Text("Remind Days Before:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(0 to "Same Day", 1 to "1 Day Before", 2 to "2 Days Before").forEach { (d, dLabel) ->
                                                FilterChip(
                                                    selected = remindBeforeDays == d,
                                                    onClick = { remindBeforeDays = d },
                                                    label = { Text(dLabel, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        repeatSetting = "YEARLY"
                                    }
                                }

                                "MOON" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // 4 selection Card buttons for Moon Phasing
                                        Text("Select Moon Phase Event:", fontSize = 11.sp, color = Color.Gray)
                                        val moonOptions = listOf(
                                            "POURNAMI" to "🌕 Pournami",
                                            "AMAVASAI" to "🌑 Amavasai",
                                            "EKADASHI" to "🕉️ Ekadashi",
                                            "PRADOSHAM" to "🪔 Pradosham"
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            moonOptions.chunked(2).forEach { chunk ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    chunk.forEach { (optionKey, optionLabel) ->
                                                        val isSelected = moonPhaseType == optionKey
                                                        Card(
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = if (isSelected) Color(0xFFFFF9C4) else Color(0xFFF1F5F9)
                                                            ),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clickable { moonPhaseType = optionKey }
                                                                .border(
                                                                    width = if (isSelected) 2.dp else 1.dp,
                                                                    color = if (isSelected) Color(0xFFFFB300) else Color(0xFFE2E8F0),
                                                                    shape = RoundedCornerShape(8.dp)
                                                                )
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.padding(12.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = optionLabel,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isSelected) Color(0xFFE65100) else Color(0xFF334155)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Optional Tamil Month Filter
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Optional Tamil Month Filter:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        var filterExpanded by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedButton(
                                                onClick = { filterExpanded = true },
                                                modifier = Modifier.fillMaxWidth().testTag("tamil_month_filter_drop")
                                            ) {
                                                Text(
                                                    text = if (moonTamilMonthFilter == null) "None / All Months"
                                                           else TamilCalendarHelper.tamilMonthsEnglish[moonTamilMonthFilter!! - 1]
                                                )
                                            }
                                            DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                                                DropdownMenuItem(
                                                    text = { Text("None / All Months") },
                                                    onClick = {
                                                        moonTamilMonthFilter = null
                                                        filterExpanded = false
                                                    }
                                                )
                                                for (i in 1..12) {
                                                    DropdownMenuItem(
                                                        text = { Text(TamilCalendarHelper.tamilMonthsEnglish[i - 1]) },
                                                        onClick = {
                                                            moonTamilMonthFilter = i
                                                            filterExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Remind days before selection
                                        Text("Remind Days Before:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf(0 to "Same Day", 1 to "1 Day Before", 2 to "2 Days Before").forEach { (d, dLabel) ->
                                                FilterChip(
                                                    selected = remindBeforeDays == d,
                                                    onClick = { remindBeforeDays = d },
                                                    label = { Text(dLabel, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                        repeatSetting = "YEARLY"
                                    }
                                }

                                "CUSTOM" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Tap to select card
                                        Text("Select English Gregorian Date:", fontSize = 11.sp, color = Color.Gray)
                                        val dateObj = try {
                                            LocalDate.parse(customGregorianDate)
                                        } catch (e: Exception) {
                                            LocalDate.now()
                                        }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    DatePickerDialog(
                                                        context,
                                                        { _, year, month, dayOfMonth ->
                                                            val selDate = LocalDate.of(year, month + 1, dayOfMonth)
                                                            customGregorianDate = selDate.toString()
                                                        },
                                                        dateObj.year,
                                                        dateObj.monthValue - 1,
                                                        dateObj.dayOfMonth
                                                    ).show()
                                                }
                                                .border(1.dp, Color(0xFFCBD5E0), shape = RoundedCornerShape(8.dp)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("Selected Gregorian Date:", fontSize = 10.sp, color = Color.Gray)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(dateObj.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Icon(Icons.Default.DateRange, contentDescription = "Choose Date", tint = SaffronRed)
                                            }
                                        }

                                        // Prominently demonstrate conversion corresponding Tamil Date info card
                                        val targetTamilDate = TamilCalendarHelper.getTamilDate(dateObj.year, dateObj.monthValue, dateObj.dayOfMonth)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFA5D6A7), shape = RoundedCornerShape(8.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Corresponding Tamil Date Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Month: ${targetTamilDate.tamilMonthNameEnglish} / ${targetTamilDate.tamilMonthNameTamil}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF37474F)
                                                )
                                                Text(
                                                    text = "Day in Month: Day ${targetTamilDate.tamilDay}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF37474F)
                                                )
                                                Text(
                                                    text = "Tamil Year Name: ${targetTamilDate.tamilYearName.uppercase()} Year",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        // Repeat frequency mode selections
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("மீள்செயல் / Repeat Frequency:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        var repeatExp by remember { mutableStateOf(false) }
                                        val rfList = listOf("ONE_TIME" to "Once", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "YEARLY" to "Yearly")
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedButton(onClick = { repeatExp = true }, modifier = Modifier.fillMaxWidth()) {
                                                Text(rfList.find { it.first == repeatSetting }?.second ?: repeatSetting)
                                            }
                                            DropdownMenu(expanded = repeatExp, onDismissRequest = { repeatExp = false }) {
                                                rfList.forEach { (mode, label) ->
                                                    DropdownMenuItem(
                                                        text = { Text(label) },
                                                        onClick = {
                                                            repeatSetting = mode
                                                            repeatExp = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        remindBeforeDays = 0
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Time Selection section (Hour, Minute, AM/PM 3-Column drum-roll selector)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("மீள்செயல் நேரம் / Reminder Time (Drum-roll Picker):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour Picker: 1 to 12
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hour", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                WheelPicker(
                                    items = (1..12).toList(),
                                    selectedIndex = (1..12).indexOf(selectedHour12).coerceAtLeast(0),
                                    onItemSelected = { _, h -> selectedHour12 = h }
                                )
                            }

                            // Colon separator
                            Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                            // Minute Picker: 00 to 59
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Minute", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                WheelPicker(
                                    items = (0..59).toList(),
                                    selectedIndex = (0..59).indexOf(selectedMinute).coerceAtLeast(0),
                                    onItemSelected = { _, m -> selectedMinute = m },
                                    labelProvider = { it.toString().padStart(2, '0') }
                                )
                            }

                            // AM/PM Picker: AM or PM
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("AM/PM", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                WheelPicker(
                                    items = listOf("AM", "PM"),
                                    selectedIndex = listOf("AM", "PM").indexOf(selectedAmPm).coerceAtLeast(0),
                                    onItemSelected = { _, ap -> selectedAmPm = ap }
                                )
                            }
                        }
                    }
                }

                // 7. Submit / Form cancel/save buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_cancel_btn")
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val trimmedTitle = title.trim()

                                // Strict validation
                                if (trimmedTitle.length < 3) {
                                    titleErrorText = "Title must be at least 3 characters"
                                    titleFocusRequester.requestFocus()
                                    return@Button
                                }
                                
                                val finalReminder = Reminder(
                                    id = reminder?.id ?: 0,
                                    title = trimmedTitle,
                                    description = description.trim(),
                                    type = type,
                                    tamilMonth = if (type == "TAMIL") tamilMonth else if (type == "MOON") moonTamilMonthFilter else null,
                                    tamilDate = if (type == "TAMIL") tamilDateVal else null,
                                    englishDayOfMonth = if (type == "ENGLISH") englishDayOfMonth else null,
                                    moonPhaseType = if (type == "MOON") moonPhaseType else null,
                                    customGregorianDate = if (type == "CUSTOM") customGregorianDate else if (type == "ENGLISH") customGregorianDate else null,
                                    reminderTime = reminderTime,
                                    repeatSetting = repeatSetting,
                                    remindBeforeDays = remindBeforeDays,
                                    isEnabled = reminder?.isEnabled ?: true,
                                    isSystemReminder = isSystem,
                                    repeatType = if (repeatSetting == "ONE_TIME") "ONCE" else repeatSetting
                                )
                                onSave(finalReminder)
                            },
                            enabled = isTitleValid,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_save_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTitleValid) SaffronRed else Color(0xFFBDBDBD),
                                disabledContainerColor = Color(0xFFBDBDBD),
                                contentColor = Color.White,
                                disabledContentColor = Color(0xFF757575)
                            )
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabScreen(viewModel: TamilCalendarViewModel) {
    val defaultTime by viewModel.defaultReminderTime.collectAsStateWithLifecycle()
    val showNakshatra by viewModel.showNakshatra.collectAsStateWithLifecycle()
    val useDarkMode by viewModel.useDarkMode.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val defaultParts = remember(defaultTime) {
        val parts = defaultTime.split(":")
        val h24 = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val h12 = if (h24 == 0) 12 else if (h24 > 12) h24 - 12 else h24
        val amPm = if (h24 >= 12) "PM" else "AM"
        Triple(h12, m, amPm)
    }

    var hourIndex by remember { mutableStateOf(defaultParts.first - 1) }
    var minuteIndex by remember { mutableStateOf(defaultParts.second) }
    var amPmIndex by remember { mutableStateOf(if (defaultParts.third == "PM") 1 else 0) }

    LaunchedEffect(defaultParts) {
        val selectedHour12 = hourIndex + 1
        val selectedMinute = minuteIndex
        val selectedAmPm = if (amPmIndex == 1) "PM" else "AM"

        val currentLocalParts = Triple(selectedHour12, selectedMinute, selectedAmPm)
        if (currentLocalParts != defaultParts) {
            hourIndex = defaultParts.first - 1
            minuteIndex = defaultParts.second
            amPmIndex = if (defaultParts.third == "PM") 1 else 0
        }
    }

    var showSaved by remember { mutableStateOf(false) }
    var savedTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(savedTrigger) {
        if (savedTrigger > 0) {
            showSaved = true
            kotlinx.coroutines.delay(2000)
            showSaved = false
        }
    }

    fun saveDefaultTime() {
        val selectedHour12 = hourIndex + 1
        val selectedMinute = minuteIndex
        val selectedAmPm = if (amPmIndex == 1) "PM" else "AM"

        val h24 = when {
            selectedAmPm == "AM" && selectedHour12 == 12 -> 0
            selectedAmPm == "AM" -> selectedHour12
            selectedAmPm == "PM" && selectedHour12 == 12 -> 12
            else -> selectedHour12 + 12
        }

        val formattedResult = String.format("%02d:%02d", h24, selectedMinute)
        if (formattedResult != defaultTime) {
            viewModel.setDefaultReminderTime(formattedResult)
            savedTrigger++
        }
    }

    val hour = (hourIndex + 1).toString().padStart(2, '0')
    val minute = minuteIndex.toString().padStart(2, '0')
    val amPm = if (amPmIndex == 1) "PM" else "AM"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "அமைப்புகள் / Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("settings_title")
        )

        Divider()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("settings_time_card"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A237E)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Default Reminder Time",
                    fontSize = 13.sp,
                    color = Color(0xFFB0BEC5)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "$hour : $minute $amPm",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 3.sp
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(
                        items = (1..12).map { 
                            it.toString().padStart(2, '0') 
                        },
                        selectedIndex = hourIndex,
                        onSelected = { 
                            hourIndex = it
                            saveDefaultTime()
                        },
                        modifier = Modifier.width(70.dp),
                        infiniteScroll = true
                    )

                    Text(
                        ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    WheelPicker(
                        items = (0..59).map { 
                            it.toString().padStart(2, '0') 
                        },
                        selectedIndex = minuteIndex,
                        onSelected = { 
                            minuteIndex = it
                            saveDefaultTime()
                        },
                        modifier = Modifier.width(70.dp),
                        infiniteScroll = true
                    )

                    Spacer(Modifier.width(12.dp))

                    WheelPicker(
                        items = listOf("AM", "PM"),
                        selectedIndex = amPmIndex,
                        onSelected = { 
                            amPmIndex = it
                            saveDefaultTime()
                        },
                        modifier = Modifier.width(60.dp),
                        infiniteScroll = false
                    )
                }

                if (showSaved) {
                    Text(
                        text = "✓ Saved",
                        fontSize = 11.sp,
                        color = Color(0xFF81C784),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        SettingsToggleRow(
            title = "Show Nakshatras (நட்சத்திரங்கள்)",
            description = "Determine if daily nakshatra stellar names should be calculated and shown inside the day info panel.",
            checked = showNakshatra,
            onCheckedChange = { viewModel.setShowNakshatra(it) },
            testTag = "toggle_show_nakshatra"
        )

        SettingsToggleRow(
            title = "Enable Dark Mode Theme (இருண்ட தீம்)",
            description = "Toggle the application to use dark aesthetic tones instead of traditional amber-white light themes.",
            checked = useDarkMode,
            onCheckedChange = { viewModel.setUseDarkMode(it) },
            testTag = "toggle_dark_mode"
        )

        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Tamil Calendar App v1.0.0 (Offline Native Conversion)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag + "_switch")
        )
    }
}
