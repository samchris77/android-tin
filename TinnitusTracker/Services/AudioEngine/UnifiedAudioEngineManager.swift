import AVFoundation
import Foundation
import Combine
import QuartzCore
import MediaPlayer

protocol AudioManagerDelegate: AnyObject {
    func audioManagerDidUpdateFrequency(_ frequency: Float)
    func audioManagerDidUpdateVolume(_ volume: Float) 
    func audioManagerDidUpdatePlayingState(_ isPlaying: Bool)
}

class UnifiedAudioEngineManager: ObservableObject {
    static let shared = UnifiedAudioEngineManager()
    
    private let audioEngine = AVAudioEngine()
    private let audioSession = AVAudioSession.sharedInstance()
    private var sampleRate: Double = 44100.0
    
    // Frequency Matching
    private var frequencySourceNode: AVAudioSourceNode!
    private let frequencyMixer = AVAudioMixerNode()
    private let frequencyEQ = AVAudioUnitEQ(numberOfBands: 1)
    private var frequencyTime: Float = 0.0
    private let twoPi = 2.0 * Float.pi
    
    // Published properties
    @Published var isFrequencyPlaying = false
    @Published var currentFrequency: Float = 440.0
    @Published var currentFrequencyVolume: Float = 0.5
    
    // Session tracking properties
    @Published var sessionStartTime: Date?
    @Published var currentSessionDuration: TimeInterval = 0
    @Published var lastSessionDuration: TimeInterval = 0
    @Published var totalSessionTime: TimeInterval = 0
    
    // Interpolation properties
    private var targetFrequency: Float = 440.0
    private var targetVolume: Float = 0.5
    private var displayLink: CADisplayLink?
    private var lastUpdateTime: CFTimeInterval = 0
    private let interpolationSpeed: Float = 8.0 // Higher = faster interpolation
    
    // Delegate
    weak var delegate: AudioManagerDelegate?
    
    // Session tracking timer
    private var sessionTimer: Timer?
    
    // Track last Now Playing info to prevent redundant updates
    private var lastNowPlayingFrequency: Float = 0
    private var lastNowPlayingIsPlaying: Bool = false
    private var lastNowPlayingDuration: TimeInterval = 0
    
    private var cancellables = Set<AnyCancellable>()
    
    private init() {
        setupAudioSession()
        loadInitialState()
        setupEngine()
        setupAudioInterruptionHandling()
        loadSessionData()
    }
    
    private func loadInitialState() {
        let savedState = loadAudioState()
        self.currentFrequency = savedState.frequency
        self.currentFrequencyVolume = savedState.volume
        self.targetFrequency = savedState.frequency
        self.targetVolume = savedState.volume
        print("🔊 Audio Manager initial state loaded: \(self.currentFrequency) Hz")
    }
    
    deinit {
        stopInterpolation()
        stopSessionTracking()
        stopAll()
    }
    
    private func setupAudioSession() {
        do {
            // First, ensure the session is not active to avoid conflicts
            try audioSession.setActive(false, options: .notifyOthersOnDeactivation)
            
            // Try the preferred configuration first
            try audioSession.setCategory(.playback, mode: .default, options: [.allowBluetooth, .allowBluetoothA2DP, .duckOthers])
            
            // Activate the session
            try audioSession.setActive(true, options: [])
            sampleRate = audioSession.sampleRate
            
            print("✅ Audio session setup successful - Category: \(audioSession.category.rawValue), Mode: \(audioSession.mode.rawValue)")
            
            // Setup remote command center for lock screen controls
            setupRemoteCommandCenter()
        } catch let error as NSError {
            print("❌ Primary audio session setup failed: \(error.localizedDescription) (Code: \(error.code))")
            
            // Fallback configuration for error -50 and other issues
            setupAudioSessionFallback()
        }
    }
    
