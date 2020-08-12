package com.aminook.tunemyday.di

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminook.tunemyday.business.domain.model.Day
import com.aminook.tunemyday.business.domain.model.DayFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.util.*
import javax.inject.Singleton


@InstallIn(FragmentComponent::class)
@Module
class FragmentModule {



    @FragmentScoped
    @Provides
    fun provideNextSevenDays(dayFactory:DayFactory):List<Day>{
        return  dayFactory.getNextSevenDays(Date())
    }

}