package com.example.homebrewhelper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for HomeBrewHelper
 * Enables Hilt dependency injection throughout the app
 */
@HiltAndroidApp
class HomeBrewHelperApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Note: Initialization is handled in ViewModels to ensure proper dependency injection
        android.util.Log.d("HomeBrewHelper", "Application started")
    }
}