    private func setupAudioSessionFallback() {
        do {
            // Try a simpler configuration without bluetooth options
            try audioSession.setCategory(.playback, mode: .default, options: [])
            try audioSession.setActive(true, options: [])
            sampleRate = audioSession.sampleRate
            
            print("✅ Audio session fallback successful - Using basic playback configuration")
            
            // Setup remote command center for lock screen controls
            setupRemoteCommandCenter()
        } catch let error as NSError {
            print("❌ Audio session fallback also failed: \(error.localizedDescription) (Code: \(error.code))")
            
            // Final fallback - try minimal configuration
            do {
                try audioSession.setCategory(.ambient)
                try audioSession.setActive(true)
                sampleRate = audioSession.sampleRate
                print("⚠️ Using minimal audio session configuration")
                setupRemoteCommandCenter()
            } catch {
                print("💥 Complete audio session failure: \(error)")
            }
        }
    }
    
    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        // Enable and configure play command
        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { [weak self] event -> MPRemoteCommandHandlerStatus in
            guard let self = self else { return .commandFailed }
            print("🎵 Lock screen PLAY command received")
            if !self.isFrequencyPlaying {
                self.startFrequencyMatching(frequency: self.currentFrequency, volume: self.currentFrequencyVolume)
                return .success
            }
            return .noActionableNowPlayingItem
        }
        
        // Enable and configure pause command
        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { [weak self] event -> MPRemoteCommandHandlerStatus in
            guard let self = self else { return .commandFailed }
            print("⏸️ Lock screen PAUSE command received")
            if self.isFrequencyPlaying {
                self.stopFrequencyMatching()
                return .success
            }
            return .noActionableNowPlayingItem
        }
        
        // Enable and configure stop command
        commandCenter.stopCommand.isEnabled = true
        commandCenter.stopCommand.addTarget { [weak self] event -> MPRemoteCommandHandlerStatus in
            guard let self = self else { return .commandFailed }
            print("⏹️ Lock screen STOP command received")
            self.stopFrequencyMatching()
            self.clearNowPlayingInfo()
            return .success
        }
        
        // Enable toggle play/pause command for better compatibility
        commandCenter.togglePlayPauseCommand.isEnabled = true
        commandCenter.togglePlayPauseCommand.addTarget { [weak self] event -> MPRemoteCommandHandlerStatus in
            guard let self = self else { return .commandFailed }
            print("⏯️ Lock screen TOGGLE command received")
            if self.isFrequencyPlaying {
                self.stopFrequencyMatching()
            } else {
                self.startFrequencyMatching(frequency: self.currentFrequency, volume: self.currentFrequencyVolume)
            }
            return .success
        }
        
        // Disable commands that don't apply to frequency generation
        commandCenter.nextTrackCommand.isEnabled = false
        commandCenter.previousTrackCommand.isEnabled = false
        commandCenter.seekForwardCommand.isEnabled = false
        commandCenter.seekBackwardCommand.isEnabled = false
        commandCenter.skipForwardCommand.isEnabled = false
        commandCenter.skipBackwardCommand.isEnabled = false
        
