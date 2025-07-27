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
        
        // Initialize any application-wide resources or configurations here
        // For example:
        // - Crash reporting
        // - Analytics
        // - Network configuration
        // - Database pre-population
    }
}