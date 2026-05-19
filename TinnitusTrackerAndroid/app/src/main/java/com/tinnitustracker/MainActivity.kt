package com.tinnitustracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinnitustracker.data.database.AppDatabase
import com.tinnitustracker.data.database.entities.DiaryEntry
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.DiaryRepository
import com.tinnitustracker.data.repository.ListeningSessionRepository
import com.tinnitustracker.data.repository.SoundPresetRepository
import com.tinnitustracker.data.repository.TfiRepository
import com.tinnitustracker.data.repository.UserSettingsRepository
import com.tinnitustracker.ui.assessment.TfiQuestionnaireScreen
import com.tinnitustracker.ui.assessment.TfiQuestionnaireViewModel
import com.tinnitustracker.ui.assessment.TfiQuestionnaireViewModelFactory
import com.tinnitustracker.ui.assessment.TfiResultsScreen
import com.tinnitustracker.ui.components.MiniPlayer
import com.tinnitustracker.ui.home.HomeScreen
import com.tinnitustracker.ui.home.PresetPickerBottomSheet
import com.tinnitustracker.ui.matcher.FrequencyMatchingScreen
import com.tinnitustracker.ui.matcher.FrequencyMatchingViewModel
import com.tinnitustracker.ui.matcher.FrequencyMatchingViewModelFactory
import com.tinnitustracker.ui.onboarding.OnboardingScreen
import com.tinnitustracker.ui.onboarding.OnboardingViewModel
import com.tinnitustracker.ui.onboarding.OnboardingViewModelFactory
import com.tinnitustracker.ui.records.RecordsScreen
import com.tinnitustracker.ui.records.RecordsViewModel
import com.tinnitustracker.ui.records.RecordsViewModelFactory
import com.tinnitustracker.ui.records.QuickLogBottomSheet
import com.tinnitustracker.ui.settings.SettingsScreen
import com.tinnitustracker.ui.sounds.SoundSettingsScreen
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.TinnitusTrackerTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import com.tinnitustracker.ui.home.HomeViewModel
import com.tinnitustracker.ui.home.HomeViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var audioRepository: AudioRepository
    private lateinit var userSettingsRepository: UserSettingsRepository
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var tfiRepository: TfiRepository
    private lateinit var listeningSessionRepository: ListeningSessionRepository
    private lateinit var soundPresetRepository: SoundPresetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TinnitusTrackerApp
        userSettingsRepository = UserSettingsRepository(applicationContext)
        val db = AppDatabase.get(applicationContext)
        diaryRepository = DiaryRepository(db.diaryDao())
        tfiRepository = TfiRepository(db.tfiAssessmentDao(), userSettingsRepository)
        listeningSessionRepository = ListeningSessionRepository(db.listeningSessionDao())
        soundPresetRepository = SoundPresetRepository(db.soundPresetDao())
        audioRepository = AudioRepository(applicationContext, app.audioEngine, listeningSessionRepository, soundPresetRepository, userSettingsRepository)

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
                Root(
                    audioRepository,
                    userSettingsRepository,
                    tfiRepository,
                    listeningSessionRepository,
                    diaryRepository,
                    soundPresetRepository
                )
            }
        }
    }
}

@Composable
private fun Root(
    audio: AudioRepository,
    repo: UserSettingsRepository,
    tfi: TfiRepository,
    sessions: ListeningSessionRepository,
    diary: DiaryRepository,
    preset: SoundPresetRepository
) {
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
        RootScaffold(audio, repo, tfi, sessions, diary, preset, onReplayOnboarding = { replayOnboarding = true })
    }
}

private enum class Tab(val label: String) {
    Home("홈"), Sound("소리"), Records("기록"), Settings("설정")
}

/** Subscreens reachable from inside the 소리 tab — single level deep for now. */
private enum class SoundSub { Matcher }

