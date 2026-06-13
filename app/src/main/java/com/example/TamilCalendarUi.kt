package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

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
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (activeTab) {
                    "CALENDAR" -> CalendarTabScreen(viewModel)
                    "REMINDERS" -> RemindersTabScreen(viewModel)
                    "SETTINGS" -> SettingsTabScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun CalendarTabScreen(viewModel: TamilCalendarViewModel) {
    val activeMonthDate by viewModel.currentActiveMonthDate.collectAsStateWithLifecycle()
    val useTamilNumerals by viewModel.useTamilNumerals.collectAsStateWithLifecycle()
    val showNakshatra by viewModel.showNakshatra.collectAsStateWithLifecycle()
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()

    var selectedDayDetails by remember { mutableStateOf<TamilDate?>(null) }
    var touchXOffset by remember { mutableStateOf(0f) }

    // Dynamic month calculation
    val daysInMonth = activeMonthDate.lengthOfMonth()
    val firstDayOfWeek = activeMonthDate.withDayOfMonth(1).dayOfWeek.value % 7 // 0 for Sunday in calendar logic

    // Scan all dates in the month to see which Tamil Months are present
    val tamilMonthsInView = remember(activeMonthDate) {
        val list = mutableSetOf<String>()
        val englishTamilNames = mutableSetOf<String>()
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

    // Capture Tamil Year Name of the general active month (use middle of the month)
    val midMonthTamilDate = remember(activeMonthDate) {
        TamilCalendarHelper.getTamilDate(activeMonthDate.year, activeMonthDate.monthValue, 15)
    }

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
        // App top header matching Clean Minimalism (Parabhava Year)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 12.dp),
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
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.prevMonth() },
                        modifier = Modifier.size(36.dp).testTag("prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1C1B1F)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.size(36.dp).testTag("next_month_btn")
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
            
            // Large Month Name
            Text(
                text = activeMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F),
                modifier = Modifier.testTag("english_month_header")
            )
            
            // Subtitle of tamil months
            Text(
                text = tamilMonthsInView,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B), // Slate-500
                modifier = Modifier.testTag("tamil_month_header")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Weekdays Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val days = listOf("S", "M", "T", "W", "T", "F", "S")
            val daysTamil = listOf("ஞா", "தி", "செ", "பு", "வி", "வெ", "ச")
            for (i in 0..6) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = days[i],
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (i == 0) Color(0xFFEF4444) else Color(0xFF94A3B8)
                    )
                    Text(
                        text = daysTamil[i],
                        fontSize = 9.sp,
                        color = if (i == 0) Color(0xFFEF4444).copy(alpha = 0.7f) else Color(0xFF94A3B8).copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid Layout
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.5.dp) // minimal clean lines
        ) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..daysInMonth) {
                            val currentDate = activeMonthDate.withDayOfMonth(dayNumber)
                            val tDate = remember(currentDate) {
                                TamilCalendarHelper.getTamilDate(
                                    currentDate.year,
                                    currentDate.monthValue,
                                    currentDate.dayOfMonth
                                )
                            }
                            val isToday = remember(currentDate) {
                                currentDate == LocalDate.now()
                            }

                            CalendarCell(
                                dayNumber = dayNumber,
                                tamilDate = tDate,
                                isToday = isToday,
                                useTamilNumerals = useTamilNumerals,
                                showNakshatra = showNakshatra,
                                onClick = { selectedDayDetails = tDate },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("day_${dayNumber}")
                            )
                        } else {
                            // Blank cell styling
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                                    .background(Color.White)
                                    .border(0.5.dp, Color(0xFFF1F5F9))
                            )
                        }
                    }
                }
            }
        }

        // Quick Indicators Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IndicatorLabel(color = Color(0xFF005FB0), text = "Today")
            IndicatorLabel(color = Color(0xFFFFFBEB), border = Color(0xFFFBBF24), text = "Full Moon")
            IndicatorLabel(color = Color(0xFFF1F5F9), border = Color(0xFFCBD5E1), text = "New Moon")
            IndicatorLabel(color = Color.Transparent, text = "Holiday (Red)", isRedText = true)
        }
    }

    // Show day details sheet when tapped
    selectedDayDetails?.let { tDate ->
        DayDetailsDialog(
            tamilDate = tDate,
            showNakshatra = showNakshatra,
            reminders = reminders.filter { reminder ->
                // Filter matching reminder instances showing up on this specific day
                val eventTrigger = ReminderScheduler.calculateNextTrigger(reminder, LocalDate.of(tDate.englishYear, tDate.englishMonth, tDate.englishDay).atStartOfDay())
                eventTrigger?.toLocalDate() == LocalDate.of(tDate.englishYear, tDate.englishMonth, tDate.englishDay)
            },
            onDismiss = { selectedDayDetails = null }
        )
    }
}

