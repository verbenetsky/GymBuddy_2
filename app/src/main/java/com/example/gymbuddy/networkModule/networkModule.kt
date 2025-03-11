package com.example.gymbuddy.networkModule

import com.example.gymbuddy.pushnotification.FcmApi
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import dagger.Module
import dagger.Provides

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideFcmApi(): FcmApi {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.33:8080/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FcmApi::class.java)
    }
}
