package com.aminook.tunemyday.di

import android.content.Context
import androidx.fragment.app.FragmentFactory
import com.aminook.tunemyday.business.data.cache.ScheduleRepository
import com.aminook.tunemyday.business.data.cache.ScheduleRepositoryImpl
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

        @Singleton
        @Provides
        fun provideScheduleDao(scheduleDatabase: ScheduleDatabase) =
            scheduleDatabase.scheduleDao()

        @Singleton
        @Provides
        fun provideTodoScheduleDao(scheduleDatabase: ScheduleDatabase) =
            scheduleDatabase.todoScheduleDao()

    }

    @Singleton
    @Binds
    abstract fun bindScheduleRepository(repository: ScheduleRepositoryImpl): ScheduleRepository
}