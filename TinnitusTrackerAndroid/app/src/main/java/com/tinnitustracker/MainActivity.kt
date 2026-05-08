package com.tinnitustracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinnitustracker.audio.engine.AudioEngine
import com.tinnitustracker.data.repository.UserSettingsRepository
import com.tinnitustracker.ui.matcher.FrequencyMatchingScreen
import com.tinnitustracker.ui.matcher.FrequencyMatchingViewModel
import com.tinnitustracker.ui.matcher.FrequencyMatchingViewModelFactory
import com.tinnitustracker.ui.settings.SettingsScreen
import com.tinnitustracker.ui.theme.DarkBg
import com.tinnitustracker.ui.theme.OrangeAccent
import com.tinnitustracker.ui.theme.TextPrimary
import com.tinnitustracker.ui.theme.TextTertiary
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioEngine: AudioEngine
    private lateinit var userSettingsRepository: UserSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine = AudioEngine(applicationContext)
        userSettingsRepository = UserSettingsRepository(applicationContext)
        setContent {
            TinnitusTrackerTheme {
                RootScaffold(audioEngine, userSettingsRepository)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioEngine.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.release()
    }
}

private enum class Tab(val label: String) { Matcher("톤 찾기"), Settings("설정") }

@Composable
private fun RootScaffold(engine: AudioEngine, repo: UserSettingsRepository) {
    var current by remember { mutableStateOf(Tab.Matcher) }
    val matcherVm: FrequencyMatchingViewModel = viewModel(factory = FrequencyMatchingViewModelFactory(engine, repo))

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkBg) {
                Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab,
                        onClick = { current = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    Tab.Matcher  -> Icons.Filled.GraphicEq
                                    Tab.Settings -> Icons.Filled.Settings
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangeAccent,
                            selectedTextColor = OrangeAccent,
                            indicatorColor = OrangeAccent.copy(alpha = 0.2f),
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (current) {
                Tab.Matcher  -> FrequencyMatchingScreen(matcherVm)
                Tab.Settings -> SettingsScreen()
            }
        }
    }
}
