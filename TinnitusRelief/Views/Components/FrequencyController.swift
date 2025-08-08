import SwiftUI
import AVFoundation

struct FrequencyController: View {
    @ObservedObject private var audioManager = UnifiedAudioEngineManager.shared
    @State private var isExpanded = false
    
    var body: some View {
        VStack(spacing: 0) {
            if isExpanded {
                expandedController
            } else {
                compactController
            }
        }
        .background(.ultraThinMaterial)
        .cornerRadius(12, corners: [.topLeft, .topRight])
        .shadow(radius: 8, y: -2)
        .animation(.spring(response: 0.4, dampingFraction: 0.8), value: isExpanded)
    }
    
    private var compactController: some View {
        HStack(spacing: 16) {
            // Play/Pause Button
            Button(action: togglePlayback) {
                Image(systemName: audioManager.isFrequencyPlaying ? "pause.fill" : "play.fill")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.orange)
                    .frame(width: 32, height: 32)
                    .background(
                        Circle()
                            .fill(Color.orange.opacity(0.1))
                    )
            }
            
            // Frequency Display
            VStack(alignment: .leading, spacing: 2) {
                Text("Frequency Match")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Text("\(formatFrequency(audioManager.currentFrequency))")
                    .font(.system(size: 14, weight: .semibold, design: .monospaced))
                    .foregroundColor(.primary)
            }
            
            Spacer()
            
            // Volume Display
            VStack(alignment: .trailing, spacing: 2) {
                Text("Volume")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Text("\(Int(audioManager.currentFrequencyVolume * 100))%")
                    .font(.system(size: 14, weight: .semibold, design: .monospaced))
                    .foregroundColor(.primary)
            }
            
            // Expand Button
            Button(action: { isExpanded.toggle() }) {
                Image(systemName: "chevron.up")
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.secondary)
                    .rotationEffect(.degrees(isExpanded ? 180 : 0))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .onTapGesture {
            isExpanded.toggle()
        }
    }
    
    private var expandedController: some View {
        VStack(spacing: 16) {
            // Header
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Frequency Match")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    Text("Active Session")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                Button(action: togglePlayback) {
                    Image(systemName: audioManager.isFrequencyPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(.white)
                        .frame(width: 40, height: 40)
                        .background(
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [Color.orange, Color.red],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                        )
                }
                
                Button(action: { isExpanded.toggle() }) {
                    Image(systemName: "chevron.down")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.secondary)
                }
            }
            
            // Controls
            VStack(spacing: 12) {
                // Frequency Control
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("Frequency")
                            .font(.body)
                            .foregroundColor(.primary)
                        
                        Spacer()
                        
                        Text(formatFrequency(audioManager.currentFrequency))
                            .font(.system(size: 16, weight: .semibold, design: .monospaced))
                            .foregroundColor(.orange)
                    }
                    
                    HStack {
                        Text("20 Hz")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Slider(
                            value: Binding(
                                get: { UnifiedAudioEngineManager.normalizedXFromFrequency(audioManager.currentFrequency) },
                                set: { newValue in
                                    let frequency = UnifiedAudioEngineManager.frequencyFromNormalizedX(newValue)
                                    audioManager.updateFrequency(frequency)
                                }
                            ),
                            in: 0...1
                        )
                        .accentColor(.orange)
                        
                        Text("20 kHz")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                
                // Volume Control
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("Volume")
                            .font(.body)
                            .foregroundColor(.primary)
                        
                        Spacer()
                        
                        Text("\(Int(audioManager.currentFrequencyVolume * 100))%")
                            .font(.system(size: 16, weight: .semibold, design: .monospaced))
                            .foregroundColor(.orange)
                    }
                    
                    HStack {
                        Image(systemName: "speaker.fill")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Slider(
                            value: Binding(
                                get: { audioManager.currentFrequencyVolume },
                                set: { newValue in
                                    audioManager.updateFrequencyVolume(newValue)
                                }
                            ),
                            in: 0...1
                        )
                        .accentColor(.orange)
                        
                        Image(systemName: "speaker.wave.3.fill")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            // Action Buttons
            HStack(spacing: 12) {
                Button(action: { audioManager.stopFrequencyMatching() }) {
                    Text("Stop")
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .frame(height: 40)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.red.opacity(0.1))
                        )
                }
                
                Button(action: {}) {
                    Text("Save")
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(.orange)
                        .frame(maxWidth: .infinity)
                        .frame(height: 40)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.orange.opacity(0.1))
                        )
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 16)
    }
    
    private func togglePlayback() {
        if audioManager.isFrequencyPlaying {
            audioManager.stopFrequencyMatching()
        } else {
            audioManager.startFrequencyMatching(
                frequency: audioManager.currentFrequency,
                volume: audioManager.currentFrequencyVolume
            )
        }
    }
    
    private func formatFrequency(_ frequency: Float) -> String {
        if frequency >= 1000 {
            return String(format: "%.1f kHz", frequency / 1000)
        } else {
            return String(format: "%.0f Hz", frequency)
        }
    }
}


#Preview {
    VStack {
        Spacer()
        FrequencyController()
    }
    .background(Color.gray.opacity(0.1))
}