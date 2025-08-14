import SwiftUI

struct DiaryEntryView: View {
    @StateObject private var viewModel = DiaryViewModel()
    
    // Entry data
    @State private var loudness: Int = 5
    @State private var stress: Int = 3
    @State private var comfort: Int = 5
    
    private let currentDate = Date()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 32) {
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
                            title: "Loudness",
                            value: $loudness,
                            color: .orange
                        )
                        SymptomSlider(
                            title: "Stress",
                            value: $stress,
                            color: .red
                        )
                        SymptomSlider(
                            title: "Comfort",
                            value: $comfort,
                            color: .blue
                        )
                    }
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(.ultraThinMaterial)
                    )
                    
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
                .padding(.horizontal, 20)
            }
            .navigationBarHidden(true)
        }
        .onAppear {
            // Reset form for new entry
            loudness = 5
            stress = 3
            comfort = 5
        }
    }
    
    private func saveEntry() {
        // Create diary entry with session data
        viewModel.createEntry(
            loudness: Int16(loudness),
            comfort: Int16(comfort),
            stress: Int16(stress),
            notes: nil
        )
        
        HapticFeedback.success.trigger()
        
        // Reset form
        loudness = 5
        stress = 3
        comfort = 5
        
        // Fetch updated entries
        viewModel.fetchEntries()
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

private struct SymptomSlider: View {
    let title: String
    @Binding var value: Int
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
            
            HStack {
                Slider(
                    value: .init(
                        get: { Double(value) },
                        set: { value = Int($0) }
                    ),
                    in: 0...10,
                    step: 1
                )
                .accentColor(color)
                
                Text("\(value)")
                    .font(.system(size: 20, weight: .semibold, design: .monospaced))
                    .frame(width: 40, alignment: .trailing)
            }
        }
    }
}

#Preview {
    DiaryEntryView()
}