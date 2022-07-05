package com.rvt.storagedemo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.downloader.PRDownloader
import com.rvt.storagedemo.di.coreNetworkModule
import com.rvt.storagedemo.di.networkNodule
import com.rvt.storagedemo.di.repository
import com.rvt.storagedemo.di.viewModel
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import javax.inject.Inject

@HiltAndroidApp
class App : Application() , Configuration.Provider{

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(level = Level.DEBUG)
            androidContext(this@App)
            modules(listOf(
                coreNetworkModule,
                networkNodule,
                repository,
                viewModel,
            ))
        }

        PRDownloader.initialize(applicationContext)
    }
}