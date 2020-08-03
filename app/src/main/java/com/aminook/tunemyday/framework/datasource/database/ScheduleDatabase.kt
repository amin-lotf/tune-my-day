package com.aminook.tunemyday.framework.datasource.database

import android.content.Context
import android.graphics.Color
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aminook.tunemyday.framework.datasource.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(
    entities = [
        ToDoEntity::class,
        AlarmEntity::class,
        ProgramEntity::class,
        ScheduleEntity::class,
        ToDoScheduleMapper::class
    ],
    version = 2,
    exportSchema = false
)

abstract class ScheduleDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun programDao():ProgramDao
    abstract fun alarmDao():AlarmDao
    abstract fun todoDao():ToDoDao
    abstract fun todoScheduleDao():ToDoScheduleDao


    companion object {

        @Volatile
        private var INSTANCE: ScheduleDatabase? = null

        fun getDatabase(
            context: Context

        ): ScheduleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleDatabase::class.java,
                    "schedule_database"
                )
                    //TODO(REMOVE THE FALLBACK)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }

}