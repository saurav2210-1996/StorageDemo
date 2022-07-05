package com.rvt.storagedemo.di

import android.os.Build
import android.os.Environment
import com.rvt.appusageanalysis.AppUsageStateUtil
import com.rvt.storagedemo.repository.CountryRepository
import com.rvt.storagedemo.repository.CountryRepositoryImp
import com.rvt.storagedemo.services.CountryService
import com.rvt.storagedemo.ui.MainActivityViewModel
import com.rvt.storagedemo.ui.helper.FileObserver
import com.rvt.storagedemo.ui.outputDir
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.io.File

val repository = module{


    val destURL = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + outputDir
    }else{
        File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), outputDir).path
    }
    single { FileObserver(destURL) }
    single { AppUsageStateUtil(androidContext()) }
    single<CountryRepository> { CountryRepositoryImp(get()) }
}