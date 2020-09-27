package com.aminook.tunemyday.framework.datasource.cache.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aminook.tunemyday.business.domain.model.SubTodo
import com.aminook.tunemyday.framework.datasource.cache.model.*


@Database(
    entities = [
        ToDoEntity::class,
        AlarmEntity::class,
        ProgramEntity::class,
        ScheduleEntity::class,
        ToDoScheduleMapper::class,
        SubTodoEntity::class
    ],
    version = 4,
    exportSchema = false
)

abstract class ScheduleDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun programDao(): ProgramDao
    abstract fun alarmDao(): AlarmDao
    abstract fun todoDao(): ToDoDao
    abstract fun subTodoDao():SubTodoDao
    abstract fun todoScheduleDao(): ToDoScheduleDao


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