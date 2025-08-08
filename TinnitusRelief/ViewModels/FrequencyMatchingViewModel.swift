import SwiftUI
import Combine
import CoreData

class FrequencyMatchingViewModel: ObservableObject {
    @Published var controlPosition: CGPoint = CGPoint(x: 0.5, y: 0.5)
    @Published var isDragging = false
    @Published var currentFrequency: Float = 440.0
    @Published var currentVolume: Float = 0.5
    @Published var isPlaying = false
    
    private let audioManager = UnifiedAudioEngineManager.shared
    private var cancellables = Set<AnyCancellable>()
    private let hapticFeedback = UIImpactFeedbackGenerator(style: .light)
    
    init() {
        setupBindings()
        setupInitialPosition()
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
        
        $controlPosition
            .debounce(for: .milliseconds(16), scheduler: DispatchQueue.main)
            .sink { [weak self] position in
                self?.updateAudioFromPosition(position)
            }
            .store(in: &cancellables)
    }
    
    private func setupInitialPosition() {
        let initialFrequency: Float = 1000.0
        let initialVolume: Float = 0.3
        
        audioManager.updateFrequency(initialFrequency)
        audioManager.updateFrequencyVolume(initialVolume)
        
        let normalizedX = UnifiedAudioEngineManager.normalizedXFromFrequency(initialFrequency)
        let normalizedY = UnifiedAudioEngineManager.normalizedYFromVolume(initialVolume)
        
        controlPosition = CGPoint(x: CGFloat(normalizedX), y: CGFloat(normalizedY))
    }
    
    private func updateAudioFromPosition(_ position: CGPoint) {
        let frequency = UnifiedAudioEngineManager.frequencyFromNormalizedX(Float(position.x))
        let volume = UnifiedAudioEngineManager.volumeFromNormalizedY(Float(position.y))
        
        audioManager.setFrequencyAndVolume(frequency: frequency, volume: volume)
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
    }
    
    func updateControlPosition(to position: CGPoint, in size: CGSize) {
        let normalizedX = max(0, min(1, position.x / size.width))
        let normalizedY = max(0, min(1, position.y / size.height))
        
        controlPosition = CGPoint(x: normalizedX, y: normalizedY)
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
        // Note: Audio continues playing - will be managed by FrequencyController
        // Only cleanup the Combine subscriptions (handled automatically)
    }
}