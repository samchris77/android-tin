import SwiftUI

struct LogSessionView: View {
    @StateObject private var viewModel = DiaryViewModel()
    @State private var selectedSeverity: Int = 5
    @State private var selectedStress: Int = 3
    @State private var notes: String = ""
    @State private var showSuccessMessage = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Header Section
                    headerSection
                    
                    // Severity Rating
                    severitySection
                    
                    // Stress Level
                    stressSection
                    
                    // Notes Section
                    notesSection
                    
                    // Quick Actions
                    quickActionsSection
                    
                    // Save Button
                    saveButton
                    
                    Spacer(minLength: 20)
                }
                .padding()
            }
            .navigationTitle("Log Session")
            .navigationBarTitleDisplayMode(.large)
            .overlay(
                Group {
                    if showSuccessMessage {
                        SuccessMessageView()
                            .transition(.scale.combined(with: .opacity))
                    }
                }
            )
        }
    }
    
    private var headerSection: some View {
        VStack(spacing: 12) {
            Image(systemName: "plus.circle.fill")
                .font(.system(size: 48))
                .foregroundColor(.green)
            
            Text("Log Your Progress")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            Text("Track your tinnitus symptoms and mood to identify patterns over time")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Text(DateFormatter.currentDateFormatter.string(from: Date()))
                .font(.subheadline)
                .foregroundColor(.orange)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(
                    Capsule()
                        .fill(Color.orange.opacity(0.1))
                )
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 4, x: 0, y: 2)
        )
    }
    
    private var severitySection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "speaker.wave.3.fill")
                    .font(.title2)
                    .foregroundColor(.orange)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("Tinnitus Severity")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    Text("How loud is your tinnitus right now?")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
            }
            
            // Severity Scale
            VStack(spacing: 12) {
                HStack {
                    Text("Mild")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    Text("Severe")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                HStack(spacing: 8) {
                    ForEach(1...10, id: \.self) { level in
                        Button(action: {
                            selectedSeverity = level
                            HapticFeedback.light.trigger()
                        }) {
                            Circle()
                                .fill(selectedSeverity >= level ? severityColor(for: level) : Color.gray.opacity(0.2))
                                .frame(width: 28, height: 28)
                                .overlay(
                                    Text("\(level)")
                                        .font(.caption2)
                                        .fontWeight(.semibold)
                                        .foregroundColor(selectedSeverity >= level ? .white : .secondary)
                                )
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                
                Text("Current: \(selectedSeverity)/10")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(severityColor(for: selectedSeverity))
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    private var stressSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "brain.head.profile")
                    .font(.title2)
                    .foregroundColor(.blue)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("Stress Level")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    Text("How stressed or anxious do you feel?")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
            }
            
            // Stress Scale
            VStack(spacing: 12) {
                HStack {
                    Text("Relaxed")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    Text("Very Stressed")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                HStack(spacing: 8) {
                    ForEach(1...5, id: \.self) { level in
                        Button(action: {
                            selectedStress = level
                            HapticFeedback.light.trigger()
                        }) {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(selectedStress >= level ? stressColor(for: level) : Color.gray.opacity(0.2))
                                .frame(width: 50, height: 36)
                                .overlay(
                                    Text("\(level)")
                                        .font(.subheadline)
                                        .fontWeight(.semibold)
                                        .foregroundColor(selectedStress >= level ? .white : .secondary)
                                )
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                
                Text("Current: \(selectedStress)/5")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(stressColor(for: selectedStress))
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    private var notesSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "note.text")
                    .font(.title2)
                    .foregroundColor(.purple)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("Additional Notes")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    Text("Any triggers, treatments, or observations?")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
            }
            
            TextEditor(text: $notes)
                .frame(minHeight: 80)
                .padding(8)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        .background(Color(UIColor.systemBackground))
                )
                .overlay(
                    Group {
                        if notes.isEmpty {
                            VStack {
                                HStack {
                                    Text("Optional notes about your symptoms, activities, or treatments...")
                                        .foregroundColor(.secondary)
                                        .padding(.leading, 4)
                                        .padding(.top, 8)
                                    Spacer()
                                }
                                Spacer()
                            }
                        }
                    }
                )
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Quick Tags")
                .font(.headline)
                .foregroundColor(.primary)
            
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: 8) {
                QuickTagButton(title: "Sleep Issues", icon: "moon.fill", isSelected: false)
                QuickTagButton(title: "Loud Environment", icon: "speaker.3.fill", isSelected: false)
                QuickTagButton(title: "Stress", icon: "exclamationmark.triangle.fill", isSelected: false)
                QuickTagButton(title: "Caffeine", icon: "cup.and.saucer.fill", isSelected: false)
                QuickTagButton(title: "Exercise", icon: "figure.walk", isSelected: false)
                QuickTagButton(title: "Medication", icon: "pill.fill", isSelected: false)
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    private var saveButton: some View {
        Button(action: saveEntry) {
            HStack {
                Image(systemName: "checkmark.circle.fill")
                Text("Save Entry")
            }
            .font(.system(size: 18, weight: .semibold))
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .background(
                LinearGradient(
                    colors: [Color.green, Color.blue],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .cornerRadius(12)
            .shadow(radius: 4, x: 0, y: 2)
        }
    }
    
    private func saveEntry() {
        // Save logic here
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
        selectedSeverity = 5
        selectedStress = 3
        notes = ""
    }
    
    private func severityColor(for level: Int) -> Color {
        switch level {
        case 1...3:
            return .green
        case 4...6:
            return .orange
        case 7...10:
            return .red
        default:
            return .gray
        }
    }
    
    private func stressColor(for level: Int) -> Color {
        switch level {
        case 1:
            return .green
        case 2:
            return .blue
        case 3:
            return .orange
        case 4:
            return .red
        case 5:
            return .purple
        default:
            return .gray
        }
    }
}

struct QuickTagButton: View {
    let title: String
    let icon: String
    @State var isSelected: Bool
    
    var body: some View {
        Button(action: {
            isSelected.toggle()
            HapticFeedback.light.trigger()
        }) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.caption)
                Text(title)
                    .font(.caption2)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(isSelected ? Color.blue.opacity(0.2) : Color.gray.opacity(0.1))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(isSelected ? Color.blue : Color.clear, lineWidth: 1)
                    )
            )
            .foregroundColor(isSelected ? .blue : .secondary)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct SuccessMessageView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 48))
                .foregroundColor(.green)
            
            Text("Entry Saved!")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            
            Text("Your progress has been recorded")
                .font(.body)
                .foregroundColor(.secondary)
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 8, x: 0, y: 4)
        )
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