        print("🎛️ Remote command center configured successfully")
    }
    
    private func updateNowPlayingInfo() {
        // Get current values
        let currentElapsedTime = sessionStartTime != nil ? Date().timeIntervalSince(sessionStartTime!) : 0.0
        
        // Check if anything actually changed to avoid redundant updates
        let frequencyChanged = abs(currentFrequency - lastNowPlayingFrequency) > 1.0
        let playingStateChanged = isFrequencyPlaying != lastNowPlayingIsPlaying
        let durationChanged = abs(currentElapsedTime - lastNowPlayingDuration) >= 5.0 // Only update duration every 5 seconds
        
        guard frequencyChanged || playingStateChanged || durationChanged else {
            return // Skip update if nothing significant changed
        }
        
        var nowPlayingInfo = [String: Any]()
        
        // Set track information
        nowPlayingInfo[MPMediaItemPropertyTitle] = LocalizedString("audio.now.playing.title")
        nowPlayingInfo[MPMediaItemPropertyArtist] = "Audio Session"
        nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = "\(Int(currentFrequency)) Hz"
        
        // Set playback duration - iOS requires this for lock screen controls
        // Set to a large duration for continuous therapy sessions (1 hour)
        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = 3600.0
        
        // Set playback rate (1.0 for playing, 0.0 for paused)
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isFrequencyPlaying ? 1.0 : 0.0
        
        // Set elapsed time based on current session
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentElapsedTime
        
        // Create custom artwork for lock screen
        if let customIcon = UIImage(named: "soundwave_icon_clean") {
            let artworkSize = CGSize(width: 200, height: 200)
            nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: artworkSize) { _ in
                return customIcon
            }
            // Only log artwork setup on first time or state changes to reduce console spam
            if playingStateChanged || lastNowPlayingFrequency == 0 {
                print("🎨 Lock screen artwork set with custom soundwave icon")
            }
        } else if let waveformIcon = UIImage(systemName: "waveform.path") {
            let artworkSize = CGSize(width: 200, height: 200)
            nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: artworkSize) { _ in
                return waveformIcon
            }
            if playingStateChanged || lastNowPlayingFrequency == 0 {
                print("🎨 Lock screen artwork set with waveform symbol (fallback)")
            }
        } else if let speakerIcon = UIImage(systemName: "speaker.wave.2.fill") {
            let artworkSize = CGSize(width: 200, height: 200)
            nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: artworkSize) { _ in
                return speakerIcon
            }
            if playingStateChanged || lastNowPlayingFrequency == 0 {
                print("🎨 Lock screen artwork set with speaker symbol (final fallback)")
            }
        } else {
            if playingStateChanged || lastNowPlayingFrequency == 0 {
                print("⚠️ Failed to load any artwork for lock screen")
            }
        }
        
        // Set the metadata
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        
        // Update tracking variables
        lastNowPlayingFrequency = currentFrequency
        lastNowPlayingIsPlaying = isFrequencyPlaying
        lastNowPlayingDuration = currentElapsedTime
        
        print("🔒 Lock screen info updated - Playing: \(isFrequencyPlaying), Frequency: \(Int(currentFrequency))Hz, Duration: \(Int(currentElapsedTime))s")
    }
    
    private func clearNowPlayingInfo() {
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
        print("🔒 Lock screen info cleared")
    }
    
    private func setupEngine() {
        // Initialize white noise source node
        frequencySourceNode = AVAudioSourceNode { [weak self] (isSilence, timestamp, frameCount, audioBufferList) -> OSStatus in
            guard let self = self, self.isFrequencyPlaying else {
                // Fill with silence
                let ablPointer = UnsafeMutableAudioBufferListPointer(audioBufferList)
                for buffer in ablPointer {
                    memset(buffer.mData, 0, Int(buffer.mDataByteSize))
                }
                return noErr
            }
            
            let ablPointer = UnsafeMutableAudioBufferListPointer(audioBufferList)
            let amplitude = self.currentFrequencyVolume * 0.3 // Further reduced amplitude to minimize background hissing
            
            for buffer in ablPointer {
                let buf: UnsafeMutableBufferPointer<Float> = UnsafeMutableBufferPointer(buffer)
                for frame in 0..<Int(frameCount) {
                    // Generate white noise sample
                    let randomSample = Float.random(in: -1.0...1.0)
                    buf[frame] = amplitude * randomSample
                }
            }
            
            return noErr
        }
        
        // Configure single-band EQ for focused frequency targeting
        let band = frequencyEQ.bands[0]
        band.filterType = .bandPass
        band.frequency = currentFrequency // Will be updated dynamically
        band.bandwidth = 0.8 // Narrow bandwidth for sharp focus
        band.gain = 10.0 // Strong boost to emphasize the target frequency
        band.bypass = false
        
        // Attach nodes
        audioEngine.attach(frequencySourceNode)
        audioEngine.attach(frequencyEQ)
        audioEngine.attach(frequencyMixer)
        
        // Create format
        guard let format = AVAudioFormat(standardFormatWithSampleRate: sampleRate, channels: 1) else {
            print("Failed to create audio format")
            return
        }
        
        // Connect frequency matching chain: Source -> EQ -> Mixer -> Output
        audioEngine.connect(frequencySourceNode, to: frequencyEQ, format: format)
        audioEngine.connect(frequencyEQ, to: frequencyMixer, format: format)
        audioEngine.connect(frequencyMixer, to: audioEngine.mainMixerNode, format: format)
        
        audioEngine.prepare()
    }
    
    
    private func setupAudioInterruptionHandling() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleAudioInterruption),
            name: AVAudioSession.interruptionNotification,
            object: nil
        )
    }
    
    @objc private func handleAudioInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else {
            return
        }
        
        switch type {
        case .began:
            stopEngine()
        case .ended:
            guard let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt else { return }
            let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
            if options.contains(.shouldResume) {
                if isFrequencyPlaying {
                    startEngine()
                }
            }
        @unknown default:
            break
        }
    }
    
    private func startEngine() {
        guard !audioEngine.isRunning else { return }
        
        do {
            try audioEngine.start()
        } catch {
            print("Failed to start audio engine: \(error)")
        }
    }
    
    private func stopEngine() {
        guard audioEngine.isRunning else { return }
        audioEngine.stop()
    }
    
    func stopAll() {
        stopInterpolation()
        stopFrequencyMatching()
        stopEngine()
    }
    
    // MARK: - Audio Parameter Interpolation
    private func startInterpolation() {
        guard displayLink == nil else { return }
        
        displayLink = CADisplayLink(target: self, selector: #selector(updateInterpolation))
        displayLink?.preferredFramesPerSecond = 60
        displayLink?.add(to: .main, forMode: .common)
        lastUpdateTime = CACurrentMediaTime()
    }
    
    func stopInterpolation() {
        displayLink?.invalidate()
        displayLink = nil
    }
    
    @objc private func updateInterpolation() {
        let currentTime = CACurrentMediaTime()
        let deltaTime = Float(currentTime - lastUpdateTime)
        lastUpdateTime = currentTime
        
        guard deltaTime > 0 else { return }
        
        // Exponential smoothing for frequency with easing
        let frequencyDiff = targetFrequency - currentFrequency
        if abs(frequencyDiff) > 0.1 {
            let progress = min(1.0, interpolationSpeed * deltaTime)
            let easedProgress = UnifiedAudioEngineManager.easeOutExpo(progress)
            let frequencyStep = frequencyDiff * easedProgress
            let newFrequency = currentFrequency + frequencyStep
            
            DispatchQueue.main.async {
                self.currentFrequency = newFrequency
            }
            
            // Update EQ filter frequency
            if let band = frequencyEQ.bands.first {
                band.frequency = newFrequency
            }
            delegate?.audioManagerDidUpdateFrequency(newFrequency)
        }
        
        // Exponential smoothing for volume with easing
        let volumeDiff = targetVolume - currentFrequencyVolume
        if abs(volumeDiff) > 0.001 {
            let progress = min(1.0, interpolationSpeed * deltaTime)
            let easedProgress = UnifiedAudioEngineManager.easeInOutQuad(progress)
            let volumeStep = volumeDiff * easedProgress
            let newVolume = currentFrequencyVolume + volumeStep
            
            DispatchQueue.main.async {
                self.currentFrequencyVolume = newVolume
            }
            
            delegate?.audioManagerDidUpdateVolume(newVolume)
        }
        
        // Stop interpolation if we're close enough to targets
        if abs(frequencyDiff) <= 0.1 && abs(volumeDiff) <= 0.001 {
            // Snap to final values
            DispatchQueue.main.async {
                self.currentFrequency = self.targetFrequency
                self.currentFrequencyVolume = self.targetVolume
            }
            
            if let band = frequencyEQ.bands.first {
                band.frequency = targetFrequency
            }
            
            delegate?.audioManagerDidUpdateFrequency(targetFrequency)
            delegate?.audioManagerDidUpdateVolume(targetVolume)
            
            stopInterpolation()
        }
    }
}

