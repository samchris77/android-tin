package com.tinnitustracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinnitustracker.data.database.AppDatabase
import com.tinnitustracker.data.database.entities.DiaryEntry
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.DiaryRepository
import com.tinnitustracker.data.repository.UserSettingsRepository
import com.tinnitustracker.ui.home.HomeScreen
import com.tinnitustracker.ui.matcher.FrequencyMatchingScreen
import com.tinnitustracker.ui.matcher.FrequencyMatchingViewModel
import com.tinnitustracker.ui.matcher.FrequencyMatchingViewModelFactory
import com.tinnitustracker.ui.onboarding.OnboardingScreen
import com.tinnitustracker.ui.onboarding.OnboardingViewModel
import com.tinnitustracker.ui.onboarding.OnboardingViewModelFactory
import com.tinnitustracker.ui.records.RecordsScreen
import com.tinnitustracker.ui.settings.SettingsScreen
import com.tinnitustracker.ui.sounds.SoundSettingsScreen
import com.tinnitustracker.ui.theme.DarkBg
import com.tinnitustracker.ui.theme.OrangeAccent
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
                Root(audioRepository, userSettingsRepository)
            }
        }
    }
}

@Composable
private fun Root(audio: AudioRepository, repo: UserSettingsRepository) {
    val onboardingComplete by repo.onboardingComplete.collectAsState(initial = false)
    var replayOnboarding by rememberSaveable { mutableStateOf(false) }

    val showOnboarding = !onboardingComplete || replayOnboarding

    if (showOnboarding) {
        val onboardingVm: OnboardingViewModel =
            viewModel(factory = OnboardingViewModelFactory(repo))
        OnboardingScreen(
            onFinished = { replayOnboarding = false },
            vm = onboardingVm
        )
    } else {
        RootScaffold(audio, repo, onReplayOnboarding = { replayOnboarding = true })
    }
}

private enum class Tab(val label: String) {
    Home("홈"), Sound("소리"), Records("기록"), Settings("설정")
}

/** Subscreens reachable from inside the 소리 tab — single level deep for now. */
private enum class SoundSub { Matcher }

@Composable
private fun RootScaffold(
    audio: AudioRepository,
    repo: UserSettingsRepository,
    onReplayOnboarding: () -> Unit
) {
    var current by rememberSaveable { mutableStateOf(Tab.Home) }
    var soundSub by rememberSaveable { mutableStateOf<SoundSub?>(null) }

    val matcherVm: FrequencyMatchingViewModel =
        viewModel(factory = FrequencyMatchingViewModelFactory(audio, repo))

    // System back inside a 소리 subscreen returns to the 소리 root.
    BackHandler(enabled = current == Tab.Sound && soundSub != null) {
        soundSub = null
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkBg) {
                Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab,
                        onClick = {
                            // Switching tabs collapses any 소리 subscreen.
                            if (current == Tab.Sound && tab != Tab.Sound) soundSub = null
                            current = tab
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    Tab.Home     -> Icons.Filled.Home
                                    Tab.Sound    -> Icons.Filled.GraphicEq
                                    Tab.Records  -> Icons.Filled.CalendarMonth
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
                Tab.Home -> HomeScreen(
                    onStartTherapy = { current = Tab.Sound }
                )
                Tab.Sound -> when (soundSub) {
                    null -> SoundSettingsScreen(
                        onOpenMatcher = { soundSub = SoundSub.Matcher }
                    )
                    SoundSub.Matcher -> FrequencyMatchingScreen(matcherVm)
                }
                Tab.Records -> RecordsScreen()
                Tab.Settings -> SettingsScreen(onReplayOnboarding = onReplayOnboarding)
            }
        }
    }
}
