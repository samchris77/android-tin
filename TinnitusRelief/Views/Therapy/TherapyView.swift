import SwiftUI

struct TherapyView: View {
    @Binding var selectedTab: Int
    @State private var selectedTherapyMode = 0 // 0 = Frequency Match, 1 = Timer
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Segmented Control
                Picker("Therapy Mode", selection: $selectedTherapyMode) {
                    Text("Frequency Match").tag(0)
                    Text("Timer").tag(1)
                }
                .pickerStyle(.segmented)
                .padding()
                .background(Color(.systemGroupedBackground))
                
                // Content based on selection
                Group {
                    if selectedTherapyMode == 0 {
                        FrequencyMatchingTherapyView()
                    } else {
                        SleepTimerTherapyView()
                    }
                }
                .animation(.easeInOut(duration: 0.3), value: selectedTherapyMode)
            }
            .navigationTitle("Therapy")
            .navigationBarTitleDisplayMode(.large)
        }
    }
}

struct FrequencyMatchingTherapyView: View {
    @StateObject private var viewModel = FrequencyMatchingViewModel()
    @StateObject private var audioManager = UnifiedAudioEngineManager.shared
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Header Section
                VStack(spacing: 12) {
                    Image(systemName: "waveform.path")
                        .font(.system(size: 48))
                        .foregroundColor(.orange)
                    
                    Text("Frequency Matching")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                    
                    Text("Find and match your tinnitus frequency for personalized therapy")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(.ultraThinMaterial)
                        .shadow(radius: 4, x: 0, y: 2)
                )
                
                // Frequency Control Area
                VStack(spacing: 16) {
                    Text("Drag the control point to match your tinnitus")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    // Main frequency control
                    FrequencyMatchingView(selectedTab: .constant(1))
                        .frame(height: 300)
                    
                    // Current values display
                    HStack(spacing: 32) {
                        VStack(spacing: 4) {
                            Text("Frequency")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("\(Int(audioManager.currentFrequency)) Hz")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.orange)
                        }
                        
                        VStack(spacing: 4) {
                            Text("Volume")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("\(Int(audioManager.currentFrequencyVolume * 100))%")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.blue)
                        }
                    }
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.ultraThinMaterial)
                    )
                }
                
                // Control Buttons
                HStack(spacing: 16) {
                    Button(action: {
                        if audioManager.isFrequencyPlaying {
                            audioManager.stopFrequencyMatching()
                        } else {
                            audioManager.startFrequencyMatching(
                                frequency: audioManager.currentFrequency,
                                volume: audioManager.currentFrequencyVolume
                            )
                        }
                    }) {
                        HStack {
                            Image(systemName: audioManager.isFrequencyPlaying ? "stop.fill" : "play.fill")
                            Text(audioManager.isFrequencyPlaying ? "Stop" : "Play")
                        }
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(
                            LinearGradient(
                                colors: audioManager.isFrequencyPlaying ? 
                                    [Color.red, Color.orange] : 
                                    [Color.orange, Color.yellow],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .cornerRadius(12)
                        .shadow(radius: 4, x: 0, y: 2)
                    }
                    
                    Button(action: {
                        // Save current frequency as preferred
                    }) {
                        HStack {
                            Image(systemName: "bookmark.fill")
                            Text("Save")
                        }
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.orange)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.orange, lineWidth: 2)
                                .background(Color.clear)
                        )
                    }
                }
                
                Spacer(minLength: 20)
            }
            .padding()
        }
    }
}

struct SleepTimerTherapyView: View {
    @State private var selectedDuration: TimeInterval = 1800
    @State private var isTimerActive = false
    @State private var customMinutes: String = ""
    @State private var isUsingCustomDuration = false
    
    let durations: [TimeInterval] = [900, 1800, 2700, 3600]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Header Section
                VStack(spacing: 12) {
                    Image(systemName: "moon.fill")
                        .font(.system(size: 48))
                        .foregroundColor(.purple)
                    
                    Text("Sleep Timer")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                    
                    Text("Gradually fade out therapy sounds for better sleep")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(.ultraThinMaterial)
                        .shadow(radius: 4, x: 0, y: 2)
                )
                
                // Duration Selection
                VStack(spacing: 16) {
                    Text("Select Duration")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 2), spacing: 12) {
                        ForEach(durations, id: \.self) { duration in
                            Button(action: {
                                selectedDuration = duration
                                isUsingCustomDuration = false
                            }) {
                                VStack(spacing: 8) {
                                    Text(formatDuration(duration))
                                        .font(.title2)
                                        .fontWeight(.semibold)
                                    
                                    Text("minutes")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                .frame(height: 80)
                                .frame(maxWidth: .infinity)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill((selectedDuration == duration && !isUsingCustomDuration) ? 
                                              Color.purple.opacity(0.2) : 
                                              Color.gray.opacity(0.1))
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke((selectedDuration == duration && !isUsingCustomDuration) ? 
                                                        Color.purple : Color.clear, lineWidth: 2)
                                        )
                                )
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    
                    // Custom Duration
                    VStack(spacing: 12) {
                        Text("Or set custom duration")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        HStack(spacing: 12) {
                            TextField("Enter minutes", text: $customMinutes)
                                .keyboardType(.numberPad)
                                .textFieldStyle(.roundedBorder)
                                .frame(maxWidth: 120)
                                .onChange(of: customMinutes) { _ in
                                    updateCustomDuration()
                                }
                            
                            Text("minutes")
                                .font(.body)
                                .foregroundColor(.secondary)
                            
                            Spacer()
                        }
                        .padding()
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(isUsingCustomDuration ? 
                                      Color.purple.opacity(0.2) : 
                                      Color.gray.opacity(0.1))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(isUsingCustomDuration ? Color.purple : Color.clear, lineWidth: 2)
                                )
                        )
                    }
                }
                
                // Timer Control
                Button(action: {
                    isTimerActive.toggle()
                }) {
                    HStack {
                        Image(systemName: isTimerActive ? "stop.fill" : "timer")
                        Text(isTimerActive ? "Stop Timer" : "Start Timer")
                    }
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(
                        LinearGradient(
                            colors: isTimerActive ? 
                                [Color.red, Color.orange] : 
                                [Color.purple, Color.blue],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(12)
                    .shadow(radius: 4, x: 0, y: 2)
                }
                
                Spacer(minLength: 20)
            }
            .padding()
        }
    }
    
    private func updateCustomDuration() {
        guard let minutes = Int(customMinutes), minutes > 0, minutes <= 120 else {
            isUsingCustomDuration = false
            return
        }
        
        selectedDuration = TimeInterval(minutes * 60)
        isUsingCustomDuration = true
    }
    
    private func formatDuration(_ duration: TimeInterval) -> String {
        let minutes = Int(duration / 60)
        return "\(minutes)"
    }
}

#Preview {
    TherapyView(selectedTab: .constant(1))
}