@Composable
fun IndicatorLabel(color: Color, text: String, border: Color? = null, isRedText: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!isRedText) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (border != null) Modifier.border(0.5.dp, border, CircleShape) else Modifier
                    )
            )
        }
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isRedText) Color(0xFFEF4444) else Color(0xFF64748B),
            fontWeight = if (isRedText || text == "Today") FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarCell(
    dayNumber: Int,
    tamilDate: TamilDate,
    isToday: Boolean,
    useTamilNumerals: Boolean,
    showNakshatra: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFestival = tamilDate.festivalName != null && tamilDate.festivalName != "Pournami / பௌர்ணமி" && tamilDate.festivalName != "Amavasai / அமாவாசை"
    
    // Clean Minimalism Cell Backgrounds
    val bgColors = when {
        isToday -> Color.White // Uses a solid circle container instead of whole cell bg
        tamilDate.isPournami -> Color(0xFFFEF3C7).copy(alpha = 0.5f) // bg-amber-50
        tamilDate.isAmavasai -> Color(0xFFF1F5F9) // slate-100
        else -> Color.White
    }

    val contentColor = when {
        isToday -> Color.White
        isFestival -> Color(0xFFEF4444) // Holiday red
        tamilDate.isPournami -> Color(0xFFB45309) // Amber-700
        else -> Color(0xFF1C1B1F)
    }

    val nakshatrasList = tamilDate.nakshatra.split(" / ")
    val nakshatraToDisplay = if (nakshatrasList.size > 1) nakshatrasList[1] else tamilDate.nakshatra

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .background(bgColors)
            .border(
                width = 0.5.dp,
                color = Color(0xFFF1F5F9) // slate-100 border
            )
            .combinedClickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isToday) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Crisp high-contrast circle for today
                Box(
                    modifier = Modifier
                        .size(if (showNakshatra) 32.dp else 38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF005FB0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dayNumber.toString(),
                            fontSize = if (showNakshatra) 11.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("english_date_${dayNumber}")
                        )
                        val tamilDayLabel = if (useTamilNumerals) {
                            TamilCalendarHelper.toTamilNumerals(tamilDate.tamilDay)
                        } else {
                            tamilDate.tamilDay.toString()
                        }
                        Text(
                            text = tamilDayLabel,
                            fontSize = if (showNakshatra) 7.sp else 8.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.testTag("tamil_date_${dayNumber}")
                        )
                    }
                }
                if (showNakshatra) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = nakshatraToDisplay,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF005FB0),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.testTag("nakshatra_${dayNumber}")
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                // Top tag for month transitions or specialized icons
                if (tamilDate.tamilDay == 1) {
                    Text(
                        text = tamilDate.tamilMonthNameTamil,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center,
                        lineHeight = 7.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (tamilDate.isPournami || tamilDate.isAmavasai) {
                    Text(
                        text = if (tamilDate.isPournami) "POURNAMI" else "AMAVASAI",
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tamilDate.isPournami) Color(0xFFB45309) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 7.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // English Day
                Text(
                    text = dayNumber.toString(),
                    fontSize = if (showNakshatra) 14.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.testTag("english_date_${dayNumber}")
                )

                if (showNakshatra) {
                    Text(
                        text = nakshatraToDisplay,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isFestival) Color(0xFFEF4444).copy(alpha = 0.8f) else Color(0xFF6750A4),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.testTag("nakshatra_${dayNumber}")
                    )
                }

                // Tamil Day Numeral Text
                val tamilDayLabel = if (useTamilNumerals) {
                    TamilCalendarHelper.toTamilNumerals(tamilDate.tamilDay)
                } else {
                    tamilDate.tamilDay.toString()
                }

                Text(
                    text = tamilDayLabel,
                    fontSize = 9.sp,
                    color = if (isFestival) Color(0xFFEF4444) else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("tamil_date_${dayNumber}")
                )
            }
        }
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

                // Festival Spotlight section if a festival exists
                if (tamilDate.festivalName != null) {
                    val festivalDesc = FestivalSignificanceProvider.festivalDescriptions[tamilDate.festivalName]
                    val accentColor = festivalDesc?.colorAccent ?: SaffronRed
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                            } else {
                                Text(
                                    text = tamilDate.festivalName,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Standard Info Grid
                InfoRow(label = "English Date", value = "${tamilDate.englishDay}-${tamilDate.englishMonth}-${tamilDate.englishYear}")
                InfoRow(label = "Tamil Month", value = "${tamilDate.tamilMonthNameTamil} / ${tamilDate.tamilMonthNameEnglish}")
                InfoRow(label = "Tamil Date", value = "${tamilDate.tamilDay} (Tamil Numeral: ${TamilCalendarHelper.toTamilNumerals(tamilDate.tamilDay)})")
                
                val cycleName = tamilDate.tamilYearName
                InfoRow(label = "Tamil Year", value = cycleName)

                // Simplified Tithi
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

                // Show today's active scheduled alarms/reminders
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(1.dp) // using spacer or weight to avoid squeeze
        )
    }
}

