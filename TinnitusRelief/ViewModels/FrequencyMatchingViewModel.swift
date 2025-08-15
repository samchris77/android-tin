import SwiftUI
import Combine
import CoreData
import QuartzCore

class FrequencyMatchingViewModel: ObservableObject {
    @Published var controlPosition: CGPoint = CGPoint(x: 0.5, y: 0.5)
    @Published var isDragging = false
    @Published var currentFrequency: Float = 440.0
    @Published var currentVolume: Float = 0.5
    @Published var isPlaying = false
    
    private let audioManager = UnifiedAudioEngineManager.shared
    private var cancellables = Set<AnyCancellable>()
    private let hapticFeedback = UIImpactFeedbackGenerator(style: .light)
    
    // Real-time update system
    private var displayLink: CADisplayLink?
    private var pendingPosition: CGPoint?
    private var lastUpdateTime: CFTimeInterval = 0
    
    // Loading state guard
    private var isLoadingState = false
    
    init() {
        print("🏗️ FrequencyMatchingViewModel init called")
        setupBindings()
        setupInitialPosition()
        print("🏗️ FrequencyMatchingViewModel init complete")
    }
    
    private func setupBindings() {
        audioManager.$isFrequencyPlaying
            .receive(on: DispatchQueue.main)
            .assign(to: \.isPlaying, on: self)
            .store(in: &cancellables)
        
        audioManager.$currentFrequency
            .receive(on: DispatchQueue.main)
            .assign(to: \.currentFrequency, on: self)
            .store(in: &cancellables)
        
        audioManager.$currentFrequencyVolume
            .receive(on: DispatchQueue.main)
            .assign(to: \.currentVolume, on: self)
            .store(in: &cancellables)
        
        // Update control position when frequency or volume changes externally
        Publishers.CombineLatest(audioManager.$currentFrequency, audioManager.$currentFrequencyVolume)
            .receive(on: DispatchQueue.main)
            .sink { [weak self] frequency, volume in
                guard let self = self else { return }
                
                // Don't update position during loading or dragging
                guard !self.isLoadingState && !self.isDragging else {
                    return
                }
                
                let normalizedX = UnifiedAudioEngineManager.normalizedXFromFrequency(frequency)
                let normalizedY = UnifiedAudioEngineManager.normalizedYFromVolume(volume)
                let newPosition = CGPoint(x: CGFloat(normalizedX), y: CGFloat(normalizedY))
                
                self.controlPosition = newPosition
            }
            .store(in: &cancellables)
        
        // Remove debouncing for real-time updates during dragging
        $controlPosition
            .sink { [weak self] position in
                guard let self = self else { return }
                if self.isDragging {
                    // Immediate updates during dragging
                    self.updateAudioFromPositionImmediate(position)
                } else {
                    // Smooth interpolated updates when not dragging
                    self.pendingPosition = position
                    self.startRealTimeUpdates()
                }
            }
            .store(in: &cancellables)
    }
    
    private func setupInitialPosition() {
        print("🎯 setupInitialPosition called")
        guard !isLoadingState else { 
            print("⚠️ setupInitialPosition blocked - already loading")
            return 
        }
        
        isLoadingState = true
        let savedState = audioManager.loadAudioState()
        
        audioManager.stopInterpolation()
        audioManager.updateFrequency(savedState.frequency)
        audioManager.updateFrequencyVolume(savedState.volume)
        controlPosition = savedState.position
        
        print("🎯 setupInitialPosition complete - position set to \(savedState.position)")
        isLoadingState = false
    }
    
    
    private func updateAudioFromPosition(_ position: CGPoint) {
        let frequency = UnifiedAudioEngineManager.frequencyFromNormalizedX(Float(position.x))
        let volume = UnifiedAudioEngineManager.volumeFromNormalizedY(Float(position.y))
        
        audioManager.setFrequencyAndVolume(frequency: frequency, volume: volume)
    }
    
    private func updateAudioFromPositionImmediate(_ position: CGPoint) {
        let frequency = UnifiedAudioEngineManager.frequencyFromNormalizedX(Float(position.x))
        let volume = UnifiedAudioEngineManager.volumeFromNormalizedY(Float(position.y))
        
        audioManager.setFrequencyAndVolumeImmediate(frequency: frequency, volume: volume)
    }
    
    // MARK: - Real-time Update System
    private func startRealTimeUpdates() {
        guard displayLink == nil else { return }
        
        displayLink = CADisplayLink(target: self, selector: #selector(processRealTimeUpdates))
        displayLink?.preferredFramesPerSecond = 60
        displayLink?.add(to: .main, forMode: .common)
        lastUpdateTime = CACurrentMediaTime()
    }
    
    private func stopRealTimeUpdates() {
        displayLink?.invalidate()
        displayLink = nil
        pendingPosition = nil
    }
    
    @objc private func processRealTimeUpdates() {
        guard let position = pendingPosition else {
            stopRealTimeUpdates()
            return
        }
        
        updateAudioFromPosition(position)
        pendingPosition = nil
        stopRealTimeUpdates()
    }
    
    func startDragging() {
        isDragging = true
        hapticFeedback.impactOccurred()
        
        if !isPlaying {
            startPlaying()
        }
    }
    
    func stopDragging() {
        isDragging = false
        // Allow smooth interpolation when dragging stops
        if let position = pendingPosition {
            updateAudioFromPosition(position)
        }
        // Save audio state once when dragging stops
        audioManager.saveAudioState()
    }
    
    func updateControlPosition(to position: CGPoint, in size: CGSize) {
        let normalizedX = max(0, min(1, position.x / size.width))
        let normalizedY = max(0, min(1, position.y / size.height))
        
        let newPosition = CGPoint(x: normalizedX, y: normalizedY)
        
        // Avoid unnecessary updates if position hasn't changed significantly
        let threshold: CGFloat = 0.001
        let deltaX = abs(newPosition.x - controlPosition.x)
        let deltaY = abs(newPosition.y - controlPosition.y)
        
        if deltaX > threshold || deltaY > threshold {
            controlPosition = newPosition
        }
    }
    
    func startPlaying() {
        audioManager.startFrequencyMatching(frequency: currentFrequency, volume: currentVolume)
    }
    
    func stopPlaying() {
        audioManager.stopFrequencyMatching()
    }
    
    func saveMatchedFrequency() {
        // TODO: Save to Core Data
        // Note: Audio continues playing - managed by UnifiedAudioEngineManager
    }
    
    func getFrequencyDisplayText() -> String {
        if currentFrequency < 1000 {
            return String(format: "%.0f Hz", currentFrequency)
        } else {
            return String(format: "%.1f kHz", currentFrequency / 1000)
        }
    }
    
    func getVolumeDisplayText() -> String {
        return String(format: "%.0f%%", currentVolume * 100)
    }
    
    deinit {
        stopRealTimeUpdates()
    }
}