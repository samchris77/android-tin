import kotlinx.coroutines.*

class AudioSessionTracker(
    private val scope: CoroutineScope,
    private val onSessionComplete: (SessionData) -> Unit
) {
    private val _liveState = MutableStateFlow(LiveSessionState())
    val liveState: StateFlow<LiveSessionState> = _liveState.asStateFlow()

    private var sessionStartTime = 0L
    private var currentSoundStartTime = 0L
    private var currentSoundId: String? = null
    
    private val soundDurations = mutableMapOf<String, Long>()
    private var isPlaying = false
    private var totalListeningTime = 0L
    
    private var debounceJob: Job? = null
    private var tickerJob: Job? = null

    fun onPlay(soundId: String) {
        val now = System.currentTimeMillis()
        
        if (debounceJob?.isActive == true) {
            // Resume: Cancel the 60-sec countdown
            debounceJob?.cancel()
            currentSoundStartTime = now
            currentSoundId = soundId
            isPlaying = true
        } else {
            // New Session
            sessionStartTime = now
            currentSoundStartTime = now
            currentSoundId = soundId
            totalListeningTime = 0L
            soundDurations.clear()
            isPlaying = true
        }
        startTicker()
    }

    fun onSoundChanged(newSoundId: String) {
        if (!isPlaying) return
        val now = System.currentTimeMillis()
        tallyCurrentSound(now)
        currentSoundStartTime = now
        currentSoundId = newSoundId
    }

    fun onPause() {
        if (!isPlaying) return
        val now = System.currentTimeMillis()
        
        tallyCurrentSound(now)
        isPlaying = false
        
        // Update UI to reflect paused state
        tickerJob?.cancel()
        _liveState.update { 
            it.copy(isTracking = true, durationMillis = totalListeningTime, isPausedDebouncing = true) 
        }

        // Start 60-second debounce
        debounceJob = scope.launch {
            delay(60_000L) 
            finalizeSession() 
        }
    }

    private fun tallyCurrentSound(now: Long) {
        val timeSpent = now - currentSoundStartTime
        totalListeningTime += timeSpent
        currentSoundId?.let { id ->
            soundDurations[id] = soundDurations.getOrDefault(id, 0L) + timeSpent
        }
    }

    private fun finalizeSession() {
        if (totalListeningTime >= 300_000L) { // 5-minute minimum
            val dominantSoundId = soundDurations.maxByOrNull { it.value }?.key 
            if (dominantSoundId != null) {
                onSessionComplete(
                    SessionData(totalListeningTime, dominantSoundId, sessionStartTime)
                )
            }
        }
        _liveState.value = LiveSessionState() // Reset UI
        soundDurations.clear()
        totalListeningTime = 0L
        currentSoundId = null
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                if (isPlaying) {
                    val currentActiveTime = System.currentTimeMillis() - currentSoundStartTime
                    _liveState.update {
                        it.copy(
                            isTracking = true,
                            durationMillis = totalListeningTime + currentActiveTime,
                            isPausedDebouncing = false
                        )
                    }
                }
                delay(1000L)
            }
        }
    }
}