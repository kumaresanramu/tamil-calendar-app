# 📅 Tamil Calendar (தமிழ் நாட்காட்டி)

An elegant, native, and completely offline-first Tamil Calendar application built for Android with modern Material Design 3 and Jetpack Compose. Designed to bring the traditional Tamil panchangam (அல்மனாக்) directly into your pocket with robust local tools.

---

## 🚀 Instant Download & Sideloading
The latest release-ready Android application package is compiled and available for immediate installation. Depending on your editor's sidebar filtering, you can access the APK from either of the following paths:

*   📂 **Root Releases Folder:** [releases/latest/TamilCalendar.apk](./releases/latest/TamilCalendar.apk)
*   📂 **Inside App Module Folder:** [app/releases/latest/TamilCalendar.apk](./app/releases/latest/TamilCalendar.apk)

*Sideloading Instructions:*
1. Locate and download the `TamilCalendar.apk` file from either of the paths above.
2. Transfer it to your target Android device (or view/interact with the running build directly in your Web Emulator).
3. If prompted by the operating system, enable "Install from Unknown Sources" in your Android system security settings.
4. Open the file and click **Install**.

---

## 🌟 Key Features

### 1. Traditional Almanac & Calendar (நாள்காட்டி)
*   **Dual Calendars:** Perfectly pairs the Gregorian Calendar with the Tamil Calendar (Chithirai/சித்திரை to Panguni/பங்குனி).
*   **Auspicious & Astronomical Timings:** Displays Nakshatra (நட்சத்திரம்), Tithi (திதி), Yoga, and Karana for any selected date.
*   **Panchangam Engine:** Computes Rahu Kalam (ராகு காலம்), Yamagandam (எமகண்டம்), and Gulika Kalam (குளிகை காலம்) dynamically based on local sunrise and sunset timings.
*   **Special Days & Festivals:** Fully highlights monthly auspicious events such as Pournami (Full Moon), Amavasai (New Moon), Ekadashi, and Pradosham.

### 2. Multi-Criteria Muhurtham Finder (முகூர்த்தம் தேடி)
*   Search and filter dynamic date ranges to discover highly auspicious marriage, housewarming, or business commencement hours (ஆத்த காலங்கள்) without requiring active internet connectivity.
*   Displays **Abhijit Muhurtham** (அபிஜித் முகூர்த்தம்) and rules out overlaps with adverse planetary hours.

### 3. Smart Local Reminders (நினைவூட்டல்கள்)
*   Schedule alerts for specific days or recurring holy intervals (e.g., automated alerts 1 day before every Pradosham or Ekadashi).
*   Powered by modern Android **AlarmManager** and local wake locks for highly accurate, battery-conscious local scheduling.

### 4. Chit Fund & Dividend Tracker (சீட்டு சேமிப்பு)
*   Keep your localized savings organized! Set monthly contribution plans, track monthly auction winners, calculate net dividend returns, and record your payout months.
*   Computes compound ROI and total contributions dynamically.

### 5. Family Birthday Planner (பிறந்தநாள் கேலெண்டர்)
*   Register dear ones with their Gregorian and computed astronomical Nakshatra.
*   Issues warm notifications on their birthdays automatically, keeping families close.

---

## 🛠️ Devops & Resiliency Features

This app is engineered with high production-grade stability and modern Android safety guardrails:

### Exception Protection & Diagnostics Report
*   **Crash Safeguard Matrix:** Configured with a default Uncaught Exception Handler that intercepts random OS runtime faults on a real device.
*   **System Notifications on Crash:** Instead of silently closing, the app instantly pops a high-priority system notification indicating it has safely recovered.
*   **Interactive Diagnostic Modal:** On relaunch, an interactive diagnostics report overlay allows the user to immediately review the exact stack trace and copy it onto their clipboard with a single tap for remote developer diagnostics.

### Android 12+ Background Stability & Permissions
*   **Exact Alarm Scheduling (`SCHEDULE_EXACT_ALARM`):** Requests system permission gracefully on launch to prevent modern Android restrictions (introduced in Android 12) from blocking background reminders.
*   **Instant Notification Prompts:** Prompts for Android 13+ `POST_NOTIFICATIONS` runtime authorization during onboarding so you never miss a holy moon phase event.

### Production Release Protections
*   **Large Heap Memory Allocation (`largeHeap="true"`):** Enables expanded RAM availability inside `AndroidManifest.xml` to prevent out-of-memory overhead during extensive panchangam date generations.
*   **Engineered Proguard/R8 Rules:** Custom obfuscation configurations directly preserve Room Database classes, JSON serialization adapters, desugared time reflection parameters, and helper models ensuring 100% stable production builds.

---

## 🏗️ Technical Architecture

*   **UI Layer:** 100% declarative UI built with **Jetpack Compose** and official **Material Design 3 (M3)** design tokens.
*   **Data Persistence:** local SQLite querying handled by **Room Database** with KSP-supported compile-time verification.
*   **Architecture Pattern:** Clean MVVM (Model-View-ViewModel) decoupling data flows from visual view states via thread-safe Kotlin Coroutines and asynchronous StateFlow stream publishers.
*   **Core Math:** Astronomical algorithms to compute Julian date calendars, Moon age phases, and sidereal Nakshatras offline.
