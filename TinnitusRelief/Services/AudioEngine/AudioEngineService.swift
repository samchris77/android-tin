import AVFoundation
import Foundation
import Accelerate

class AudioEngineService: ObservableObject {
    private let engine = AVAudioEngine()
    private var sourceNode: AVAudioSourceNode!
    private let mixer = AVAudioMixerNode()
    
    @Published var isPlaying = false
    @Published var currentFrequency: Float = 440.0
    @Published var currentVolume: Float = 0.5
    
    private let audioSession = AVAudioSession.sharedInstance()
    private var sampleRate: Double = 44100.0
    private var time: Float = 0.0
    private let twoPi = 2.0 * Float.pi
    
    init() {
        setupAudioSession()
        setupEngine()
        setupAudioInterruptionHandling()
    }
    
    deinit {
        stopEngine()
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
        let format = AVAudioFormat(standardFormatWithSampleRate: sampleRate, channels: 1)!
        
        // Initialize source node with render block
        sourceNode = AVAudioSourceNode { [weak self] (isSilence, timestamp, frameCount, audioBufferList) -> OSStatus in
            guard let self = self else { return noErr }
            
            let ablPointer = UnsafeMutableAudioBufferListPointer(audioBufferList)
            
            for frame in 0..<Int(frameCount) {
                let amplitude = self.currentVolume * 0.25
                let frequency = self.currentFrequency
                
                let sampleValue = amplitude * sin(self.twoPi * frequency * self.time / Float(self.sampleRate))
                self.time += 1.0
                
                if self.time > Float(self.sampleRate) {
                    self.time -= Float(self.sampleRate)
                }
                
                for buffer in ablPointer {
                    let buf: UnsafeMutableBufferPointer<Float> = UnsafeMutableBufferPointer(buffer)
                    buf[frame] = sampleValue
                }
            }
            
            return noErr
        }
        
        engine.attach(sourceNode)
        engine.attach(mixer)
        
        engine.connect(sourceNode, to: mixer, format: format)
        engine.connect(mixer, to: engine.mainMixerNode, format: format)
        
        engine.prepare()
    }
    
    private func setupAudioInterruptionHandling() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleAudioInterruption),
            name: AVAudioSession.interruptionNotification,
            object: nil
        )
    }
    
    func startEngine() {
        guard !engine.isRunning else { return }
        
        do {
            try engine.start()
            DispatchQueue.main.async {
                self.isPlaying = true
            }
        } catch {
            print("Failed to start engine: \(error)")
        }
    }
    
    func stopEngine() {
        guard engine.isRunning else { return }
        
        engine.stop()
        DispatchQueue.main.async {
            self.isPlaying = false
        }
        time = 0.0
    }
    
    func updateFrequency(_ frequency: Float) {
        let clampedFrequency = max(20, min(20000, frequency))
        currentFrequency = clampedFrequency
    }
    
    func updateVolume(_ volume: Float) {
        let clampedVolume = max(0, min(1, volume))
        currentVolume = clampedVolume
    }
    
    func setFrequencyAndVolume(frequency: Float, volume: Float) {
        updateFrequency(frequency)
        updateVolume(volume)
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
                startEngine()
            }
        @unknown default:
            break
        }
    }
}

extension AudioEngineService {
    func playTinnitusMatchingTone(frequency: Float, volume: Float) {
        setFrequencyAndVolume(frequency: frequency, volume: volume)
        startEngine()
    }
    
    func stopTinnitusMatchingTone() {
        stopEngine()
    }
}

extension AudioEngineService {
    static func frequencyFromNormalizedX(_ x: Float) -> Float {
        let minFreq = log10(20.0)
        let maxFreq = log10(20000.0)
        let logFreq = minFreq + Double(x) * (maxFreq - minFreq)
        return Float(pow(10, logFreq))
    }
    
    static func normalizedXFromFrequency(_ frequency: Float) -> Float {
        let minFreq = log10(20.0)
        let maxFreq = log10(20000.0)
        let logFreq = log10(Double(frequency))
        return Float((logFreq - minFreq) / (maxFreq - minFreq))
    }
    
    static func volumeFromNormalizedY(_ y: Float) -> Float {
        return 1.0 - y
    }
    
    static func normalizedYFromVolume(_ volume: Float) -> Float {
        return 1.0 - volume
    }
}