package com.example.homebrewhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.homebrewhelper.ui.navigation.HomeBrewNavigation
import com.example.homebrewhelper.ui.theme.HomebrewhelperTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the HomeBrewHelper application
 * Entry point for the Jetpack Compose UI with Hilt dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            HomebrewhelperTheme {
                HomeBrewHelperApp()
            }
        }
    }
}

/**
 * Main app composable that sets up the navigation and overall app structure
 */
@Composable
fun HomeBrewHelperApp() {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        HomeBrewNavigation(
            navController = navController
        )
    }
}