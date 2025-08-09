import SwiftUI
import AVFoundation

struct FrequencyView: View {
    // Use local state instead of observing audio manager directly
    @State private var currentFrequency: Float = 1000.0
    @State private var currentVolume: Float = 0.5
    @State private var isPlaying: Bool = false
    
    // Audio manager reference (not observed)
    private let audioManager = UnifiedAudioEngineManager.shared
    
    // Strong reference to delegate to prevent deallocation
    @State private var audioDelegate: FrequencyViewDelegate?
    
    var body: some View {
        ZStack {
            // Full-screen background
            Color.black
                .ignoresSafeArea(.all)
            
            VStack(spacing: 50) {
                Spacer()
                
                // Current frequency display (using local state)
                VStack(spacing: 8) {
                    Text("\(Int(currentFrequency))")
                        .font(.system(size: 72, weight: .light, design: .monospaced))
                        .foregroundColor(.white)
                    
                    Text("Hz")
                        .font(.system(size: 24, weight: .light))
                        .foregroundColor(.white.opacity(0.7))
                }
                
                // Main frequency control - Large circular dial
                FrequencyDialControl(
                    frequency: $currentFrequency,
                    isPlaying: isPlaying,
                    onFrequencyChange: { newFreq in
                        audioManager.updateFrequency(newFreq)
                    }
                )
                .frame(width: 280, height: 280)
                
                Spacer()
                
                // Volume control
                VStack(spacing: 16) {
                    Text("Volume")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(.white.opacity(0.8))
                    
                    HStack(spacing: 20) {
                        Image(systemName: "speaker.fill")
                            .foregroundColor(.white.opacity(0.5))
                            .font(.system(size: 16))
                        
                        Slider(value: $currentVolume, in: 0...1)
                            .accentColor(.orange)
                            .onChange(of: currentVolume) { newValue in
                                audioManager.updateFrequencyVolume(newValue)
                            }
                        
                        Image(systemName: "speaker.wave.3.fill")
                            .foregroundColor(.white.opacity(0.5))
                            .font(.system(size: 16))
                    }
                    .padding(.horizontal, 40)
                }
                
                Spacer()
            }
            
            // Fixed play/pause button - always in the same position
            VStack {
                Spacer()
                
                Button(action: {
                    audioManager.togglePlayback()
                    HapticFeedback.light.trigger()
                }) {
                    ZStack {
                        Circle()
                            .fill(.ultraThinMaterial)
                            .frame(width: 80, height: 80)
                            .overlay(
                                Circle()
                                    .stroke(.white.opacity(0.2), lineWidth: 1)
                            )
                        
                        Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 32, weight: .medium))
                            .foregroundColor(.white)
                    }
                }
                .scaleEffect(isPlaying ? 1.1 : 1.0)
                .animation(.easeInOut(duration: 0.2), value: isPlaying)
                .padding(.bottom, 100) // Position above tab bar
            }
        }
        .onAppear {
            setupAudioDelegate()
            // Initialize local state with current audio manager values
            currentFrequency = audioManager.currentFrequency
            currentVolume = audioManager.currentFrequencyVolume
            isPlaying = audioManager.isFrequencyPlaying
        }
    }
    
    private func setupAudioDelegate() {
        let delegate = FrequencyViewDelegate(
            onFrequencyUpdate: { freq in currentFrequency = freq },
            onVolumeUpdate: { vol in currentVolume = vol },
            onPlayingStateUpdate: { playing in isPlaying = playing }
        )
        
        // Store strong reference to prevent deallocation
        audioDelegate = delegate
        audioManager.delegate = delegate
    }
}

// Delegate implementation for FrequencyView
private class FrequencyViewDelegate: AudioManagerDelegate {
    private let onFrequencyUpdate: (Float) -> Void
    private let onVolumeUpdate: (Float) -> Void
    private let onPlayingStateUpdate: (Bool) -> Void
    
    init(onFrequencyUpdate: @escaping (Float) -> Void,
         onVolumeUpdate: @escaping (Float) -> Void,
         onPlayingStateUpdate: @escaping (Bool) -> Void) {
        self.onFrequencyUpdate = onFrequencyUpdate
        self.onVolumeUpdate = onVolumeUpdate
        self.onPlayingStateUpdate = onPlayingStateUpdate
    }
    
    func audioManagerDidUpdateFrequency(_ frequency: Float) {
        DispatchQueue.main.async {
            self.onFrequencyUpdate(frequency)
        }
    }
    
    func audioManagerDidUpdateVolume(_ volume: Float) {
        DispatchQueue.main.async {
            self.onVolumeUpdate(volume)
        }
    }
    
    func audioManagerDidUpdatePlayingState(_ isPlaying: Bool) {
        DispatchQueue.main.async {
            self.onPlayingStateUpdate(isPlaying)
        }
    }
}

struct FrequencyDialControl: View {
    @Binding var frequency: Float
    let isPlaying: Bool
    let onFrequencyChange: (Float) -> Void
    
    @State private var angle: Double = 0
    @State private var isDragging = false
    
