package com.example

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

private val AuspiciousGreen = Color(0xFF1B5E20)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsBottomSheet(
    tamilDate: TamilDate,
    showNakshatra: Boolean,
    reminders: List<Reminder>,
    onDismiss: () -> Unit,
    onAddReminder: (LocalDate) -> Unit,
    onDeleteReminder: (Reminder) -> Unit
) {
    val date = LocalDate.of(tamilDate.englishYear, tamilDate.englishMonth, tamilDate.englishDay)
    val dayOfWeekName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val formattedEngDate = "$dayOfWeekName, ${tamilDate.englishDay} ${DateTimeFormatter.ofPattern("MMMM yyyy").format(date)}"

    val dayOfWeek = date.atStartOfDay(java.time.ZoneId.systemDefault()).let {
        val c = Calendar.getInstance()
        c.timeInMillis = it.toInstant().toEpochMilli()
        c.get(Calendar.DAY_OF_WEEK)
    }
    val (sunrise, sunset) = TamilCalendarHelper.getTodaySunriseSunset(date)
    val rahu = TamilCalendarHelper.calculateRahuKalam(dayOfWeek, sunrise, sunset)
    val yama = TamilCalendarHelper.calculateYamagandam(dayOfWeek, sunrise, sunset)
    val kuligai = TamilCalendarHelper.calculateKuligai(dayOfWeek, sunrise, sunset)

    val midday = (sunrise + sunset) / 2
    val abhijitStart = midday - 24 * 60 * 1000L
    val abhijitEnd = midday + 24 * 60 * 1000L

    val now = System.currentTimeMillis()
    val isToday = (date == LocalDate.now())
    val isRahuActive = isToday && now in rahu.first..rahu.second
    val isYamaActive = isToday && now in yama.first..yama.second
    val isKuliActive = isToday && now in kuligai.first..kuligai.second

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        modifier = Modifier.testTag("day_details_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "நாள் விபரம் / Day Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronRed
                )
                Text(
                    text = formattedEngDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Text(
                    text = "${tamilDate.tamilMonthNameTamil} ${tamilDate.tamilDay} / ${tamilDate.tamilMonthNameEnglish} ${tamilDate.tamilDay}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B00)
                )
                Text(
                    text = "Year Name: ${tamilDate.tamilYearName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            HorizontalDivider()

            // Lunar Phase & Tithi
            val paksha = if (tamilDate.tithi <= 15) "வளர்பிறை / Shukla Paksha (Waxing)" else "தேய்பிறை / Krishna Paksha (Waning)"
            val phaseIcon = if (tamilDate.tithi <= 15) "📈" else "📉"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🌙 Lunar Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(4.dp))
                    Text("Phase: $paksha $phaseIcon", fontSize = 13.sp)
                    Text("Tithi: ${tamilDate.tithi} (Age: ${tamilDate.lunarAge} days)", fontSize = 13.sp)
                    if (showNakshatra && tamilDate.nakshatra.isNotEmpty()) {
                        Text("Nakshatra (நட்சத்திரம்): ${tamilDate.nakshatra} ⭐", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Inauspicious Times Section with dynamic activity checks
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "━━ Inauspicious Times (அசுப நேரம்) ━━",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                // Rahu Kalam Row
                InauspiciousItem(
                    label = "Rahu Kalam (இராகு காலம்)",
                    range = TamilCalendarHelper.formatTimeRange(rahu.first, rahu.second),
                    desc = "Avoid starting auspicious operations",
                    isActive = isRahuActive,
                    elapsedMillis = now - rahu.first,
                    totalMillis = rahu.second - rahu.first,
                    pillColor = SaffronRed
                )

                // Yamagandam Row
                InauspiciousItem(
                    label = "Yamagandam (எமகண்டம்)",
                    range = TamilCalendarHelper.formatTimeRange(yama.first, yama.second),
                    desc = "Avoid starts and travel",
                    isActive = isYamaActive,
                    elapsedMillis = now - yama.first,
                    totalMillis = yama.second - yama.first,
                    pillColor = Color(0xFFF57C00)
                )

                // Kuligai Row
                InauspiciousItem(
                    label = "Kuligai (குளிகை)",
                    range = TamilCalendarHelper.formatTimeRange(kuligai.first, kuligai.second),
                    desc = "Good for routine chores",
                    isActive = isKuliActive,
                    elapsedMillis = now - kuligai.first,
                    totalMillis = kuligai.second - kuligai.first,
                    pillColor = Color(0xFFE65100)
                )
            }

            // Auspicious times Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "━━ Auspicious Times (சுப நேரம்) ━━",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🟢", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                    Column {
                        Text("Abhijit Muhurtham (அபிஜித் முகூர்த்தம்)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = AuspiciousGreen)
                        Text(TamilCalendarHelper.formatTimeRange(abhijitStart, abhijitEnd), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Auspicious midday hour for general goodness", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            // Festivals Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "━━ Festivals (பண்டிகைகள்) ━━",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = if (tamilDate.festivalName != null) "🎉 ${tamilDate.festivalName}" else "🎉 No scheduled festivals for today",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (tamilDate.festivalName != null) SaffronRed else Color.Black
                )
            }

            // Reminders Section with delete
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "━━ Custom Reminders (நினைவூட்டல்கள்) ━━",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (reminders.isEmpty()) {
                    Text("🔔 No reminders set on this day", fontSize = 13.sp, color = Color.Gray)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (r in reminders) {
                            Surface(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Alert",
                                            tint = Color(0xFF1565C0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = r.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (r.description.isNotEmpty()) {
                                                Text(r.description, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteReminder(r) },
                                        modifier = Modifier.size(24.dp).testTag("delete_reminder_${r.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = SaffronRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val today = LocalDate.now()
            val isPastDate = date.isBefore(today)

            if (!isPastDate) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onAddReminder(date)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("add_reminder_from_sheet"),
                    border = BorderStroke(1.5.dp, Color(0xFFFF6B00))
                ) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFFFF6B00))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Reminder for this day",
                        color = Color(0xFFFF6B00)
                    )
                }
            } else {
                Text(
                    text = "📅 Past date — reminders not available",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun InauspiciousItem(
    label: String,
    range: String,
    desc: String,
    isActive: Boolean,
    elapsedMillis: Long,
    totalMillis: Long,
    pillColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                Text("🔴", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                Column {
                    Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = pillColor)
                    Text(range, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(desc, fontSize = 11.sp, color = Color.Gray)
                }
            }
            if (isActive) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, SaffronRed)
                ) {
                    Text(
                        text = "ACTIVE NOW",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronRed,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (isActive && totalMillis > 0) {
            val ratio = (elapsedMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = pillColor,
                trackColor = Color(0xFFEEEEEE)
            )
        }
    }
}

@Composable
fun FamilyBirthdayTrackerScreen(
    viewModel: TamilCalendarViewModel,
    onBack: () -> Unit
) {
    val familyList by viewModel.allFamilyMembers.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("birthday_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Family Birthdays", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SaffronRed)
                }
            }

        Spacer(Modifier.height(16.dp))

        if (familyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎂", fontSize = 48.sp)
                    Text("No family member birthdays added yet", fontWeight = FontWeight.Medium, color = Color.Gray)
                    Text("Tap + Add to keep track and schedule yearly alerts!", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(familyList) { member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF5)),
                        border = BorderStroke(0.5.dp, Color(0xFFFFE0B2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Text("Relationship: ${member.relationship}", fontSize = 12.sp, color = Color.Gray)
                                Text("🎂 English Birthday: ${member.birthDateEnglish}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(2.dp))
                                Text("Tamil Star: ${member.birthNakshatra}", fontSize = 12.sp)
                                Text("Tamil Date: ${member.birthTamilMonth} ${member.birthTamilDate}", fontSize = 12.sp)
                                if (member.phoneNumber.isNotEmpty()) {
                                    Text("📞 Phone: ${member.phoneNumber}", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteFamilyMember(member)
                                    Toast.makeText(context, "Deleted birthday of ${member.name}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("delete_member_${member.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SaffronRed)
                            }
                        }
                    }
                }
            }
        }
        }
        
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_birthday_btn"),
            containerColor = Color(0xFFB71C1C),
            contentColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            text = {
                Text(
                    text = "Add",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    softWrap = false
                )
            }
        )
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var relationship by remember { mutableStateOf("") }
        var birthDateEnglish by remember { mutableStateOf("") }
        var birthNakshatra by remember { mutableStateOf("") }
        var birthTamilMonth by remember { mutableStateOf("") }
        var birthTamilDateStr by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }

        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val localD = LocalDate.of(year, month + 1, dayOfMonth)
                birthDateEnglish = localD.toString() // yyyy-MM-dd
                
                // Prefill Tamil Details automatically
                val tDate = TamilCalendarHelper.getTamilDate(year, month + 1, dayOfMonth)
                birthTamilMonth = tDate.tamilMonthNameTamil
                birthTamilDateStr = tDate.tamilDay.toString()
                birthNakshatra = tDate.nakshatra.split(" / ").first().trim()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("add_family_member_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Add Family Birthday 🍰", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaffronRed)
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth().testTag("member_name")
                    )

                    OutlinedTextField(
                        value = relationship,
                        onValueChange = { relationship = it },
                        label = { Text("Relationship (e.g. Spouse, Son)") },
                        modifier = Modifier.fillMaxWidth().testTag("member_ref")
                    )

                    // English birthdate trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (birthDateEnglish.isEmpty()) "Tap to select Birth Date" else "Date: $birthDateEnglish",
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }

                    OutlinedTextField(
                        value = birthNakshatra,
                        onValueChange = { birthNakshatra = it },
                        label = { Text("Birth Star / Nakshatra") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = birthTamilMonth,
                            onValueChange = { birthTamilMonth = it },
                            label = { Text("Tamil Month") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = birthTamilDateStr,
                            onValueChange = { birthTamilDateStr = it },
                            label = { Text("Tamil Date No.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val dateNo = birthTamilDateStr.toIntOrNull() ?: 1
                                if (name.isNotEmpty() && birthDateEnglish.isNotEmpty()) {
                                    val member = FamilyMember(
                                        name = name,
                                        relationship = relationship,
                                        birthDateEnglish = birthDateEnglish,
                                        birthNakshatra = birthNakshatra,
                                        birthTamilMonth = birthTamilMonth,
                                        birthTamilDate = dateNo,
                                        phoneNumber = phoneNumber
                                    )
                                    viewModel.saveFamilyMember(member)
                                    showAddDialog = false
                                    Toast.makeText(context, "Added birthday for $name!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please fill in Name and Birth Date", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronRed),
                            modifier = Modifier.testTag("save_member_btn")
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
fun ChitFundTrackerScreen(
    viewModel: TamilCalendarViewModel,
    onBack: () -> Unit
) {
    val chitList by viewModel.allChitFunds.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    
    // For editing/claiming auctions for each installment
    var showAuctionDialog by remember { mutableStateOf(false) }
    var selectedChitForAuction by remember { mutableStateOf<ChitFund?>(null) }
    var selectedInstallmentIndex by remember { mutableStateOf(1) }
    
    val context = LocalContext.current
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chit_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Chit Fund Tracker", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SaffronRed)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (chitList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🪙", fontSize = 48.sp)
                        Text(
                            "No active chits registered yet",
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            "Tap + Add to keep track of monthly installments and view in Calendar!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chitList) { chit ->
                        var isExpanded by remember { mutableStateOf(false) }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FFF9)),
                            border = BorderStroke(0.5.dp, Color(0xFFA5D6A7))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header section with Name & Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chit.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AuspiciousGreen
                                        )
                                        Text(
                                            text = "Value: ₹${String.format("%,.0f", chit.chitValue)} | Duration: ${chit.totalMonths} mos",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { isExpanded = !isExpanded },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Expand details",
                                                tint = AuspiciousGreen
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteChitFund(chit)
                                                Toast.makeText(context, "Deleted Chit ${chit.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(36.dp).testTag("delete_chit_${chit.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = SaffronRed
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFE8F5E9))

                                // Basic summary layout
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Max contribution", fontSize = 11.sp, color = Color.Gray)
                                        Text("₹${String.format("%,.0f", chit.monthlyContribution)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Due date", fontSize = 11.sp, color = Color.Gray)
                                        Text("Day ${chit.paymentDayOfMonth} of month", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // High quality balance details
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Paid", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("₹${String.format("%,.0f", chit.getTotalPaidByUser())}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AuspiciousGreen)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Dividends", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("₹${String.format("%,.0f", chit.getTotalDividendsEarned())}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE55100))
                                    }
                                }

                                // Displays User claims/payouts
                                if (chit.isWonByUser) {
                                    Surface(
                                        color = Color(0xFFFFF9C4),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🏆 ", fontSize = 18.sp)
                                            Column {
                                                Text("Claimed in Month ${chit.userWonMonth}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4527A0))
                                                Text("Prize Received: ₹${String.format("%,.0f", chit.userWonAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "⚡ Chit not won yet. Click on edit below to record winning bid and claim payouts.",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }

                                // EXPANDED INSTALLMENTS LIST
                                if (isExpanded) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Installments Details:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AuspiciousGreen)
                                    
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        for (m in 1..chit.totalMonths) {
                                            val details = chit.getInstallmentDetails(m)
                                            
                                            Surface(
                                                color = if (m == chit.userWonMonth && chit.isWonByUser) Color(0xFFFFFDE7) else Color.White,
                                                border = BorderStroke(0.5.dp, if (m == chit.userWonMonth) Color(0xFFFBC02D) else Color(0xFFE0E0E0)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    // Left: Month, Paid checkbox
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1.3f)
                                                    ) {
                                                        Checkbox(
                                                            checked = details.isPaid,
                                                            onCheckedChange = { checked ->
                                                                val currentPaid = chit.getPaidMonths().toMutableList()
                                                                if (checked) {
                                                                    if (!currentPaid.contains(m)) currentPaid.add(m)
                                                                } else {
                                                                    currentPaid.remove(m)
                                                                }
                                                                val updatedChit = chit.copy(
                                                                    paidMonthsJson = currentPaid.sorted().toString()
                                                                )
                                                                viewModel.updateChitFund(updatedChit)
                                                            },
                                                            modifier = Modifier.testTag("pay_installment_${chit.id}_$m")
                                                        )
                                                        Column {
                                                            Text(
                                                                text = "Month $m",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (m == chit.userWonMonth) Color(0xFFE65100) else Color.Black
                                                            )
                                                            if (m == chit.userWonMonth && chit.isWonByUser) {
                                                                Text("👑 PRIZE CLAIMED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                                                            }
                                                        }
                                                    }

                                                    // Middle: Bid and payment calculations
                                                    Column(
                                                        modifier = Modifier.weight(2f),
                                                        horizontalAlignment = Alignment.Start
                                                    ) {
                                                        Text("Bid Discount: ₹${String.format("%,.0f", details.winningBidDiscount)}", fontSize = 11.sp, color = Color.Gray)
                                                        Text("Pays: ₹${String.format("%,.0f", details.finalPayment)} (Div: ₹${String.format("%,.0f", details.dividendPerMember)})", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                    }

                                                    // Right: Edit Bid / Claim
                                                    IconButton(
                                                        onClick = {
                                                            selectedChitForAuction = chit
                                                            selectedInstallmentIndex = m
                                                            showAuctionDialog = true
                                                        },
                                                        modifier = Modifier.size(32.dp).testTag("edit_bid_${chit.id}_$m")
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit bid", tint = AuspiciousGreen, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // End of Column

        // Extended Floating Action Button using correct styling
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_chit_btn"),
            containerColor = Color(0xFFB71C1C), // dark red / maroon
            contentColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            text = {
                Text(
                    text = "Add",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    softWrap = false
                )
            }
        )
    }

    // ADD CHIT DIALOG DEFINITION
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var chitValueStr by remember { mutableStateOf("") }
        var totalMonthsStr by remember { mutableStateOf("20") }
        var membersCountStr by remember { mutableStateOf("20") }
        var commissionStr by remember { mutableStateOf("5") }
        var paymentDayStr by remember { mutableStateOf("10") }
        var startYearStr by remember { mutableStateOf(LocalDate.now().year.toString()) }
        var startMonthStr by remember { mutableStateOf(LocalDate.now().monthValue.toString()) }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("add_chit_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Add Tamil Chit Fund 🪙", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaffronRed)
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Chit Name") },
                        modifier = Modifier.fillMaxWidth().testTag("chit_name")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = chitValueStr,
                            onValueChange = { chitValueStr = it },
                            label = { Text("Total Value (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.2f).testTag("chit_value")
                        )
                        OutlinedTextField(
                            value = commissionStr,
                            onValueChange = { commissionStr = it },
                            label = { Text("Comm %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = totalMonthsStr,
                            onValueChange = { 
                                totalMonthsStr = it 
                                membersCountStr = it // Auto fill members count to match total months
                            },
                            label = { Text("Months") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = membersCountStr,
                            onValueChange = { membersCountStr = it },
                            label = { Text("Members") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startYearStr,
                            onValueChange = { startYearStr = it },
                            label = { Text("Start Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.1f)
                        )
                        OutlinedTextField(
                            value = startMonthStr,
                            onValueChange = { startMonthStr = it },
                            label = { Text("Month (1-12)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    OutlinedTextField(
                        value = paymentDayStr,
                        onValueChange = { paymentDayStr = it },
                        label = { Text("Payment Day No of Month") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val value = chitValueStr.toDoubleOrNull() ?: 0.0
                                val months = totalMonthsStr.toIntOrNull() ?: 1
                                val members = membersCountStr.toIntOrNull() ?: 1
                                val commission = commissionStr.toDoubleOrNull() ?: 5.0
                                val payDay = paymentDayStr.toIntOrNull() ?: 10
                                val sYear = startYearStr.toIntOrNull() ?: LocalDate.now().year
                                val sMonth = startMonthStr.toIntOrNull() ?: LocalDate.now().monthValue
                                
                                val maxContribution = if (members > 0) value / members else 0.0
                                
                                if (name.isNotEmpty() && value > 0.0) {
                                    val newChit = ChitFund(
                                        name = name,
                                        chitValue = value,
                                        totalMonths = months,
                                        membersCount = members,
                                        companyCommissionPercent = commission,
                                        monthlyContribution = maxContribution,
                                        startYear = sYear,
                                        startMonth = sMonth,
                                        paymentDayOfMonth = payDay
                                    )
                                    viewModel.saveChitFund(newChit)
                                    showAddDialog = false
                                    Toast.makeText(context, "Chit $name added!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please fill in Name and Value", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronRed),
                            modifier = Modifier.testTag("save_chit_btn")
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // RECORD MONTHLY AUCTION BID DIALOG
    if (showAuctionDialog && selectedChitForAuction != null) {
        val chit = selectedChitForAuction!!
        val m = selectedInstallmentIndex
        
        var winningBidStr by remember { 
            mutableStateOf(chit.getBidForInstallment(m).toString().removeSuffix(".0")) 
        }
        var isThisUserWonMonth by remember { 
            mutableStateOf(chit.isWonByUser && chit.userWonMonth == m) 
        }

        Dialog(onDismissRequest = { showAuctionDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("record_bid_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Record Auction Month $m 📈",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuspiciousGreen
                    )
                    Text(
                        text = "Enter the winning discount bid for installment Month $m. The commission of ${chit.companyCommissionPercent}% (${String.format("₹%,.0f", chit.getCompanyCommission())}) is automatically factored in for dividend sharing.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = winningBidStr,
                        onValueChange = { winningBidStr = it },
                        label = { Text("Winning Bid Discount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("winning_bid_field")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { isThisUserWonMonth = !isThisUserWonMonth }
                    ) {
                        Checkbox(
                            checked = isThisUserWonMonth,
                            onCheckedChange = { isThisUserWonMonth = it }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Did YOU win this month's auction?", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAuctionDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val bid = winningBidStr.toDoubleOrNull() ?: 0.0
                                
                                val currentBids = chit.getAuctionBids().toMutableList()
                                while (currentBids.size < chit.totalMonths) {
                                    currentBids.add(0.0)
                                }
                                currentBids[m - 1] = bid

                                var updatedChit = chit.copy(
                                    auctionBidsJson = currentBids.toString()
                                )

                                if (isThisUserWonMonth) {
                                    updatedChit = updatedChit.copy(
                                        isWonByUser = true,
                                        userWonMonth = m,
                                        userWonAmount = chit.chitValue - bid
                                    )
                                } else if (chit.userWonMonth == m) {
                                    // User previously won this month but has cleared the box
                                    updatedChit = updatedChit.copy(
                                        isWonByUser = false,
                                        userWonMonth = 0,
                                        userWonAmount = 0.0
                                    )
                                }

                                viewModel.updateChitFund(updatedChit)
                                showAuctionDialog = false
                                Toast.makeText(context, "Bid recorded for month $m!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronRed),
                            modifier = Modifier.testTag("save_bid_btn")
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
fun MuhurthamFinderScreen(
    viewModel: TamilCalendarViewModel,
    onBack: () -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(2)) }
    var selectedEventIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val eventTypes = remember {
        listOf(
            Triple("General Auspicious / பொது", "GENERAL", "Auspicious standard days suitable for routine deeds"),
            Triple("Marriage / திருமணம்", "MARRIAGE", "Propitious dates optimized to carry out weddings"),
            Triple("Housewarming / கிரகப்பிரவேசம்", "HOUSEWARMING", "Strong days for entry, griha-pravesha ceremonies"),
            Triple("New Business / தொழில் துவக்கம்", "BUSINESS", "Auspicious weekdays to launch ventures")
        )
    }

    // Pickers
    val startPicker = DatePickerDialog(
        context,
        { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) },
        startDate.year, startDate.monthValue - 1, startDate.dayOfMonth
    )
    val endPicker = DatePickerDialog(
        context,
        { _, y, m, d -> endDate = LocalDate.of(y, m + 1, d) },
        endDate.year, endDate.monthValue - 1, endDate.dayOfMonth
    )

    // Calculate dates
    val matchedDates = remember(startDate, endDate, selectedEventIndex) {
        val list = mutableListOf<Pair<LocalDate, TamilDate>>()
        if (endDate.isBefore(startDate)) return@remember list
        
        var d = startDate
        val limit = endDate.plusDays(1)
        while (d.isBefore(limit)) {
            val tDate = TamilCalendarHelper.getTamilDate(d.year, d.monthValue, d.dayOfMonth)
            
            // Exclude inauspicious phases (Amavasai/Pradosham) and severe Tithis (Ashtami / Navami)
            val isInauspiciousPhase = tDate.isAmavasai || tDate.isPradosham || tDate.tithi == 8 || tDate.tithi == 23 || tDate.tithi == 9 || tDate.tithi == 24
            
            if (!isInauspiciousPhase) {
                // Apply specific filter rules based on selection
                val isSuited = when (eventTypes[selectedEventIndex].second) {
                    "MARRIAGE" -> {
                        // Ideal months (Chithirai, Vaigasi, Avani, Thai, Panguni)
                        tDate.tamilMonthIndex in listOf(1, 2, 5, 10, 11, 12)
                    }
                    "HOUSEWARMING" -> {
                        // Propitious weekdays
                        d.dayOfWeek.value in listOf(1, 3, 4, 5) // Mon, Wed, Thu, Fri
                    }
                    "BUSINESS" -> {
                        // Propitious business starts
                        d.dayOfWeek.value in listOf(1, 3, 5) // Mon, Wed, Fri
                    }
                    else -> true // General auspicious
                }
                if (isSuited) {
                    list.add(Pair(d, tDate))
                }
            }
            d = d.plusDays(1)
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("muhurtham_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text("Muhurtham Finder 🔍", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SaffronRed)
        }

        Spacer(Modifier.height(16.dp))

        // Range Selector Inputs
        Text("Select Date Range:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { startPicker.show() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black)
            ) {
                Column {
                    Text("Start Date", fontSize = 10.sp, color = Color.Gray)
                    Text("$startDate", fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = { endPicker.show() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black)
            ) {
                Column {
                    Text("End Date", fontSize = 10.sp, color = Color.Gray)
                    Text("$endDate", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Segmented tabs for Event selection
        Text("Event Type:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        ScrollableTabRow(
            selectedTabIndex = selectedEventIndex,
            edgePadding = 0.dp,
            containerColor = Color.White,
            contentColor = SaffronRed
        ) {
            eventTypes.forEachIndexed { i, triple ->
                Tab(
                    selected = selectedEventIndex == i,
                    onClick = { selectedEventIndex = i },
                    text = { Text(triple.first.split("/").last().trim(), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Text(
            text = eventTypes[selectedEventIndex].third,
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Matched Auspicious Days (${matchedDates.size}):",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = AuspiciousGreen
        )

        Spacer(Modifier.height(8.dp))

        if (matchedDates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "No auspicious days matched the selected filters.\nTry widening the date range query.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matchedDates) { pair ->
                    val dayD = pair.first
                    val tD = pair.second
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F9F4))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dayD.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "Auspicious",
                                        fontSize = 10.sp,
                                        color = AuspiciousGreen,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Tamil Date: ${tD.tamilMonthNameTamil} ${tD.tamilDay} (Month ${tD.tamilMonthIndex})",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            val star = tD.nakshatra.split(" / ").lastOrNull() ?: tD.nakshatra
                            Text(
                                text = "Star: $star | Tithi: ${tD.tithi}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniMonthCard(
    year: Int,
    month: Int,
    onMonthSelected: (LocalDate) -> Unit
) {
    val targetMonthDate = remember(year, month) { LocalDate.of(year, month, 1) }
    val daysInMonth = targetMonthDate.lengthOfMonth()
    val gridFirstDay = targetMonthDate.dayOfWeek.value % 7 // Sunday = 0, Monday = 1... Sunday is 7, 7 % 7 is 0. Mon is 1 % 7 is 1, correct!
    val totalCells = gridFirstDay + daysInMonth
    val rows = (totalCells + 6) / 7

    val monthData = remember(year, month) {
        val lists = mutableListOf<TamilDate>()
        var festCount = 0
        for (day in 1..daysInMonth) {
            val tDate = TamilCalendarHelper.getTamilDate(year, month, day)
            lists.add(tDate)
            val isFestival = tDate.festivalName != null && tDate.festivalName != "Pournami / பௌர்ணமி" && tDate.festivalName != "Amavasai / அமாவாசை"
            if (isFestival) festCount++
        }
        Pair(lists, festCount)
    }
    val daysList = monthData.first
    val festivalCount = monthData.second

    val englishMonthName = targetMonthDate.format(DateTimeFormatter.ofPattern("MMMM"))
    val tamilMonthName = daysList.getOrNull(14)?.tamilMonthNameTamil ?: ""

    Card(
        modifier = Modifier
            .padding(2.dp)
            .clickable { onMonthSelected(targetMonthDate) }
            .testTag("mini_month_${month}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = englishMonthName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = tamilMonthName, fontSize = 8.sp, color = Color(0xFF6D4C41), fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Mini 7-column calendar
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                for (row in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - gridFirstDay + 1
                            
                            Box(
                                modifier = Modifier.size(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..daysInMonth) {
                                    val tDate = daysList[dayNumber - 1]
                                    val cellDate = LocalDate.of(year, month, dayNumber)
                                    val isToday = (cellDate == LocalDate.now())
                                    val isFestival = tDate.festivalName != null && tDate.festivalName != "Pournami / பௌர்ணமி" && tDate.festivalName != "Amavasai / அமாவாசை"
                                    
                                    when {
                                        isToday -> {
                                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF1565C0)))
                                        }
                                        tDate.isPournami -> {
                                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFFFD54F)))
                                        }
                                        tDate.isAmavasai -> {
                                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1A237E)))
                                        }
                                        isFestival -> {
                                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFD32F2F)))
                                        }
                                        else -> {
                                            Text(text = dayNumber.toString(), fontSize = 5.sp, color = Color.Gray, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            // Festival count badge
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFEBEE),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "$festivalCount Fest",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun YearlyViewScreen(
    currentYear: Int,
    onMonthSelected: (LocalDate) -> Unit,
    onBack: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentYear) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("yearly_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { selectedYear-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Year")
                }
                Text(
                    text = "$selectedYear",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                IconButton(onClick = { selectedYear++ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Year")
                }
            }
            
            Box(modifier = Modifier.size(48.dp)) // spacer
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Month cards grid layout
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(12) { index ->
                MiniMonthCard(
                    year = selectedYear,
                    month = index + 1,
                    onMonthSelected = onMonthSelected
                )
            }
        }
    }
}
