package com.aminook.tunemyday.di

import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.util.DayFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import java.util.*


@InstallIn(FragmentComponent::class)
@Module
class FragmentModule {



    @FragmentScoped
    @Provides
    fun provideNextSevenDays(dayFactory: DayFactory):List<Day>{
        return  dayFactory.getDaysOfWeek()
    }

}