    // Frequency range: 20Hz - 16kHz (logarithmic scale)
    private let minFreq: Float = 20
    private let maxFreq: Float = 16000
    
    var body: some View {
        ZStack {
            // Outer ring
            Circle()
                .stroke(.white.opacity(0.1), lineWidth: 3)
            
            // Progress ring
            Circle()
                .trim(from: 0, to: progressValue)
                .stroke(
                    LinearGradient(
                        colors: [.orange, .yellow, .orange],
                        startPoint: .leading,
                        endPoint: .trailing
                    ),
                    style: StrokeStyle(lineWidth: 4, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
                .animation(.easeOut(duration: 0.3), value: progressValue)
            
            // Center control point
            ZStack {
                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 120, height: 120)
                
                Circle()
                    .fill(isPlaying ? .orange : .white.opacity(0.3))
                    .frame(width: 80, height: 80)
                    .scaleEffect(isDragging ? 1.1 : 1.0)
                    .animation(.easeInOut(duration: 0.2), value: isDragging)
                
                VStack(spacing: 2) {
                    Image(systemName: "waveform")
                        .font(.system(size: 20, weight: .medium))
                        .foregroundColor(isPlaying ? .white : .primary)
                    
                    Text("FREQ")
                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                        .foregroundColor(isPlaying ? .white : .primary)
                }
            }
            
            // Frequency markers
            ForEach(frequencyMarkers, id: \.frequency) { marker in
                VStack {
                    Rectangle()
                        .fill(.white.opacity(marker.isMainMarker ? 0.8 : 0.4))
                        .frame(width: 2, height: marker.isMainMarker ? 20 : 12)
                    
                    if marker.isMainMarker {
                        Text(marker.label)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(.white.opacity(0.6))
                            .padding(.top, 4)
                    }
                    
                    Spacer()
                }
                .rotationEffect(.degrees(marker.angle - 90))
                .offset(y: -140)
                .rotationEffect(.degrees(90 - marker.angle))
            }
        }
        .rotationEffect(.degrees(angle))
        .gesture(
            DragGesture()
                .onChanged { value in
                    if !isDragging {
                        isDragging = true
                        HapticFeedback.light.trigger()
                    }
                    
                    let center = CGPoint(x: 140, y: 140) // Half of frame size
                    let vector = CGPoint(x: value.location.x - center.x, y: value.location.y - center.y)
                    let newAngle = atan2(vector.y, vector.x) * 180 / .pi
                    
                    angle = newAngle
                    updateFrequencyFromAngle(newAngle)
                }
                .onEnded { _ in
                    isDragging = false
                    HapticFeedback.medium.trigger()
                }
        )
        .onAppear {
            // Set initial angle based on current frequency
            angle = angleFromFrequency(frequency)
        }
        .onChange(of: frequency) { newFreq in
            // Update angle when frequency changes externally
            let newAngle = angleFromFrequency(newFreq)
            if abs(newAngle - angle) > 5 { // Only update if significant change
                angle = newAngle
            }
        }
    }
    
    private var progressValue: Double {
        let normalizedFreq = (log(frequency) - log(minFreq)) / (log(maxFreq) - log(minFreq))
        return Double(normalizedFreq)
    }
    
    private func updateFrequencyFromAngle(_ angle: Double) {
        // Convert angle to frequency (logarithmic scale)
        let normalizedAngle = (angle + 90) / 360 // Normalize to 0-1
        let clampedAngle = max(0, min(1, normalizedAngle))
        
        let logFreq = log(minFreq) + Float(clampedAngle) * (log(maxFreq) - log(minFreq))
        let newFrequency = exp(logFreq)
        
        let clampedFreq = max(minFreq, min(maxFreq, newFrequency))
        frequency = clampedFreq
        onFrequencyChange(clampedFreq)
    }
    
    private func angleFromFrequency(_ freq: Float) -> Double {
        let normalizedFreq = (log(freq) - log(minFreq)) / (log(maxFreq) - log(minFreq))
        return Double(normalizedFreq * 360 - 90)
    }
    
    private var frequencyMarkers: [FrequencyMarker] {
        let frequencies: [(Float, String, Bool)] = [
            (100, "100", true),
            (500, "500", true),
            (1000, "1K", true),
            (2000, "2K", true),
            (5000, "5K", true),
            (10000, "10K", true),
            (15000, "15K", true)
        ]
        
        return frequencies.map { freq, label, isMain in
            let normalizedFreq = (log(freq) - log(minFreq)) / (log(maxFreq) - log(minFreq))
            let angle = Double(normalizedFreq * 360)
            return FrequencyMarker(frequency: freq, label: label, angle: angle, isMainMarker: isMain)
        }
    }
}

struct FrequencyMarker {
    let frequency: Float
    let label: String
    let angle: Double
    let isMainMarker: Bool
}

// Extension to UnifiedAudioEngineManager for play/pause toggle
extension UnifiedAudioEngineManager {
    func togglePlayback() {
        if isFrequencyPlaying {
            stopFrequencyMatching()
        } else {
            startFrequencyMatching(frequency: currentFrequency, volume: currentFrequencyVolume)
        }
    }
}

#Preview {
    FrequencyView()
}