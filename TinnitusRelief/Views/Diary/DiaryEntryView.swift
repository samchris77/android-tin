import SwiftUI

struct DiaryEntryView: View {
    @StateObject private var viewModel = DiaryViewModel()
    
    // Entry data
    @State private var severity: Double = 5.0
    @State private var hasUnsavedChanges = false
    
    private let currentDate = Date()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 32) {
                    // Header with current date
                    VStack(spacing: 8) {
                        Text("New Log Entry")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                        
                        Text(DateFormatter.entryDateFormatter.string(from: currentDate))
                            .font(.title3)
                            .foregroundColor(.orange)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 8)
                            .background(
                                Capsule()
                                    .fill(.orange.opacity(0.1))
                            )
                    }
                    .padding(.top, 20)
                    
                    // Severity slider
                    VStack(alignment: .leading, spacing: 20) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Tinnitus Severity")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                            
                            Text("How loud is your tinnitus right now?")
                                .font(.body)
                                .foregroundColor(.secondary)
                        }
                        
                        VStack(spacing: 16) {
                            // Severity value display
                            Text("\(Int(severity))/10")
                                .font(.system(size: 48, weight: .light, design: .monospaced))
                                .foregroundColor(severityColor(for: severity))
                                .animation(.easeInOut(duration: 0.2), value: severity)
                            
                            // Slider
                            VStack(spacing: 12) {
                                Slider(value: $severity, in: 0...10, step: 1)
                                    .accentColor(severityColor(for: severity))
                                    .onChange(of: severity) { _ in
                                        HapticFeedback.light.trigger()
                                        hasUnsavedChanges = true
                                    }
                                
                                HStack {
                                    Text("Mild")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                    
                                    Spacer()
                                    
                                    Text("Severe")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                            .padding(.horizontal, 8)
                        }
                        .padding(24)
                        .background(
                            RoundedRectangle(cornerRadius: 16)
                                .fill(.ultraThinMaterial)
                                .shadow(radius: 2, x: 0, y: 1)
                        )
                    }
                    
                    
                    Spacer(minLength: 100) // Space for auto-save indicator
                }
                .padding(.horizontal, 20)
            }
            .navigationBarHidden(true)
        }
        .onAppear {
            // Reset form for new entry
            severity = 5.0
            hasUnsavedChanges = false
        }
        .onDisappear {
            // Auto-save when navigating away
            if hasUnsavedChanges {
                saveEntry()
            }
        }
        .overlay(
            // Auto-save indicator
            VStack {
                Spacer()
                
                if hasUnsavedChanges {
                    HStack {
                        Image(systemName: "square.and.arrow.down")
                            .foregroundColor(.orange)
                        
                        Text("Changes will be saved automatically")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(
                        Capsule()
                            .fill(.ultraThinMaterial)
                            .shadow(radius: 4, x: 0, y: 2)
                    )
                    .padding(.bottom, 100) // Above tab bar
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
                }
            }
            .animation(.easeInOut(duration: 0.3), value: hasUnsavedChanges)
        )
    }
    
    private func saveEntry() {
        guard hasUnsavedChanges else { return }
        
        // Save via view model with Core Data
        viewModel.addEntry(
            date: currentDate,
            severity: Int(severity),
            notes: nil
        )
        
        // Provide haptic feedback
        HapticFeedback.success.trigger()
        
        // Reset state
        hasUnsavedChanges = false
    }
    
    private func severityColor(for value: Double) -> Color {
        switch value {
        case 0...2:
            return .green
        case 3...4:
            return .blue
        case 5...6:
            return .orange
        case 7...8:
            return .red
        default:
            return .purple
        }
    }
}

// DiaryEntry is now managed by Core Data model

extension DateFormatter {
    static let entryDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .full
        formatter.timeStyle = .none
        return formatter
    }()
}

#Preview {
    DiaryEntryView()
}