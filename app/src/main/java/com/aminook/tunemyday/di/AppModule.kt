package com.aminook.tunemyday.di

import androidx.fragment.app.FragmentFactory
import com.aminook.tunemyday.framework.presentation.common.AppFragmentFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Singleton

@InstallIn(ActivityComponent::class)
@Module
abstract class AppModule {

        companion object{

            @ActivityScoped
            @Provides
            fun provideAppFragmentFactory():FragmentFactory{
                return AppFragmentFactory()
            }
        }
}