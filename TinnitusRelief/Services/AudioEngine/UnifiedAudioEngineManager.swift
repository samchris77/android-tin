import AVFoundation
import Foundation
import Combine
import QuartzCore

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
    
    // Interpolation properties
    private var targetFrequency: Float = 440.0
    private var targetVolume: Float = 0.5
    private var displayLink: CADisplayLink?
    private var lastUpdateTime: CFTimeInterval = 0
    private let interpolationSpeed: Float = 8.0 // Higher = faster interpolation
    
    // Delegate
    weak var delegate: AudioManagerDelegate?
    
    private var cancellables = Set<AnyCancellable>()
    
    private init() {
        setupAudioSession()
        setupEngine()
        setupAudioInterruptionHandling()
    }
    
    deinit {
        stopInterpolation()
        stopAll()
    }
    
    private func setupAudioSession() {
        do {
            try audioSession.setCategory(.playback, mode: .default, options: [.allowBluetooth, .allowBluetoothA2DP])
            try audioSession.setActive(true)
            sampleRate = audioSession.sampleRate
        } catch {
            print("Failed to setup audio session: \(error)")
        }
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
            let amplitude = self.currentFrequencyVolume * 0.1 // Optimized amplitude for white noise + EQ boost
            
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
        
        // Configure the EQ bandpass filter for targeted frequency emphasis
        let band = frequencyEQ.bands[0]
        band.filterType = .bandPass
        band.frequency = currentFrequency // Will be updated dynamically
        band.bandwidth = 1.5 // Octaves - optimized for smooth frequency focus
        band.gain = 6.0 // Moderate boost to emphasize the target frequency
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
    
    private func stopInterpolation() {
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
        
        DispatchQueue.main.async {
            self.isFrequencyPlaying = true
            self.delegate?.audioManagerDidUpdatePlayingState(true)
        }
    }
    
    func stopFrequencyMatching() {
        DispatchQueue.main.async {
            self.isFrequencyPlaying = false
            self.delegate?.audioManagerDidUpdatePlayingState(false)
        }
        frequencyTime = 0.0
        stopEngine()
    }
    
    func updateFrequency(_ frequency: Float) {
        let clampedFrequency = max(20, min(16000, frequency))
        targetFrequency = clampedFrequency
        startInterpolation()
    }
    
    func updateFrequencyVolume(_ volume: Float) {
        let clampedVolume = max(0, min(1, volume))
        targetVolume = clampedVolume
        startInterpolation()
    }
    
    func setFrequencyAndVolume(frequency: Float, volume: Float) {
        let clampedFrequency = max(20, min(16000, frequency))
        let clampedVolume = max(0, min(1, volume))
        
        targetFrequency = clampedFrequency
        targetVolume = clampedVolume
        startInterpolation()
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
    
    // Perceptual scaling for volume (logarithmic response that matches human hearing)
    static func volumeFromNormalizedY(_ y: Float) -> Float {
        // Apply ease-out curve for more natural volume perception
        let linear = 1.0 - y
        // Use power curve to match human volume perception
        return pow(linear, 0.5) // Square root curve feels more natural
    }
    
    static func normalizedYFromVolume(_ volume: Float) -> Float {
        // Inverse of the perceptual scaling
        let perceputalVolume = pow(volume, 2.0) // Square to invert the square root
        return 1.0 - perceputalVolume
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