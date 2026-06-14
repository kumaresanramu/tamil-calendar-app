package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relationship: String,
    val birthDateEnglish: String, // format "yyyy-MM-dd"
    val birthNakshatra: String,
    val birthTamilMonth: String,
    val birthTamilDate: Int,
    val phoneNumber: String = "",
    val remindDaysBefore: Int = 1
)
