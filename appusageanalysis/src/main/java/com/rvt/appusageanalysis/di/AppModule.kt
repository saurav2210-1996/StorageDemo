package com.rvt.appusageanalysis.di

import android.content.Context
import com.rvt.appusageanalysis.AppUsageStateUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppUsageStateUtil(@ApplicationContext context: Context) = AppUsageStateUtil(context)
}
