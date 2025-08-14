import SwiftUI

struct DiaryEntryView: View {
    @StateObject private var viewModel = DiaryViewModel()
    
    // Entry data
    @State private var loudness: Int = 5
    @State private var stress: Int = 3
    
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
        }
    }
    
    private func saveEntry() {
        // Create diary entry with session data
        viewModel.createEntry(
            loudness: Int16(loudness),
            comfort: Int16(5), // Default comfort value
            stress: Int16(stress),
            notes: nil
        )
        
        HapticFeedback.success.trigger()
        
        // Reset form
        loudness = 5
        stress = 3
        
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

#Preview {
    DiaryEntryView()
}