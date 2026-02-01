package com.tinnitustracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tinnitustracker.audio.AudioEngine
import com.tinnitustracker.data.AppDatabase
import com.tinnitustracker.ui.frequency.FrequencyMatchingScreen
import com.tinnitustracker.ui.frequency.FrequencyViewModel
import com.tinnitustracker.ui.frequency.FrequencyViewModelFactory
import com.tinnitustracker.ui.therapy.TherapyScreen
import com.tinnitustracker.ui.therapy.TherapyController
import com.tinnitustracker.ui.therapy.TherapyViewModel
import com.tinnitustracker.ui.therapy.TherapyViewModelFactory
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: AudioEngine
    // private lateinit var database: AppDatabase // Database unused in new layout for now

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Audio Engine
        audioEngine = AudioEngine(this)
        // database = AppDatabase.getDatabase(this)

        setContent {
            TinnitusTrackerTheme {
                MainScreen(audioEngine = audioEngine)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}

@Composable
fun MainScreen(audioEngine: AudioEngine) {
    val navController = rememberNavController()

    // Create shared TherapyViewModel to persist across screens and drive the Controller
    val therapyViewModel: TherapyViewModel = viewModel(
        factory = TherapyViewModelFactory(audioEngine)
    )

    Scaffold(
        bottomBar = {
            Column {
                // Therapy Controller (Persistent above Nav Bar)
                // We can conditionally show it or keep it always visible.
                // Request implies it's a "component... directly above".
                TherapyController(viewModel = therapyViewModel)
                
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    // 1. Matcher Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Matcher") },
                        label = { Text("Matcher") },
                        selected = currentDestination?.hierarchy?.any { it.route == "matcher" } == true,
                        onClick = {
                            navController.navigate("matcher") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // 2. Therapy Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Spa, contentDescription = "Therapy") },
                        label = { Text("Therapy") },
                        selected = currentDestination?.hierarchy?.any { it.route == "therapy" } == true,
                        onClick = {
                            navController.navigate("therapy") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // 3. Settings Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true,
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "matcher", // Default to Matcher
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("matcher") {
                val viewModel: FrequencyViewModel = viewModel(
                    factory = FrequencyViewModelFactory(audioEngine)
                )
                FrequencyMatchingScreen(viewModel)
            }
            
            composable("therapy") {
                // AudioEngine is shared, so Notch Frequency set in Matcher applies here.
                TherapyScreen(audioEngine = audioEngine)
            }
            
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Settings Placeholder\nVersion 1.0")
    }
}
