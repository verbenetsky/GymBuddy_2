package com.example.gymbuddy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GymBuddyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Dodatkowa inicjalizacja, jeśli potrzebna
    }
}
