package com.rvt.storagedemo.di

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val coreNetworkModule = module {
    single(named("default")) { getOkHttpClient() }
    factory(named("timeout")) { (timeout : Long) -> getOkHttpClient(timeout) }
    factory { (baseUrl: String) -> getDefaultRetrofitBuilder(baseUrl) }
}

internal fun getDefaultRetrofitBuilder(baseUrl: String) : Retrofit.Builder {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
}

internal fun getOkHttpClient(timeout: Long = 10) : OkHttpClient {
    return OkHttpClient.Builder().apply {
        writeTimeout(timeout, TimeUnit.SECONDS)
        readTimeout(timeout, TimeUnit.SECONDS)
        connectTimeout(timeout, TimeUnit.SECONDS)
    }.build()
}