package com.tinnitustracker

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinnitustracker.data.database.AppDatabase
import com.tinnitustracker.data.database.entities.DiaryEntry
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.DiaryRepository
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var audioRepository: AudioRepository
    private lateinit var userSettingsRepository: UserSettingsRepository
    private lateinit var diaryRepository: DiaryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TinnitusTrackerApp
        audioRepository = AudioRepository(applicationContext, app.audioEngine)
        userSettingsRepository = UserSettingsRepository(applicationContext)
        diaryRepository = DiaryRepository(AppDatabase.get(applicationContext).diaryDao())

        // Round-trip sanity check for the Room scaffold. Inserts a marker row,
        // reads it back, then deletes it — leaves no user-visible residue.
        lifecycleScope.launch {
            val id = diaryRepository.upsert(
                DiaryEntry(
                    date = System.currentTimeMillis(),
                    severity = 0,
                    stressLevel = 0,
                    note = "room-scaffold-sanity-check"
                )
            )
            val readBack = diaryRepository.latest()
            diaryRepository.deleteById(id)
            Log.d(
                "RoomScaffold",
                "wrote id=$id readBack='${readBack?.note}' dbFile=${getDatabasePath("tinnitus_tracker.db")}"
            )
        }

        setContent {
            TinnitusTrackerTheme {
                RootScaffold(audioRepository, userSettingsRepository)
            }
        }
    }
}

private enum class Tab(val label: String) { Matcher("톤 찾기"), Settings("설정") }

@Composable
private fun RootScaffold(audio: AudioRepository, repo: UserSettingsRepository) {
    var current by remember { mutableStateOf(Tab.Matcher) }
    val matcherVm: FrequencyMatchingViewModel = viewModel(factory = FrequencyMatchingViewModelFactory(audio, repo))

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
