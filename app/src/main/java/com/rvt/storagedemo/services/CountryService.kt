package com.rvt.storagedemo.services

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

interface CountryService {

    @GET("countries/info?returns=currency,flag,unicodeFlag,dialCode")
    fun getCountryList() : Call<ResponseBody>

    companion object: KoinComponent{

        private val okHttpClient : OkHttpClient by inject(named("timeout")) { parametersOf(10L) }
        private val retrofitBuilder : Retrofit.Builder by inject { parametersOf("https://countriesnow.space/api/v0.1/") }

        fun create() : CountryService{
            val clientBuilder = okHttpClient.newBuilder()

            clientBuilder.addInterceptor { chain ->
                val newRequestBuilder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                chain.proceed(newRequestBuilder.build())
            }

            clientBuilder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })

            return retrofitBuilder
                .client(clientBuilder.build())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
                .create(CountryService::class.java)
        }
    }
}