import SwiftUI
import AVFoundation

struct FrequencyController: View {
    @ObservedObject private var audioManager = UnifiedAudioEngineManager.shared
    
    var body: some View {
        infoBar
            .background(.ultraThinMaterial)
            .cornerRadius(12, corners: [.bottomLeft, .bottomRight])
    }
    
    private var infoBar: some View {
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
                Text("Frequency")
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
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
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