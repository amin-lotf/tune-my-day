package com.aminook.tunemyday.di

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.FragmentFactory
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.data.cache.ScheduleRepositoryImpl
import com.aminook.tunemyday.business.domain.model.Color
import com.aminook.tunemyday.framework.datasource.cache.database.ScheduleDatabase
import com.aminook.tunemyday.framework.presentation.common.AppFragmentFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
abstract class AppModule {

    companion object {


        @Singleton
        @Provides
        fun provideAppFragmentFactory(): FragmentFactory {
            return AppFragmentFactory()
        }

        @Singleton
        @Provides
        fun provideScheduleDatabase(@ApplicationContext application: Context) =
            ScheduleDatabase.getDatabase(application)


        @Singleton
        @Provides
        fun provideProgramDao(scheduleDatabase: ScheduleDatabase) =
            scheduleDatabase.programDao()

        @Singleton
        @Provides
        fun provideAlarmDao(scheduleDatabase: ScheduleDatabase) =
            scheduleDatabase.alarmDao()

        @Singleton
        @Provides
        fun provideToDoDao(scheduleDatabase: ScheduleDatabase) =
            scheduleDatabase.todoDao()

//        @Singleton
//        @Provides
//        fun provideSubToDoDao(scheduleDatabase: ScheduleDatabase) =
//            scheduleDatabase.subTodoDao()

        @Singleton
        @Provides
        fun provideScheduleDao(scheduleDatabase: ScheduleDatabase) =
            scheduleDatabase.scheduleDao()

        @Singleton
        @Provides
        fun provideTodoScheduleDao(scheduleDatabase: ScheduleDatabase) =
            scheduleDatabase.todoScheduleDao()

        @Singleton
        @Provides
        fun provideLabelColor(@ApplicationContext context: Context):List<Color> {
            val white=ContextCompat.getColor(context, R.color.colorWhite)
            val black=ContextCompat.getColor(context, R.color.colorDark)
            return mutableListOf(
                Color(ContextCompat.getColor(context, R.color.label1), true,black),
                Color(ContextCompat.getColor(context, R.color.label2), false,black),
                Color(ContextCompat.getColor(context, R.color.label3), false,black),
                Color(ContextCompat.getColor(context, R.color.label4), false,black),
                Color(ContextCompat.getColor(context, R.color.label5), false,black),
                Color(ContextCompat.getColor(context, R.color.label6), false,black),
                Color(ContextCompat.getColor(context, R.color.label7), false,black),
                Color(ContextCompat.getColor(context, R.color.label8), false,white),
                Color(ContextCompat.getColor(context, R.color.label9), false,white),
                Color(ContextCompat.getColor(context, R.color.label10), false,black),
                Color(ContextCompat.getColor(context, R.color.label11), false,black),
                Color(ContextCompat.getColor(context, R.color.label12), false,black),
                Color(ContextCompat.getColor(context, R.color.label13), false,white),
                Color(ContextCompat.getColor(context, R.color.label14), false,white)
            )
        }
        @Singleton
        @Provides
        fun getDayIndexDataStore(@ApplicationContext context: Context):DataStore<Preferences>{
            return context.createDataStore(
                "settings"
            )
        }

    }

    @Singleton
    @Binds
    abstract fun bindScheduleRepository(repository: ScheduleRepositoryImpl): ScheduleRepository



}