@Composable
fun RemindersTabScreen(viewModel: TamilCalendarViewModel) {
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    var isAddEditOpen by remember { mutableStateOf(false) }
    var selectedReminderForEdit by remember { mutableStateOf<Reminder?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "நினைவூட்டல்கள் / Reminders",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("reminders_title")
            )
            Text(
                text = "Manage recurring ceremonies, festivals, or custom reminders completely offline.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No reminders",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Reminders Found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the '+' floating button to set reminders.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("reminders_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = reminders, key = { it.id }) { reminder ->
                        ReminderItemRow(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminderEnabled(reminder) },
                            onDelete = { viewModel.deleteReminder(reminder) },
                            onClick = {
                                selectedReminderForEdit = reminder
                                isAddEditOpen = true
                            }
                        )
                    }
                }
            }
        }

        // FAB to Add Reminder
        FloatingActionButton(
            onClick = {
                selectedReminderForEdit = null
                isAddEditOpen = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_reminder_fab"),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Reminder", tint = Color.White)
        }
    }

    if (isAddEditOpen) {
        AddEditReminderDialog(
            reminder = selectedReminderForEdit,
            onDismiss = {
                isAddEditOpen = false
                selectedReminderForEdit = null
            },
            onSave = { reminder ->
                viewModel.saveReminder(reminder)
                isAddEditOpen = false
                selectedReminderForEdit = null
            }
        )
    }
}

