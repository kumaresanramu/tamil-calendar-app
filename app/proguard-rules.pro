# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Room database, entities, and DAO classes
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.ReminderDatabase { *; }
-keep class com.example.ReminderDao { *; }
-keep class com.example.Reminder { *; }
-keep class com.example.FamilyMember { *; }
-keep class com.example.ChitFund { *; }
-dontwarn androidx.room.**

# Keep our models and helper classes
-keep class com.example.TamilDate { *; }
-keep class com.example.TamilCalendarHelper { *; }

# Keep java.time structures for desugared Java 8 LocalDate usages
-keep class java.time.** { *; }
-dontwarn java.time.**
