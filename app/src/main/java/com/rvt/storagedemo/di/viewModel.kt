package com.rvt.storagedemo.di

import com.rvt.storagedemo.ui.MainActivityViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModel = module {
    viewModel { MainActivityViewModel(context = androidContext(),get()) }
}