// MARK: - Frequency Matching Interface
extension UnifiedAudioEngineManager {
    func startFrequencyMatching(frequency: Float, volume: Float) {
        updateFrequency(frequency)
        updateFrequencyVolume(volume)
        
        if !audioEngine.isRunning {
            startEngine()
        }
        
        // Start session tracking
        startSessionTracking()
        
        DispatchQueue.main.async {
            self.isFrequencyPlaying = true
            self.delegate?.audioManagerDidUpdatePlayingState(true)
            
            // Update lock screen media controls
            self.updateNowPlayingInfo()
        }
    }
    
    func stopFrequencyMatching() {
        // Stop session tracking
        stopSessionTracking()
        
        DispatchQueue.main.async {
            self.isFrequencyPlaying = false
            self.delegate?.audioManagerDidUpdatePlayingState(false)
            
            // Clear lock screen media controls
            self.clearNowPlayingInfo()
        }
        frequencyTime = 0.0
        stopEngine()
    }
    
    func updateFrequency(_ frequency: Float) {
        let clampedFrequency = max(20, min(16000, frequency))
        targetFrequency = clampedFrequency
        startInterpolation()
        
        // Update lock screen info if audio is playing
        if isFrequencyPlaying {
            DispatchQueue.main.async {
                self.updateNowPlayingInfo()
            }
        }
    }
    
