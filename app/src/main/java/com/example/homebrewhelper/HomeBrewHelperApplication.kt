package com.example.homebrewhelper

import android.app.Application
import com.example.homebrewhelper.data.repository.InitializationRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for HomeBrewHelper
 * Enables Hilt dependency injection throughout the app
 */
@HiltAndroidApp
class HomeBrewHelperApplication : Application() {
    
    // Application scope for initialization tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Inject
    lateinit var initializationRepository: InitializationRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize default data in background
        initializeAppData()
    }
    
    private fun initializeAppData() {
        applicationScope.launch {
            try {
                initializationRepository.initializeDefaultData()
                    .onFailure { error ->
                        // Log error but don't crash the app
                        android.util.Log.e("HomeBrewHelper", "Failed to initialize default data", error)
                    }
            } catch (e: Exception) {
                // Handle any unexpected errors
                android.util.Log.e("HomeBrewHelper", "Unexpected error during initialization", e)
            }
        }
    }
}