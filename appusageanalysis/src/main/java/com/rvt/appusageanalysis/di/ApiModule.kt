package com.rvt.appusageanalysis.di

import com.rvt.appusageanalysis.APIConst
import com.rvt.appusageanalysis.AnthemAppService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS) // connect timeout
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val newRequestBuilder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                newRequestBuilder.addHeader("x-api-key", when(APIConst.CURRENT_BASE_URL){
                    APIConst.DEV_ENV -> APIConst.DEV_HEADER
                    APIConst.STAGE_ENV -> APIConst.STAGE_HEADER
                    APIConst.PROD_ENV -> APIConst.PROD_HEADER
                    else -> APIConst.DEV_HEADER
                })
                chain.proceed(newRequestBuilder.build())
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val BASE_URL by lazy {
            when (APIConst.CURRENT_BASE_URL) {
                APIConst.DEV_ENV -> APIConst.DEV_BASE_URL
                APIConst.STAGE_ENV -> APIConst.STAGE_BASE_URL
                APIConst.PROD_ENV -> APIConst.PROD_BASE_URL
                else -> APIConst.DEV_BASE_URL
            }
        }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideAnthemAppService(retrofit: Retrofit): AnthemAppService {
        return retrofit.create(AnthemAppService::class.java)
    }
}