    func updateFrequencyVolume(_ volume: Float) {
        let clampedVolume = max(0, min(1, volume))
        targetVolume = clampedVolume
        startInterpolation()
        
        // Update lock screen info if audio is playing
        if isFrequencyPlaying {
            DispatchQueue.main.async {
                self.updateNowPlayingInfo()
            }
        }
    }
    
    func setFrequencyAndVolume(frequency: Float, volume: Float) {
        let clampedFrequency = max(20, min(16000, frequency))
        let clampedVolume = max(0, min(1, volume))
        
        targetFrequency = clampedFrequency
        targetVolume = clampedVolume
        startInterpolation()
        
        // Update lock screen info if audio is playing
        if isFrequencyPlaying {
            DispatchQueue.main.async {
                self.updateNowPlayingInfo()
            }
        }
    }
    
    // For immediate updates during real-time interaction (no interpolation)
    func setFrequencyAndVolumeImmediate(frequency: Float, volume: Float) {
        let clampedFrequency = max(20, min(16000, frequency))
        let clampedVolume = max(0, min(1, volume))
        
        targetFrequency = clampedFrequency
        targetVolume = clampedVolume
        
        DispatchQueue.main.async {
            self.currentFrequency = clampedFrequency
            self.currentFrequencyVolume = clampedVolume
        }
        
        // Update EQ filter frequency immediately
        if let band = frequencyEQ.bands.first {
            band.frequency = clampedFrequency
        }
        
        delegate?.audioManagerDidUpdateFrequency(clampedFrequency)
        delegate?.audioManagerDidUpdateVolume(clampedVolume)
    }
}

// MARK: - Utility Extensions
extension UnifiedAudioEngineManager {
    static func frequencyFromNormalizedX(_ x: Float) -> Float {
        let minFreq = log10(20.0)
        let maxFreq = log10(16000.0)
        let logFreq = minFreq + Double(x) * (maxFreq - minFreq)
        return Float(pow(10, logFreq))
    }
    
    static func normalizedXFromFrequency(_ frequency: Float) -> Float {
        let minFreq = log10(20.0)
        let maxFreq = log10(16000.0)
        let logFreq = log10(Double(frequency))
        return Float((logFreq - minFreq) / (maxFreq - minFreq))
    }
    
    // Linear scaling for volume (ensures 50% at center position)
    static func volumeFromNormalizedY(_ y: Float) -> Float {
        // Simple linear mapping: y=0 -> volume=1.0, y=1 -> volume=0.0, y=0.5 -> volume=0.5
        return 1.0 - y
    }
    
    static func normalizedYFromVolume(_ volume: Float) -> Float {
        // Inverse of the linear scaling
        return 1.0 - volume
    }
    
    // MARK: - Easing Functions
    static func easeInOutQuad(_ t: Float) -> Float {
        if t < 0.5 {
            return 2.0 * t * t
        } else {
            return -1.0 + (4.0 - 2.0 * t) * t
        }
    }
    
    static func easeOutExpo(_ t: Float) -> Float {
        return t == 1.0 ? 1.0 : 1.0 - pow(2.0, -10.0 * t)
    }
}

