package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Contribution::class,
        Payout::class,
        Announcement::class,
        WelfareRequest::class,
        Reminder::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WelfareDatabase : RoomDatabase() {
    abstract fun welfareDao(): WelfareDao

    companion object {
        @Volatile
        private var INSTANCE: WelfareDatabase? = null

        fun getDatabase(context: Context): WelfareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WelfareDatabase::class.java,
                    "welfare_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