/** Subscreens reachable from inside the 설정 tab. */
private enum class SettingsSub { Tfi, TfiResults }

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun RootScaffold(
    audio: AudioRepository,
    repo: UserSettingsRepository,
    tfi: TfiRepository,
    sessions: ListeningSessionRepository,
    diary: DiaryRepository,
    preset: SoundPresetRepository,
    onReplayOnboarding: () -> Unit
) {
    var current by rememberSaveable { mutableStateOf(Tab.Home) }
    var soundSub by rememberSaveable { mutableStateOf<SoundSub?>(null) }
    var settingsSub by rememberSaveable { mutableStateOf<SettingsSub?>(null) }

    val matcherVm: FrequencyMatchingViewModel =
        viewModel(factory = FrequencyMatchingViewModelFactory(audio, repo))
    val tfiVm: TfiQuestionnaireViewModel =
        viewModel(factory = TfiQuestionnaireViewModelFactory(tfi))
    val recordsVm: RecordsViewModel =
        viewModel(factory = RecordsViewModelFactory(sessions, diary, tfi, repo))
    val homeVm: HomeViewModel =
        viewModel(factory = HomeViewModelFactory(audio, preset, sessions, repo))

    val tfiCadenceWeeks by repo.tfiCadenceWeeks.collectAsState(initial = 2)
    val lastTfiDate by repo.lastTfiDate.collectAsState(initial = 0L)
    val isPlaying by audio.isPlayingFlow.collectAsState(initial = false)
    val activePresetId by repo.activePresetId.collectAsState(initial = 0L)
    val activePreset by repo.activePresetId
        .flatMapLatest { id -> preset.observeById(id) }
        .collectAsState(initial = null)
    val allPresets by preset.observeAll().collectAsState(initial = emptyList())
    val liveSession by audio.liveSession.collectAsState(initial = null)
    val sleepTimer by audio.sleepTimer.collectAsState(initial = null)
    val treatmentStartDate by repo.treatmentStartDate.collectAsState(initial = 0L)
    val dailyGoalMin by repo.dailyListeningGoalMin.collectAsState(initial = 120)

    val showTfiPrompt = System.currentTimeMillis() >=
        lastTfiDate + tfiCadenceWeeks * 7L * 24L * 60L * 60L * 1000L
    val scope = rememberCoroutineScope()

    var showQuickLog by rememberSaveable { mutableStateOf(false) }
    var showPresetPicker by rememberSaveable { mutableStateOf(false) }
    /** When non-null, the active preset name is appended to the saved tags. */
    var quickLogPrefillPreset by rememberSaveable { mutableStateOf<String?>(null) }

    // System back inside a 소리 subscreen returns to the 소리 root.
    BackHandler(enabled = current == Tab.Sound && soundSub != null) {
        soundSub = null
    }
    // System back inside a 설정 subscreen returns to the 설정 root.
    BackHandler(enabled = current == Tab.Settings && settingsSub != null) {
        settingsSub = null
    }

    Scaffold(
        bottomBar = {
          Column {
            MiniPlayer(
                visible = current != Tab.Home && !(current == Tab.Sound && soundSub == SoundSub.Matcher),
                presetName = activePreset?.name,
                isPlaying = isPlaying,
                liveSession = liveSession,
                sleepTimerRemainingSec = sleepTimer?.remainingSec,
                onTogglePlay = { audio.togglePlay() },
                onLogSession = {
                    quickLogPrefillPreset = activePreset?.name
                    showQuickLog = true
                },
                onClick = {
                    if (current == Tab.Sound) soundSub = null
                    current = Tab.Sound
                }
            )
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.drawBehind {
                    val stroke = 1.dp.toPx()
                    drawLine(
                        color = Line,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = stroke
                    )
                }
            ) {
                Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab,
                        onClick = {
                            // Switching tabs collapses any open subscreen.
                            if (current == Tab.Sound && tab != Tab.Sound) soundSub = null
                            if (current == Tab.Settings && tab != Tab.Settings) settingsSub = null
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
                            selectedIconColor   = Teal,
                            selectedTextColor   = Teal,
                            indicatorColor      = TealSoft,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
          }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (current) {
                Tab.Home -> HomeScreen(
                    vm = homeVm,
                    onStartTherapy = { current = Tab.Sound },
                    onStartTfi = {
                        tfiVm.reset()
                        current = Tab.Settings
                        settingsSub = SettingsSub.Tfi
                    },
                    showTfiPrompt = showTfiPrompt,
                    onOpenQuickLog = {
                        quickLogPrefillPreset = null
                        showQuickLog = true
                    },
                    onOpenPresetPicker = { showPresetPicker = true }
                )
                Tab.Sound -> when (soundSub) {
                    null -> SoundSettingsScreen(
                        audioRepo = audio,
                        settingsRepo = repo,
                        presetRepo = preset,
                        onOpenMatcher = { soundSub = SoundSub.Matcher }
                    )
                    SoundSub.Matcher -> FrequencyMatchingScreen(matcherVm)
                }
                Tab.Records -> RecordsScreen(
                    vm = recordsVm,
                    onStartTfi = {
                        tfiVm.reset()
                        current = Tab.Settings
                        settingsSub = SettingsSub.Tfi
                    }
                )
                Tab.Settings -> when (settingsSub) {
                    null -> SettingsScreen(
                        onReplayOnboarding = onReplayOnboarding,
                        onOpenTfi = {
                            tfiVm.reset()
                            settingsSub = SettingsSub.Tfi
                        },
                        tfiCadenceWeeks = tfiCadenceWeeks,
                        onToggleTfiCadence = {
                            scope.launch {
                                repo.setTfiCadenceWeeks(if (tfiCadenceWeeks == 2) 1 else 2)
                            }
                        },
                        treatmentStartDate = treatmentStartDate,
                        onSetTreatmentStartDate = { ms ->
                            scope.launch { repo.setTreatmentStartDate(ms) }
                        },
                        dailyGoalMin = dailyGoalMin,
                        onSetDailyGoal = { mins ->
                            scope.launch { repo.setDailyListeningGoalMin(mins) }
                        }
                    )
                    SettingsSub.Tfi -> TfiQuestionnaireScreen(
                        vm = tfiVm,
                        onClose = { settingsSub = null },
                        onSubmitted = { settingsSub = SettingsSub.TfiResults }
                    )
                    SettingsSub.TfiResults -> TfiResultsScreen(
                        vm = tfiVm,
                        onDone = { settingsSub = null }
                    )
                }
            }
            
            if (showQuickLog) {
                QuickLogBottomSheet(
                    onDismiss = {
                        showQuickLog = false
                        quickLogPrefillPreset = null
                    },
                    onSave = { severity, stress, tags ->
                        val finalTags = quickLogPrefillPreset?.let { listOf(it) + tags } ?: tags
                        recordsVm.addDiaryEntry(severity, stress, finalTags)
                        showQuickLog = false
                        quickLogPrefillPreset = null
                    }
                )
            }

            if (showPresetPicker) {
                PresetPickerBottomSheet(
                    presets = allPresets,
                    activePresetId = activePresetId,
                    onPick = { id ->
                        scope.launch { repo.setActivePresetId(id) }
                        showPresetPicker = false
                    },
                    onDismiss = { showPresetPicker = false }
                )
            }
        }
    }
}
