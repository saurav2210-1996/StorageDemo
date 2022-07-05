package com.rvt.storagedemo.di

import com.rvt.storagedemo.services.CountryService
import okhttp3.internal.platform.android.AndroidSocketAdapter.Companion.factory
import org.koin.dsl.module

val networkNodule = module{

    factory{ CountryService.create()}
}