import SwiftUI

struct LogSessionView: View {
    @StateObject private var viewModel = DiaryViewModel()
    @StateObject private var audioManager = UnifiedAudioEngineManager.shared
    @State private var loudness: Int = 5
    @State private var stress: Int = 3
    @State private var showSuccessMessage = false
    @State private var manualSessionDuration: TimeInterval?
    @State private var showingDurationPicker = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Header Section
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Log Your Session")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                        Text(Date().formatted(date: .abbreviated, time: .omitted))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // Main logging sliders
                    VStack(spacing: 16) {
                        SymptomSlider(
                            title: "Tinnitus Level",
                            value: $loudness,
                            color: .orange
                        )
                        SymptomSlider(
                            title: "Current Stress",
                            value: $stress,
                            color: .red
                        )
                    }
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(.ultraThinMaterial)
                    )
                    
                    // Last Sound Session
                    lastSoundSessionSection
                    
                    
                    // Save Button
                    Button(action: saveEntry) {
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                            Text("Save Entry")
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.orange)
                        .cornerRadius(12)
                    }
                    
                    Spacer(minLength: 20)
                }
                .padding()
            }
            .navigationTitle("Log Session")
            .navigationBarTitleDisplayMode(.large)
            .overlay(
                Group {
                    if showSuccessMessage {
                        VStack {
                            HStack {
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundColor(.green)
                                Text("Entry Saved")
                                    .font(.headline)
                            }
                            .padding()
                            .background(.ultraThinMaterial)
                            .cornerRadius(12)
                            .shadow(radius: 10)
                            .transition(.opacity.combined(with: .scale))
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                    }
                }
            )
            .alert("Edit Session Duration", isPresented: $showingDurationPicker) {
                Button("5 minutes") {
                    manualSessionDuration = 5 * 60
                }
                Button("10 minutes") {
                    manualSessionDuration = 10 * 60
                }
                Button("15 minutes") {
                    manualSessionDuration = 15 * 60
                }
                Button("30 minutes") {
                    manualSessionDuration = 30 * 60
                }
                Button("1 hour") {
                    manualSessionDuration = 60 * 60
                }
                Button("Cancel", role: .cancel) { }
            } message: {
                Text("Select the duration of your sound therapy session")
            }
        }
    }
    
    
    
    
    private var lastSoundSessionSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "waveform")
                    .font(.title2)
                    .foregroundColor(.indigo)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(audioManager.currentSessionDuration > 0 ? "Current Sound Session" : 
                         (audioManager.lastSessionDuration > 0 ? "Last Sound Session" : "Sound Session"))
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    Text(audioManager.currentSessionDuration > 0 ? "Currently playing - live tracking" : 
                         (audioManager.lastSessionDuration > 0 ? "Automatically tracked from your therapy" : "Set duration manually or play therapy sounds"))
                        .font(.caption)
                        .foregroundColor(audioManager.currentSessionDuration > 0 ? .orange : .secondary)
                }
                
                Spacer()
            }
            
            VStack(spacing: 12) {
                if audioManager.currentSessionDuration > 0 || audioManager.lastSessionDuration > 0 {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Duration")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(audioManager.formatDuration(manualSessionDuration ?? (audioManager.currentSessionDuration > 0 ? audioManager.currentSessionDuration : audioManager.lastSessionDuration)))
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.indigo)
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("Frequency")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("\(Int(audioManager.currentFrequency)) Hz")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.indigo)
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("Volume")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("\(Int(audioManager.currentFrequencyVolume * 100))%")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.indigo)
                        }
                    }
                    
                    HStack(spacing: 12) {
                        Button("Edit Duration") {
                            showingDurationPicker = true
                        }
                        .font(.caption)
                        .foregroundColor(.indigo)
                        
                        Button("Clear") {
                            audioManager.clearLastSession()
                            manualSessionDuration = nil
                        }
                        .font(.caption)
                        .foregroundColor(.red)
                        
                        Spacer()
                    }
                } else {
                    // No session data - show manual entry interface
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Duration")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(manualSessionDuration != nil ? audioManager.formatDuration(manualSessionDuration!) : "0 min")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.indigo)
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("Status")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("Not tracked")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                HStack(spacing: 12) {
                    Button("Set Duration") {
                        showingDurationPicker = true
                    }
                    .font(.caption)
                    .foregroundColor(.indigo)
                    
                    if manualSessionDuration != nil {
                        Button("Clear") {
                            manualSessionDuration = nil
                        }
                        .font(.caption)
                        .foregroundColor(.red)
                    }
                    
                    Spacer()
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    
    
    
    private func saveEntry() {
        // Determine which session duration to use
        let trackedDuration = audioManager.currentSessionDuration > 0 ? audioManager.currentSessionDuration : audioManager.lastSessionDuration
        let finalSessionDuration = manualSessionDuration ?? trackedDuration
        
        // Save the session duration to the audio manager for future reference
        if let manualDuration = manualSessionDuration {
            audioManager.lastSessionDuration = manualDuration
        } else if audioManager.currentSessionDuration > 0 {
            // If audio is still playing, save the current session duration
            audioManager.lastSessionDuration = audioManager.currentSessionDuration
        }
        
        // Create diary entry with session data
        viewModel.createEntry(
            loudness: Int16(loudness),
            comfort: Int16(5), // Default comfort value
            stress: Int16(stress),
            notes: nil,
            sessionDuration: finalSessionDuration
        )
        
        HapticFeedback.success.trigger()
        
        withAnimation(.spring()) {
            showSuccessMessage = true
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            withAnimation(.easeOut) {
                showSuccessMessage = false
            }
        }
        
        // Reset form
        loudness = 5
        stress = 3
        manualSessionDuration = nil
        
        // Fetch updated entries
        viewModel.fetchEntries()
    }
    
}



private struct SymptomSlider: View {
    let title: String
    @Binding var value: Int
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text(title)
                    .font(.headline)
                    .fontWeight(.semibold)
                
                Spacer()
                
                Text("\(value)")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(color)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(color.opacity(0.1))
                    )
            }
            
            VStack(spacing: 8) {
                // Custom slider with step marks
                ZStack(alignment: .leading) {
                    // Background track
                    RoundedRectangle(cornerRadius: 3)
                        .fill(Color.gray.opacity(0.2))
                        .frame(height: 6)
                    
                    // Active track
                    RoundedRectangle(cornerRadius: 3)
                        .fill(color)
                        .frame(width: CGFloat(value) / 10 * 280, height: 6)
                    
                    // Slider handle
                    Circle()
                        .fill(color)
                        .frame(width: 20, height: 20)
                        .shadow(color: color.opacity(0.3), radius: 3, x: 0, y: 2)
                        .offset(x: CGFloat(value) / 10 * 260)
                        .gesture(
                            DragGesture()
                                .onChanged { gesture in
                                    let newValue = Int(round(gesture.location.x / 260 * 10))
                                    value = max(0, min(10, newValue))
                                }
                        )
                }
                .frame(width: 280, height: 20)
                
                // Step marks
                HStack {
                    ForEach(0...10, id: \.self) { step in
                        VStack(spacing: 4) {
                            Rectangle()
                                .fill(step == value ? color : Color.gray.opacity(0.4))
                                .frame(width: 2, height: step % 5 == 0 ? 12 : 8)
                            
                            if step % 5 == 0 {
                                Text("\(step)")
                                    .font(.caption2)
                                    .foregroundColor(step == value ? color : .secondary)
                                    .fontWeight(step == value ? .semibold : .regular)
                            }
                        }
                        .onTapGesture {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                value = step
                            }
                        }
                        
                        if step < 10 {
                            Spacer()
                        }
                    }
                }
                .frame(width: 280)
            }
        }
    }
}

extension DateFormatter {
    static let currentDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .full
        formatter.timeStyle = .none
        return formatter
    }()
}

#Preview {
    LogSessionView()
}