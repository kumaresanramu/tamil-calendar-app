package com.example

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val type: String, // "TAMIL", "ENGLISH", "MOON", "CUSTOM"
    val isSystemReminder: Boolean = false,
    
    // Tamil date (needed for TAMIL type)
    val tamilMonth: Int? = null, // 1 to 12
    val tamilDate: Int? = null, // 1 to 32
    
    // English date (needed for ENGLISH type)
    val englishDayOfMonth: Int? = null, // 1 to 31
    
    // Moon phase (needed for MOON type)
    val moonPhaseType: String? = null, // "POURNAMI", "AMAVASAI", "EKADASHI", "PRADOSHAM"
    
    // Custom Gregorian date (needed for CUSTOM type)
    val customGregorianDate: String? = null, // "yyyy-MM-dd"
    
    val reminderTime: String = "08:00", // "HH:mm"
    val repeatSetting: String = "YEARLY", // "DAILY", "WEEKLY", "MONTHLY", "YEARLY", "ONE_TIME"
    val remindBeforeDays: Int = 0, // 0, 1, or 2 days
    val isEnabled: Boolean = true,
    
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val repeatType: String = "ONCE" // ONCE / DAILY / WEEKLY / MONTHLY / YEARLY
)

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Int): Reminder?

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0")
    suspend fun getActiveReminders(): List<Reminder>

    @Query("UPDATE reminders SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun markAsCompletedInternal(id: Int, completedAt: Long)

    @Transaction
    suspend fun markAsCompleted(id: Int) {
        markAsCompletedInternal(id, System.currentTimeMillis())
    }

    @Query("DELETE FROM reminders WHERE id = :id AND isCompleted = 1")
    suspend fun deleteCompleted(id: Int)

    @Query("SELECT COUNT(*) FROM reminders WHERE isSystemReminder = 0")
    suspend fun getCustomReminderCount(): Int
}

@Database(entities = [Reminder::class], version = 3, exportSchema = false)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "tamil_calendar_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate default festivals in database on first launch
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.reminderDao()
                        
                        // 1. Thai Pongal
                        dao.insertReminder(
                            Reminder(
                                title = "Thai Pongal",
                                description = "Traditional Tamil harvest festival starting the Thai month.",
                                type = "TAMIL",
                                tamilMonth = 10,
                                tamilDate = 1,
                                repeatSetting = "YEARLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )
                        
                        // 2. Tamil New Year - Puthandu
                        dao.insertReminder(
                            Reminder(
                                title = "Tamil New Year - Puthandu",
                                description = "The start of the Tamil year on Chithirai 1.",
                                type = "TAMIL",
                                tamilMonth = 1,
                                tamilDate = 1,
                                repeatSetting = "YEARLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )

                        // 3. Aadi 1 - Aadi Pirappu
                        dao.insertReminder(
                            Reminder(
                                title = "Aadi 1 - Aadi Pirappu",
                                description = "First day of the holy month Aadi.",
                                type = "TAMIL",
                                tamilMonth = 4,
                                tamilDate = 1,
                                repeatSetting = "YEARLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )

                        // 4. Aadi 18 - Aadi Perukku
                        dao.insertReminder(
                            Reminder(
                                title = "Aadi 18 - Aadi Perukku",
                                description = "Tamil festival of water/prosperity (Aadi 18).",
                                type = "TAMIL",
                                tamilMonth = 4,
                                tamilDate = 18,
                                repeatSetting = "YEARLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )

                        // 5. Karthikai Deepam
                        dao.insertReminder(
                            Reminder(
                                title = "Karthikai Deepam",
                                description = "Festival of thousands of lights under Karthikai Full Moon.",
                                type = "MOON",
                                moonPhaseType = "POURNAMI",
                                tamilMonth = 8, // Karthikai month
                                repeatSetting = "YEARLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )

                        // 6. Diwali
                        dao.insertReminder(
                            Reminder(
                                title = "Diwali",
                                description = "Festival of lights on Aippasi month New Moon.",
                                type = "MOON",
                                moonPhaseType = "AMAVASAI",
                                tamilMonth = 7, // Aippasi month
                                repeatSetting = "YEARLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )

                        // 7. Pournami - Full Moon
                        dao.insertReminder(
                            Reminder(
                                title = "Pournami - Full Moon",
                                description = "Monthly Full Moon day reminders.",
                                type = "MOON",
                                moonPhaseType = "POURNAMI",
                                repeatSetting = "MONTHLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )

                        // 8. Amavasai - New Moon
                        dao.insertReminder(
                            Reminder(
                                title = "Amavasai - New Moon",
                                description = "Monthly New Moon day reminders.",
                                type = "MOON",
                                moonPhaseType = "AMAVASAI",
                                repeatSetting = "MONTHLY",
                                remindBeforeDays = 0,
                                isEnabled = true,
                                isSystemReminder = true
                            )
                        )

                        // In case alarms need to be initialized immediately
                        ReminderScheduler.scheduleNextAlarm(context)
                    }
                }
            }
        }
    }
}
