package com.example

import java.time.LocalDate
import kotlin.math.floor

data class TamilDate(
    val englishYear: Int,
    val englishMonth: Int, // 1-12
    val englishDay: Int,
    val tamilYearName: String,
    val tamilMonthIndex: Int, // 1-12
    val tamilMonthNameTamil: String,
    val tamilMonthNameEnglish: String,
    val tamilDay: Int, // 1-32
    val isPournami: Boolean,
    val isAmavasai: Boolean,
    val isEkadashi: Boolean,
    val isPradosham: Boolean,
    val festivalName: String?,
    val lunarAge: Double,
    val tithi: Int,
    val nakshatra: String
)

object TamilCalendarHelper {

    val nakshatrasEnglish = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashirsha", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    val nakshatrasTamil = listOf(
        "அசுவினி", "பரணி", "கார்த்திகை", "ரோகிணி", "மிருகசீரிடம்", "திருவாதிரை",
        "புனர்பூசம்", "பூசம்", "ஆயில்யம்", "மகம்", "பூரம்", "உத்திரம்",
        "அஸ்தம்", "சித்திரை", "சுவாதி", "விசாகம்", "அனுஷம்", "கேட்டை",
        "மூலம்", "பூராடம்", "உத்திராடம்", "திருவோணம்", "அவிட்டம்", "சதயம்",
        "பூரட்டாதி", "உத்திரட்டாதி", "ரேவதி"
    )

    val tamilMonthsTamil = listOf(
        "சித்திரை", "வைகாசி", "ஆனி", "ஆடி", "ஆவணி", "புரட்டாசி",
        "ஐப்பசி", "கார்த்திகை", "மார்கழி", "தை", "மாசி", "பங்குனி"
    )

    val tamilMonthsEnglish = listOf(
        "Chithirai", "Vaikaasi", "Aani", "Aadi", "Aavani", "Purattaasi",
        "Aippasi", "Karthikai", "Margazhi", "Thai", "Maasi", "Panguni"
    )

    private val tamilYearsEnglish = listOf(
        "Prabhava", "Vibhava", "Sukla", "Pramodoota", "Prachorpathi", "Angirasa", "Srimukha", "Bhava", "Yuva", "Dhatu",
        "Easwara", "Bahudhanya", "Pramathi", "Vikrama", "Vishu", "Chitrabanu", "Subhanu", "Tharanathi", "Parthiba", "Viyaya",
        "Sarvajithu", "Sarvadhari", "Virothi", "Vikruthi", "Kara", "Nandhana", "Vijaya", "Jaya", "Manmatha", "Thunmuki",
        "Hevilambi", "Vilambi", "Vikari", "Sarvari", "Plava", "Subakrithu", "Sobakrithu", "Krodhi", "Visvavasu", "Parabhava",
        "Plavanga", "Keelaka", "Saumya", "Sadharana", "Virodhikruthi", "Paridhabhi", "Pramadhisa", "Anandha", "Rakshasa", "Nala",
        "Pingala", "Kalayukthi", "Siddharthi", "Raudhri", "Durmathi", "Dundubhi", "Rudhirothkari", "Raktakshi", "Krodhana", "Akshaya"
    )

    private val tamilYearsTamil = listOf(
        "பிரபவ", "விபவ", "சுக்ல", "பிரமோதூத", "பிரஜோத்பத்தி", "ஆங்கீரச", "ஸ்ரீமுக", "பவ", "யுவ", "தாது",
        "ஈஸ்வர", "பஹுதான்ய", "பிரமாதி", "விக்ரம", "விஷு", "சித்ரபானு", "சுபானு", "தாரண", "பார்த்திப", "விய",
        "ஸர்வஜித்", "ஸர்வதாரி", "விரோதி", "விக்ருதி", "கர", "நந்தன", "விஜய", "ஜய", "மன்மத", "துன்முகி",
        "ஹேவிளம்பி", "விளம்பி", "விகாரி", "சார்வரி", "ப்லவ", "சுபகிருது", "சோபகிருது", "க்ரோதி", "விஸ்வாவஸு", "பரபவ",
        "ப்லவங்க", "கீலக", "ஸௌம்ய", "ஸாதாரண", "விரோதிலிருது", "பரிதாபி", "பிரமாதீச", "ஆனந்த", "ராக்ஷஸ", "நள",
        "பிங்கள", "காளயுக்தி", "ஸித்தார்த்தி", "ரௌத்ரி", "துர்மதி", "துந்துபி", "ருத்ரோத்காரி", "ரக்தாக்ஷி", "க்ரோதன", "அக்ஷய"
    )

    // Accurate Tamil month starting days (Sankranti days) for years 2024 to 2030
    fun getSankrantiDay(year: Int, month: Int): Int {
        return when (month) {
            1 -> if (year == 2028) 15 else 14 // Thai starts
            2 -> 13 // Maasi starts
            3 -> if (year == 2027) 15 else 14 // Panguni starts
            4 -> 14 // Chithirai starts (Puthandu)
            5 -> if (year == 2024 || year == 2028) 14 else 15 // Vaikaasi starts
            6 -> if (year == 2024 || year == 2028) 14 else 15 // Aani starts
            7 -> if (year == 2026 || year == 2027) 17 else 16 // Aadi starts
            8 -> if (year == 2024 || year == 2028) 16 else 17 // Aavani starts
            9 -> if (year == 2024 || year == 2028) 16 else 17 // Purattaasi starts
            10 -> if (year == 2026 || year == 2027) 18 else 17 // Aippasi starts
            11 -> 16 // Karthikai starts
            12 -> if (year == 2024 || year == 2028) 15 else 16 // Margazhi starts
            else -> 14
        }
    }

    private fun getJulianDate(year: Int, month: Int, day: Int, hour: Double = 12.0): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = floor(a / 4.0)
        val c = 2 - a + b
        val e = floor(365.25 * (y + 4716))
        val f = floor(30.6001 * (m + 1))
        return c + (day + hour / 24.0) + e + f - 1524.5
    }

    // Calculates the lunar age (0 to 29.53) and tithi (1 to 30)
    fun getLunarDetails(year: Int, month: Int, day: Int): Pair<Double, Int> {
        val jd = getJulianDate(year, month, day, 12.0)
        // Known New Moon near Jan 6, 2000 @ 18:14 UTC -> JD = 2451550.26
        val daysSinceNewMoon = jd - 2451550.26
        val synodicMonth = 29.530588853
        val cycles = daysSinceNewMoon / synodicMonth
        var phaseFraction = cycles - floor(cycles)
        if (phaseFraction < 0) phaseFraction += 1.0

        val lunarAge = phaseFraction * synodicMonth
        val tithi = floor(phaseFraction * 30).toInt() + 1
        return Pair(lunarAge, tithi)
    }

    fun toTamilNumerals(num: Int): String {
        if (num < 1 || num > 99) return num.toString()
        val digits = listOf("", "௧", "௨", "௩", "௪", "௫", "௬", "௭", "௮", "௯")
        val ten = "௰"
        return when {
            num in 1..9 -> digits[num]
            num == 10 -> ten
            num in 11..19 -> ten + digits[num % 10]
            num in 20..29 -> digits[2] + ten + (if (num % 10 != 0) digits[num % 10] else "")
            num in 30..39 -> digits[3] + ten + (if (num % 10 != 0) digits[num % 10] else "")
            else -> num.toString()
        }
    }

    fun getTamilDate(year: Int, month: Int, day: Int): TamilDate {
        val targetDate = LocalDate.of(year, month, day)
        val sankrantiSelected = getSankrantiDay(year, month)

        val tamilMonthIndex: Int
        val tamilDay: Int
        val isBeforeSankranti = day < sankrantiSelected

        if (!isBeforeSankranti) {
            // New Tamil Month starts in this Gregorian month
            // Tamil Month Index mapping: Apr starts Month 1 (Chithirai)
            tamilMonthIndex = when (month) {
                4 -> 1  // Chithirai
                5 -> 2  // Vaikaasi
                6 -> 3  // Aani
                7 -> 4  // Aadi
                8 -> 5  // Aavani
                9 -> 6  // Purattaasi
                10 -> 7 // Aippasi
                11 -> 8 // Karthikai
                12 -> 9 // Margazhi
                1 -> 10 // Thai
                2 -> 11 // Maasi
                3 -> 12 // Panguni
                else -> 1
            }
            tamilDay = day - sankrantiSelected + 1
        } else {
            // Prior to Sankranti, continuing previous Tamil Month
            val prevLocalDate = targetDate.minusMonths(1)
            val prevYear = prevLocalDate.year
            val prevMonth = prevLocalDate.monthValue

            tamilMonthIndex = when (month) {
                4 -> 12 // Panguni (starts in Mar)
                5 -> 1  // Chithirai (starts in Apr)
                6 -> 2  // Vaikaasi (starts in May)
                7 -> 3  // Aani (starts in Jun)
                8 -> 4  // Aadi (starts in Jul)
                9 -> 5  // Aavani (starts in Aug)
                10 -> 6 // Purattaasi (starts in Sep)
                11 -> 7 // Aippasi (starts in Oct)
                12 -> 8 // Karthikai (starts in Nov)
                1 -> 9  // Margazhi (starts in Dec)
                2 -> 10 // Thai (starts in Jan)
                3 -> 11 // Maasi (starts in Feb)
                else -> 12
            }

            val prevSankranti = getSankrantiDay(prevYear, prevMonth)
            val daysInPrevMonth = prevLocalDate.lengthOfMonth()
            val daysAfterPrevSankranti = daysInPrevMonth - prevSankranti + 1
            tamilDay = daysAfterPrevSankranti + day
        }

        // Get Tamil Year Name
        // Chithirai 1 is when Tamil New Year starts.
        // If we are in Panguni or before Chithirai 1 of the current Gregorian year, we use the previous cycle code.
        val isBeforeChithirai1 = (month < 4) || (month == 4 && day < getSankrantiDay(year, 4))
        var yearCycleIndex = (year - 1987) % 60
        if (isBeforeChithirai1) {
            yearCycleIndex = (year - 1 - 1987) % 60
        }
        if (yearCycleIndex < 0) yearCycleIndex += 60

        val tamilYearName = "${tamilYearsEnglish[yearCycleIndex]} / ${tamilYearsTamil[yearCycleIndex]}"

        // Lunar details
        val (lunarAge, tithi) = getLunarDetails(year, month, day)

        // Calculate Nakshatra (based on Moon's sidereal position)
        val jd = getJulianDate(year, month, day, 12.0)
        val daysSinceEpoch = jd - 2451550.26
        val siderealMonth = 27.321661
        val cyclesSidereal = daysSinceEpoch / siderealMonth
        var fracSidereal = cyclesSidereal - floor(cyclesSidereal)
        if (fracSidereal < 0) fracSidereal += 1.0
        val nakshatraIdx = (floor(fracSidereal * 27).toInt() % 27 + 27) % 27
        val nakshatra = "${nakshatrasEnglish[nakshatraIdx]} / ${nakshatrasTamil[nakshatraIdx]}"

        // Identify lunar phases (tithis)
        // Check surrounding days to see which day is the absolute peak of Pournami / Amavasai
        // or simple representation based on day's tithi (Tithi 15 = Pournami, Tithi 30 = Amavasai)
        val isPournami = tithi == 15
        val isAmavasai = tithi == 30
        val isEkadashi = tithi == 11 || tithi == 26
        val isPradosham = tithi == 13 || tithi == 28

        // Festivals
        var festivalName: String? = null
        if (tamilMonthIndex == 10 && tamilDay == 1) {
            festivalName = "Thai Pongal / தைப்பொங்கல்"
        } else if (tamilMonthIndex == 1 && tamilDay == 1) {
            festivalName = "Tamil New Year / தமிழ்ப்புத்தாண்டு"
        } else if (tamilMonthIndex == 4 && tamilDay == 1) {
            festivalName = "Aadi 1 / ஆடி மாதப்பிறப்பு"
        } else if (tamilMonthIndex == 4 && tamilDay == 18) {
            festivalName = "Aadi Perukku / ஆடிப்பெருக்கு"
        } else if (tamilMonthIndex == 8 && isPournami) {
            festivalName = "Karthikai Deepam / கார்த்திகை தீபம்"
        } else if (tamilMonthIndex == 7 && isAmavasai) {
            festivalName = "Diwali / தீபாவளி"
        } else if (isPournami) {
            festivalName = "Pournami / பௌர்ணமி"
        } else if (isAmavasai) {
            festivalName = "Amavasai / அமாவாசை"
        }

        return TamilDate(
            englishYear = year,
            englishMonth = month,
            englishDay = day,
            tamilYearName = tamilYearName,
            tamilMonthIndex = tamilMonthIndex,
            tamilMonthNameTamil = tamilMonthsTamil[tamilMonthIndex - 1],
            tamilMonthNameEnglish = tamilMonthsEnglish[tamilMonthIndex - 1],
            tamilDay = tamilDay,
            isPournami = isPournami,
            isAmavasai = isAmavasai,
            isEkadashi = isEkadashi,
            isPradosham = isPradosham,
            festivalName = festivalName,
            lunarAge = lunarAge,
            tithi = tithi,
            nakshatra = nakshatra
        )
    }

    // Helper to find the Gregorian Date for a specific Tamil Month and Tamil Day in a given Gregorian Year
    fun findGregorianDate(gregorianYear: Int, tamilMonthIndex: Int, tamilDay: Int): LocalDate? {
        // Iterate through all days of the Gregorian year to find the match.
        // It's super fast, absolute O(366) operations max, completely offline and bulletproof.
        var date = LocalDate.of(gregorianYear, 1, 1)
        val lastDay = LocalDate.of(gregorianYear, 12, 31)
        while (!date.isAfter(lastDay)) {
            val tDate = getTamilDate(date.year, date.monthValue, date.dayOfMonth)
            if (tDate.tamilMonthIndex == tamilMonthIndex && tDate.tamilDay == tamilDay) {
                return date
            }
            date = date.plusDays(1)
        }
        return null
    }

    // Lookup next Moon Phase Date
    fun findNextMoonPhaseDate(startDate: LocalDate, moonPhaseType: String, offsetDays: Int = 0): LocalDate {
        var date = startDate
        // Scan up to 45 days into the future
        for (i in 0..45) {
            val tDate = getTamilDate(date.year, date.monthValue, date.dayOfMonth)
            val match = when (moonPhaseType) {
                "POURNAMI" -> tDate.isPournami
                "AMAVASAI" -> tDate.isAmavasai
                "EKADASHI" -> tDate.isEkadashi
                "PRADOSHAM" -> tDate.isPradosham
                else -> false
            }
            if (match) {
                return date.minusDays(offsetDays.toLong())
            }
            date = date.plusDays(1)
        }
        return startDate // fallback
    }
}