// MARK: - Session Tracking
extension UnifiedAudioEngineManager {
    private func startSessionTracking() {
        guard sessionTimer == nil else { return }
        
        sessionStartTime = Date()
        currentSessionDuration = 0
        
        sessionTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            self?.updateSessionDuration()
        }
    }
    
    private func stopSessionTracking() {
        sessionTimer?.invalidate()
        sessionTimer = nil
        
        if let startTime = sessionStartTime {
            let duration = Date().timeIntervalSince(startTime)
            lastSessionDuration = duration
            totalSessionTime += duration
            
            // Save to UserDefaults for persistence
            UserDefaults.standard.set(lastSessionDuration, forKey: "lastSessionDuration")
            UserDefaults.standard.set(totalSessionTime, forKey: "totalSessionTime")
        }
        
        sessionStartTime = nil
        currentSessionDuration = 0
    }
    
    private func updateSessionDuration() {
        guard let startTime = sessionStartTime else { return }
        
        DispatchQueue.main.async {
            let newDuration = Date().timeIntervalSince(startTime)
            let durationChanged = abs(newDuration - self.currentSessionDuration) > 1.0 // Only update if changed by more than 1 second
            
            self.currentSessionDuration = newDuration
            
            // Only update Now Playing info periodically (every 5 seconds) to avoid spam
            if Int(newDuration) % 5 == 0 || durationChanged {
                self.updateNowPlayingInfo()
            }
        }
    }
    
    func getCurrentSessionInfo() -> (duration: TimeInterval, frequency: Float, volume: Float) {
        return (currentSessionDuration, currentFrequency, currentFrequencyVolume)
    }
    
    func getLastSessionInfo() -> (duration: TimeInterval, frequency: Float, volume: Float) {
        return (lastSessionDuration, currentFrequency, currentFrequencyVolume)
    }
    
    func loadSessionData() {
        lastSessionDuration = UserDefaults.standard.double(forKey: "lastSessionDuration")
        totalSessionTime = UserDefaults.standard.double(forKey: "totalSessionTime")
    }
    
    func saveAudioState() {
        let normalizedX = Self.normalizedXFromFrequency(currentFrequency)
        let normalizedY = Self.normalizedYFromVolume(currentFrequencyVolume)
        
        UserDefaults.standard.set(Double(normalizedX), forKey: "lastControlPositionX")
        UserDefaults.standard.set(Double(normalizedY), forKey: "lastControlPositionY")
        UserDefaults.standard.set(Double(currentFrequency), forKey: "lastFrequency")
        UserDefaults.standard.set(Double(currentFrequencyVolume), forKey: "lastVolume")
        
        UserDefaults.standard.synchronize()
        
        print("💾 Saved ball data - freq: \(String(format: "%.1f", currentFrequency))Hz, volume: \(String(format: "%.1f", currentFrequencyVolume * 100))%, position: (\(String(format: "%.3f", normalizedX)), \(String(format: "%.3f", normalizedY)))")
    }
    
    func loadAudioState() -> (frequency: Float, volume: Float, position: CGPoint) {
        let hasFreq = UserDefaults.standard.object(forKey: "lastFrequency") != nil
        let hasVol = UserDefaults.standard.object(forKey: "lastVolume") != nil
        let hasPosX = UserDefaults.standard.object(forKey: "lastControlPositionX") != nil
        let hasPosY = UserDefaults.standard.object(forKey: "lastControlPositionY") != nil
        
        let frequency = hasFreq ? Float(UserDefaults.standard.double(forKey: "lastFrequency")) : 1000.0
        let volume = hasVol ? Float(UserDefaults.standard.double(forKey: "lastVolume")) : 0.3
        let posX = hasPosX ? CGFloat(UserDefaults.standard.double(forKey: "lastControlPositionX")) : CGFloat(Self.normalizedXFromFrequency(frequency))
        let posY = hasPosY ? CGFloat(UserDefaults.standard.double(forKey: "lastControlPositionY")) : CGFloat(Self.normalizedYFromVolume(volume))
        
        if hasFreq && hasVol && hasPosX && hasPosY {
            print("📱 Loaded ball data - freq: \(String(format: "%.1f", frequency))Hz, volume: \(String(format: "%.1f", volume * 100))%, position: (\(String(format: "%.3f", posX)), \(String(format: "%.3f", posY)))")
        } else {
            print("📱 No saved data found, using defaults - freq: \(String(format: "%.1f", frequency))Hz, volume: \(String(format: "%.1f", volume * 100))%")
        }
        
        return (frequency, volume, CGPoint(x: posX, y: posY))
    }
    
    func clearLastSession() {
        lastSessionDuration = 0
        UserDefaults.standard.set(0, forKey: "lastSessionDuration")
    }
    
    func formatDuration(_ duration: TimeInterval) -> String {
        if duration < 60 {
            return String(format: "%.0f sec", duration)
        } else {
            let minutes = Int(duration) / 60
            let seconds = Int(duration) % 60
            return String(format: "%d min %d sec", minutes, seconds)
        }
    }
}
