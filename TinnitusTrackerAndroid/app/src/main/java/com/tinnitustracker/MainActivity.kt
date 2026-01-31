package com.tinnitustracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.tinnitustracker.ui.diary.DiaryEntryScreen
import com.tinnitustracker.ui.diary.DiaryScreen
import com.tinnitustracker.ui.diary.DiaryViewModel
import com.tinnitustracker.ui.diary.DiaryViewModelFactory
import com.tinnitustracker.ui.frequency.FrequencyMatchingScreen
import com.tinnitustracker.ui.frequency.FrequencyViewModel
import com.tinnitustracker.ui.frequency.FrequencyViewModelFactory
import com.tinnitustracker.ui.profile.ProfileScreen
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: AudioEngine
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Core Components
        audioEngine = AudioEngine()
        database = AppDatabase.getDatabase(this)

        setContent {
            TinnitusTrackerTheme {
                MainScreen(
                    audioEngine = audioEngine,
                    database = database
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}

@Composable
fun MainScreen(audioEngine: AudioEngine, database: AppDatabase) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NavigationBarItem(
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Frequency") },
                    label = { Text("Frequency") },
                    selected = currentDestination?.hierarchy?.any { it.route == "frequency" } == true,
                    onClick = {
                        navController.navigate("frequency") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Diary") },
                    label = { Text("Diary") },
                    selected = currentDestination?.hierarchy?.any { it.route?.startsWith("diary") == true } == true,
                    onClick = {
                        navController.navigate("diary") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                    onClick = {
                        navController.navigate("profile") {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "frequency",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("frequency") {
                val viewModel: FrequencyViewModel = viewModel(
                    factory = FrequencyViewModelFactory(audioEngine)
                )
                FrequencyMatchingScreen(viewModel)
            }
            
            composable("diary") {
                val viewModel: DiaryViewModel = viewModel(
                    factory = DiaryViewModelFactory(database.diaryDao())
                )
                DiaryScreen(
                    viewModel = viewModel,
                    onAddEntryClick = { navController.navigate("diary/add") }
                )
            }
            
            composable("diary/add") {
                val viewModel: DiaryViewModel = viewModel(
                    factory = DiaryViewModelFactory(database.diaryDao())
                )
                DiaryEntryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}
