package com.aminook.tunemyday.di

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aminook.tunemyday.business.domain.util.DateUtil
import com.aminook.tunemyday.framework.datasource.database.ScheduleDatabase
import com.aminook.tunemyday.framework.presentation.common.AppFragmentFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
abstract class AppModule {

        companion object{


            @Singleton
            @Provides
            fun provideAppFragmentFactory():FragmentFactory{
                return AppFragmentFactory()
            }

//            @Singleton
//            @Provides
//            fun provideScheduleDatabase(@ApplicationContext application:Context)=
//                Room.databaseBuilder(application,ScheduleDatabase::class.java,"schedule_database")
//                    .build()


            @Singleton
            @Provides
            fun provideScheduleDao(@ApplicationContext application:Context)=
                ScheduleDatabase.getDatabase(application).scheduleDao()

        }
}