package com.example.gymbuddy.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GymBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