@Composable
fun ReminderItemRow(
    reminder: Reminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val nextTrigger = remember(reminder) {
        ReminderScheduler.calculateNextTrigger(reminder)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("reminder_item_${reminder.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            val icon: androidx.compose.ui.graphics.vector.ImageVector
            val tint: androidx.compose.ui.graphics.Color
            when (reminder.type) {
                "TAMIL" -> {
                    icon = Icons.Default.Info
                    tint = SaffronRed
                }
                "ENGLISH" -> {
                    icon = Icons.Default.Star
                    tint = Color(0xFF3498DB)
                }
                "MOON" -> {
                    icon = Icons.Default.Favorite
                    tint = GoldenTurmeric
                }
                else -> {
                    icon = Icons.Default.Notifications
                    tint = Color(0xFF27AE60)
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = reminder.type, tint = tint)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                // Show type description
                val detailsDesc = when (reminder.type) {
                    "TAMIL" -> {
                        val mName = TamilCalendarHelper.tamilMonthsEnglish[(reminder.tamilMonth ?: 1) - 1]
                        "Tamil: $mName ${reminder.tamilDate}"
                    }
                    "ENGLISH" -> "Day ${reminder.englishDayOfMonth} of Month"
                    "MOON" -> "Moon: ${reminder.moonPhaseType}"
                    else -> "Date: ${reminder.customGregorianDate}"
                }

                Text(
                    text = "$detailsDesc | ${reminder.repeatSetting.lowercase()}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                if (reminder.isEnabled && nextTrigger != null) {
                    Text(
                        text = "Next: ${nextTrigger.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))}",
                        fontSize = 11.sp,
                        color = Color(0xFF27AE60),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("toggle_switch_${reminder.id}")
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_btn_${reminder.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderDialog(
    reminder: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var description by remember { mutableStateOf(reminder?.description ?: "") }
    var type by remember { mutableStateOf(reminder?.type ?: "CUSTOM") } // "TAMIL", "ENGLISH", "MOON", "CUSTOM"
    
    // Tamil attributes
    var tamilMonth by remember { mutableStateOf(reminder?.tamilMonth ?: 1) }
    var tamilDateVal by remember { mutableStateOf(reminder?.tamilDate ?: 1) }

    // English attributes
    var englishDayOfMonth by remember { mutableStateOf(reminder?.englishDayOfMonth ?: 1) }

    // Moon attributes
    var moonPhaseType by remember { mutableStateOf(reminder?.moonPhaseType ?: "POURNAMI") }
    var moonTamilMonthFilter by remember { mutableStateOf(reminder?.tamilMonth) } // Optional month filter

    // Custom attributes
    var customGregorianDate by remember { mutableStateOf(reminder?.customGregorianDate ?: LocalDate.now().toString()) }

    var reminderTime by remember { mutableStateOf(reminder?.reminderTime ?: "08:00") }
    var repeatSetting by remember { mutableStateOf(reminder?.repeatSetting ?: "ONE_TIME") }
    var remindBeforeDays by remember { mutableStateOf(reminder?.remindBeforeDays ?: 0) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Error logging
    var titleError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
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
                item {
                    Text(
                        text = if (reminder == null) "புதிய நினைவூட்டல் / Create Reminder" else "தொகு / Edit Reminder",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Title Input
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = it.isEmpty()
                        },
                        label = { Text("நினைவூட்டல் தலைப்பு / Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_title_input"),
                        isError = titleError,
                        supportingText = {
                            if (titleError) {
                                Text("Title is required", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }

                // Description Input
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("குறிப்பு உரை / Description Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Type Picker Tabs
                item {
                    Text("வகை / Reminder Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("CUSTOM" to "Custom", "TAMIL" to "Tamil", "ENGLISH" to "English", "MOON" to "Moon").forEach { (key, label) ->
                            Button(
                                onClick = { type = key },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (type == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if (type == key) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("type_tab_${key.lowercase()}")
                                    .padding(vertical = 2.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Dynamic Input Form Fields based on Selected Type
                when (type) {
                    "TAMIL" -> {
                        item {
                            Text("Tamil Month Selection:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            // Simple drop down text list selection
                            var monthExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { monthExpanded = true }, modifier = Modifier.fillMaxWidth().testTag("tamil_month_drop")) {
                                    Text(TamilCalendarHelper.tamilMonthsEnglish[tamilMonth - 1] + " / " + TamilCalendarHelper.tamilMonthsTamil[tamilMonth - 1])
                                }
                                DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                                    for (i in 1..12) {
                                        DropdownMenuItem(
                                            text = { Text(TamilCalendarHelper.tamilMonthsEnglish[i - 1] + " / " + TamilCalendarHelper.tamilMonthsTamil[i - 1]) },
                                            onClick = {
                                                tamilMonth = i
                                                monthExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text("Tamil Date (1-32):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            var dateExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { dateExpanded = true }, modifier = Modifier.fillMaxWidth().testTag("tamil_date_drop")) {
                                    Text("Date: $tamilDateVal (Tamil Name: ${TamilCalendarHelper.toTamilNumerals(tamilDateVal)})")
                                }
                                DropdownMenu(expanded = dateExpanded, onDismissRequest = { dateExpanded = false }) {
                                    for (i in 1..32) {
                                        DropdownMenuItem(
                                            text = { Text("Date $i (${TamilCalendarHelper.toTamilNumerals(i)})") },
                                            onClick = {
                                                tamilDateVal = i
                                                dateExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "ENGLISH" -> {
                        item {
                            Text("English Day of Month (1-31):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            var dayExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { dayExpanded = true }, modifier = Modifier.fillMaxWidth().testTag("english_day_drop")) {
                                    Text("Day of the month: $englishDayOfMonth")
                                }
                                DropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                                    for (i in 1..31) {
                                        DropdownMenuItem(
                                            text = { Text("Day $i") },
                                            onClick = {
                                                englishDayOfMonth = i
                                                dayExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "MOON" -> {
                        item {
                            Text("Moon Occurrence Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            val phases = listOf("POURNAMI" to "Pournami (Full Moon)", "AMAVASAI" to "Amavasai (New Moon)", "EKADASHI" to "Ekadashi (11th Tithi)", "PRADOSHAM" to "Pradosham (13th Tithi)")
                            var phaseExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { phaseExpanded = true }, modifier = Modifier.fillMaxWidth().testTag("moon_phase_drop")) {
                                    Text(phases.find { it.first == moonPhaseType }?.second ?: moonPhaseType)
                                }
                                DropdownMenu(expanded = phaseExpanded, onDismissRequest = { phaseExpanded = false }) {
                                    phases.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p.second) },
                                            onClick = {
                                                moonPhaseType = p.first
                                                phaseExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text("Optional Tamil Month Filter (e.g. for Diwali/Karthikai Deepam):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            var filterExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { filterExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        if (moonTamilMonthFilter == null) "None / All Months" 
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
                                            text = { Text(TamilCalendarHelper.tamilMonthsEnglish[i - 1] + " / " + TamilCalendarHelper.tamilMonthsTamil[i - 1]) },
                                            onClick = {
                                                moonTamilMonthFilter = i
                                                filterExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "CUSTOM" -> {
                        item {
                            OutlinedTextField(
                                value = customGregorianDate,
                                onValueChange = { customGregorianDate = it },
                                label = { Text("English Date (YYYY-MM-DD)") },
                                placeholder = { Text("e.g. 2026-06-13") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_date_input")
                            )
                        }
                    }
                }

                // Reminder Trigger Time Input
                item {
                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text("நேரம் / Reminder Time (HH:MM)") },
                        placeholder = { Text("e.g. 08:00") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_time_input")
                    )
                }

                // Repeat Setting Selection
                item {
                    Text("மீள்செயல் / Repeat Frequency:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val repeats = listOf("ONE_TIME" to "Once", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "YEARLY" to "Yearly")
                    var repeatExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { repeatExpanded = true }, modifier = Modifier.fillMaxWidth().testTag("repeat_drop")) {
                            Text(repeats.find { it.first == repeatSetting }?.second ?: repeatSetting)
                        }
                        DropdownMenu(expanded = repeatExpanded, onDismissRequest = { repeatExpanded = false }) {
                            repeats.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.second) },
                                    onClick = {
                                        repeatSetting = r.first
                                        repeatExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Remind before offset selecting
                item {
                    Text("முன்கூட்டியே நினைவூட்டு / Remind Before (Days):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0 to "Same Day", 1 to "1 Day Before", 2 to "2 Days Before").forEach { (days, label) ->
                            FilterChip(
                                selected = remindBeforeDays == days,
                                onClick = { remindBeforeDays = days },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("offset_chip_$days")
                            )
                        }
                    }
                }

                // Submit Form Buttons
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
                                if (title.isEmpty()) {
                                    titleError = true
                                    return@Button
                                }
                                val newReminder = Reminder(
                                    id = reminder?.id ?: 0,
                                    title = title,
                                    description = description,
                                    type = type,
                                    tamilMonth = if (type == "TAMIL") tamilMonth else if (type == "MOON") moonTamilMonthFilter else null,
                                    tamilDate = if (type == "TAMIL") tamilDateVal else null,
                                    englishDayOfMonth = if (type == "ENGLISH") englishDayOfMonth else null,
                                    moonPhaseType = if (type == "MOON") moonPhaseType else null,
                                    customGregorianDate = if (type == "CUSTOM") customGregorianDate else null,
                                    reminderTime = reminderTime,
                                    repeatSetting = repeatSetting,
                                    remindBeforeDays = remindBeforeDays,
                                    isEnabled = reminder?.isEnabled ?: true
                                )
                                onSave(newReminder)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_save_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronRed)
                        ) {
                            Text("Save", color = Color.White)
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
    val useTamilNumerals by viewModel.useTamilNumerals.collectAsStateWithLifecycle()
    val showNakshatra by viewModel.showNakshatra.collectAsStateWithLifecycle()
    val useDarkMode by viewModel.useDarkMode.collectAsStateWithLifecycle()

    var timeInput by remember { mutableStateOf(defaultTime) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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

        // Standard Default Reminder Time Field
        Card(
            modifier = Modifier.fillMaxWidth().testTag("settings_time_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Default Reminder Time (நினைவூட்டல் நேரம்):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        placeholder = { Text("e.g. 08:00") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("default_time_input"),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.setDefaultReminderTime(timeInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronRed),
                        modifier = Modifier.testTag("save_time_btn")
                    ) {
                        Text("Save Status", color = Color.White)
                    }
                }
            }
        }

        // Toggles list
        SettingsToggleRow(
            title = "Use Tamil Numerals (தமிழ் எண்கள்)",
            description = "Toggle to display Tamil scripts numbers (e.g., ௧ ௨ ௩) in the monthly calendar cells instead of standard digits.",
            checked = useTamilNumerals,
            onCheckedChange = { viewModel.setUseTamilNumerals(it) },
            testTag = "toggle_tamil_numerals"
        )

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

        Spacer(modifier = Modifier.weight(1f))

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
