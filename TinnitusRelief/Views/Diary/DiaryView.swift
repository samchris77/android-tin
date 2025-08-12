import SwiftUI
import Charts

struct DiaryView: View {
    @StateObject private var viewModel = DiaryViewModel()
    @State private var selectedTab = 0
    @State private var refreshTrigger = 0
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                TabView(selection: $selectedTab) {
                    logView
                        .tabItem {
                            Image(systemName: "plus.circle")
                            Text("Add")
                        }
                        .tag(0)
                    
                    progressTrackingView
                        .tabItem {
                            Image(systemName: "chart.line.uptrend.xyaxis")
                            Text("Progress Tracking")
                        }
                        .tag(1)
                    
                    historyView
                        .tabItem {
                            Image(systemName: "clock")
                            Text("History")
                        }
                        .tag(2)
                }
                .accentColor(.orange)
                .onChange(of: selectedTab) { newTab in
                    // Refresh data when switching to history tab
                    if newTab == 2 {
                        viewModel.fetchEntries()
                        refreshTrigger += 1
                    }
                }
            }
            .navigationTitle("Log")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Menu {
                        Button("Export Data") {
                            exportData()
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                }
            }
        }
        .onAppear {
            viewModel.fetchEntries()
        }
    }
    
    private var logView: some View {
        ScrollView {
            VStack(spacing: 24) {
                VStack(spacing: 8) {
                    DatePicker("Date", selection: $viewModel.selectedDate, displayedComponents: .date)
                        .datePickerStyle(.compact)
                    
                    HStack {
                        Text("Entry")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Text("#\(getEntryNumberForDate(viewModel.selectedDate, trigger: refreshTrigger))")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundColor(.orange)
                        
                        Spacer()
                    }
                }
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(.ultraThinMaterial)
                )
                
                // Always create new entries to support multiple entries per day
                EntryFormView(
                    entry: nil,
                    onSave: { loudness, comfort, stress in
                        viewModel.createEntry(loudness: loudness, comfort: comfort, stress: stress, notes: nil)
                        
                        // Wait for database operations to complete, then refresh UI
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                            viewModel.fetchEntries()
                            refreshTrigger += 1 // Force UI update after data is ready
                            print("UI refresh triggered for new entry - refreshTrigger: \(refreshTrigger)")
                        }
                    },
                    currentDate: viewModel.selectedDate,
                    entryNumber: getEntryNumberForDate(viewModel.selectedDate, trigger: refreshTrigger)
                )
            }
            .padding()
        }
    }
    
    private var progressTrackingView: some View {
        ScrollView {
            VStack(spacing: 24) {
                weeklySummariesView
                
                comprehensiveStatsView
                
                if #available(iOS 16.0, *) {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Loudness Trend (Last 30 Days)")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        Chart(recentEntries) { entry in
                            LineMark(
                                x: .value("Date", entry.date ?? Date()),
                                y: .value("Loudness", entry.loudnessLevel)
                            )
                            .foregroundStyle(Color.orange)
                            .lineStyle(StrokeStyle(lineWidth: 3))
                        }
                        .frame(height: 200)
                        .chartYAxis {
                            AxisMarks(values: Array(1...10)) { value in
                                AxisValueLabel {
                                    if let intValue = value.as(Int.self) {
                                        Text("\(intValue)")
                                    }
                                }
                                AxisGridLine()
                            }
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(.ultraThinMaterial)
                                .padding(-12)
                        )
                    }
                    
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Stress Level Trend (Last 30 Days)")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        Chart(recentEntries) { entry in
                            LineMark(
                                x: .value("Date", entry.date ?? Date()),
                                y: .value("Stress", entry.stressLevel)
                            )
                            .foregroundStyle(Color.red)
                            .lineStyle(StrokeStyle(lineWidth: 3))
                        }
                        .frame(height: 200)
                        .chartYAxis {
                            AxisMarks(values: Array(1...10)) { value in
                                AxisValueLabel {
                                    if let intValue = value.as(Int.self) {
                                        Text("\(intValue)")
                                    }
                                }
                                AxisGridLine()
                            }
                        }
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(.ultraThinMaterial)
                                .padding(-12)
                        )
                    }
                } else {
                    Text("Charts require iOS 16.0 or later")
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, maxHeight: 200)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(.ultraThinMaterial)
                        )
                }
            }
            .padding()
        }
    }
    
    private var historyView: some View {
        ScrollView {
            if viewModel.diaryEntries.isEmpty {
                EmptyHistoryView()
                    .padding()
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(viewModel.diaryEntries, id: \.id) { entry in
                        DiaryEntryRowView(entry: entry) {
                            viewModel.deleteEntry(entry)
                            
                            // Wait for database operations to complete, then refresh UI
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                                viewModel.fetchEntries()
                                refreshTrigger += 1 // Force UI update after data is ready
                                print("UI refresh triggered after delete - refreshTrigger: \(refreshTrigger)")
                            }
                        }
                    }
                }
                .padding()
            }
        }
        .id(refreshTrigger) // Make view reactive to refresh trigger
    }
    
    private var weeklyStatsView: some View {
        VStack(spacing: 16) {
            Text("This Week's Average")
                .font(.headline)
                .foregroundColor(.primary)
            
            HStack(spacing: 20) {
                StatCardView(
                    title: "Loudness",
                    value: viewModel.averageLoudnessForWeek(),
                    color: .orange,
                    maxValue: 10
                )
                
                StatCardView(
                    title: "Stress",
                    value: viewModel.averageStressForWeek(),
                    color: .red,
                    maxValue: 10
                )
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 4, x: 0, y: 2)
        )
    }
    
    private var recentEntries: [DiaryEntry] {
        let thirtyDaysAgo = Calendar.current.date(byAdding: .day, value: -30, to: Date()) ?? Date()
        return viewModel.diaryEntries.filter { entry in
            guard let date = entry.date else { return false }
            return date >= thirtyDaysAgo
        }.sorted { ($0.date ?? Date()) < ($1.date ?? Date()) }
    }
    
    private func deleteEntries(offsets: IndexSet) {
        for index in offsets {
            viewModel.deleteEntry(viewModel.diaryEntries[index])
        }
    }
    
    private var weeklySummariesView: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Weekly Summaries")
                .font(.headline)
                .foregroundColor(.primary)
            
            ForEach(viewModel.getWeeklySummaries(), id: \.weekOf) { summary in
                WeeklySummaryCard(summary: summary)
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 4, x: 0, y: 2)
        )
    }
    
    private var comprehensiveStatsView: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Overall Statistics")
                .font(.headline)
                .foregroundColor(.primary)
            
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 2), spacing: 16) {
                ComprehensiveStatCard(
                    title: "Total Listening Time",
                    value: formatTimeInterval(viewModel.getTotalListeningTime()),
                    subtitle: "All sessions combined",
                    color: .green
                )
                
                ComprehensiveStatCard(
                    title: "Average Session",
                    value: formatTimeInterval(viewModel.getAverageListeningTime()),
                    subtitle: "Per listening session",
                    color: .blue
                )
                
                ComprehensiveStatCard(
                    title: "Overall Avg Stress",
                    value: String(format: "%.1f/10", viewModel.getOverallAverageStress()),
                    subtitle: "Across all entries",
                    color: .red
                )
                
                ComprehensiveStatCard(
                    title: "Overall Avg Loudness",
                    value: String(format: "%.1f/10", viewModel.getOverallAverageLoudness()),
                    subtitle: "Across all entries",
                    color: .orange
                )
                
                ComprehensiveStatCard(
                    title: "Overall Avg Comfort",
                    value: String(format: "%.1f/10", viewModel.getOverallAverageComfort()),
                    subtitle: "Across all entries",
                    color: .blue
                )
                
                ComprehensiveStatCard(
                    title: "Total Entries",
                    value: "\(viewModel.diaryEntries.count)",
                    subtitle: "Diary entries logged",
                    color: .purple
                )
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 4, x: 0, y: 2)
        )
    }
    
    private func formatTimeInterval(_ timeInterval: TimeInterval) -> String {
        let hours = Int(timeInterval) / 3600
        let minutes = Int(timeInterval) % 3600 / 60
        
        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }
    
    private func getEntryNumberForDate(_ date: Date, trigger: Int) -> Int {
        let startOfDay = Calendar.current.startOfDay(for: date)
        let entriesForDate = viewModel.diaryEntries.filter { entry in
            guard let entryDate = entry.date else { return false }
            return Calendar.current.isDate(entryDate, inSameDayAs: startOfDay)
        }
        return entriesForDate.count + 1
    }
    
    private func exportData() {
        let csvContent = viewModel.exportToCSV()
        let activityVC = UIActivityViewController(activityItems: [csvContent], applicationActivities: nil)
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}

struct EntryFormView: View {
    let entry: DiaryEntry?
    let onSave: (Int16, Int16, Int16) -> Void
    let currentDate: Date
    let entryNumber: Int
    
    @State private var loudnessLevel: Int = 5
    @State private var comfortLevel: Int = 5
    @State private var stressLevel: Int = 5
    @State private var showSuccess: Bool = false
    @State private var submittedEntryNumber: Int = 0
    
    var body: some View {
        VStack(spacing: 20) {
            ScaleInputView(
                title: "Tinnitus Loudness",
                value: $loudnessLevel,
                minLabel: "Silent",
                maxLabel: "Very Loud",
                color: .orange
            )
            
            ScaleInputView(
                title: "Comfort Level",
                value: $comfortLevel,
                minLabel: "Very Uncomfortable",
                maxLabel: "Very Comfortable",
                color: .blue
            )
            
            ScaleInputView(
                title: "Stress Level",
                value: $stressLevel,
                minLabel: "No Stress",
                maxLabel: "Very Stressed",
                color: .red
            )
            
            
            Button(action: {
                // Capture the entry number that will be created (current entryNumber)
                submittedEntryNumber = entryNumber
                
                onSave(Int16(loudnessLevel), Int16(comfortLevel), Int16(stressLevel))
                
                // Show success message
                showSuccess = true
                
                // Hide success message after 3 seconds
                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                    showSuccess = false
                }
            }) {
                Text("Submit Entry")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(
                        LinearGradient(
                            colors: [Color.orange, Color.red],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .cornerRadius(12)
                    .shadow(radius: 4, x: 0, y: 2)
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 4, x: 0, y: 2)
        )
        .overlay(
            // Success message
            VStack {
                if showSuccess {
                    VStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.green)
                        
                        Text("Entry #\(submittedEntryNumber) for \(formatDate(currentDate)) submitted")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.primary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.ultraThinMaterial)
                            .shadow(radius: 8)
                    )
                    .transition(.opacity.combined(with: .scale))
                }
            }
            .animation(.easeInOut(duration: 0.3), value: showSuccess)
        )
        .onAppear {
            if let entry = entry {
                loudnessLevel = Int(entry.loudnessLevel)
                comfortLevel = Int(entry.comfortLevel)
                stressLevel = Int(entry.stressLevel)
            }
        }
    }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, yyyy"
        return formatter.string(from: date)
    }
}

struct DiaryEntryRowView: View {
    let entry: DiaryEntry
    let onDelete: () -> Void
    
    @State private var showingDeleteAlert = false
    
    var body: some View {
        if isValidEntry {
            VStack(alignment: .leading, spacing: 12) {
            // Header with date and entry number
            HStack {
                Text(formatEntryDate(entry.date))
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Text("#\(entry.entryNumber)")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundColor(.orange)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(
                        RoundedRectangle(cornerRadius: 4)
                            .fill(Color.orange.opacity(0.1))
                    )
                
                Spacer()
                
                // Delete Button
                Button(action: {
                    showingDeleteAlert = true
                }) {
                    Image(systemName: "trash")
                        .font(.system(size: 14))
                        .foregroundColor(.red)
                        .padding(8)
                        .background(
                            Circle()
                                .fill(Color.red.opacity(0.1))
                        )
                }
            }
            
            // Metrics display
            HStack(spacing: 20) {
                MetricView(
                    title: "Loudness",
                    value: Int(entry.loudnessLevel),
                    icon: "speaker.wave.2.fill",
                    color: .orange
                )
                
                MetricView(
                    title: "Comfort", 
                    value: Int(entry.comfortLevel),
                    icon: "heart.fill",
                    color: .blue
                )
                
                MetricView(
                    title: "Stress",
                    value: Int(entry.stressLevel),
                    icon: "exclamationmark.triangle.fill",
                    color: .red
                )
            }
            
            // Optional notes display
            if let notes = entry.notes, !notes.isEmpty {
                Text(notes)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                    .padding(.top, 4)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
                .shadow(radius: 2, x: 0, y: 1)
        )
        .alert("Delete Entry", isPresented: $showingDeleteAlert) {
            Button("Cancel", role: .cancel) { }
            Button("Delete", role: .destructive) {
                onDelete()
            }
        } message: {
            Text("Are you sure you want to delete this diary entry? This action cannot be undone.")
        }
        }
    }
    
    private func formatEntryDate(_ date: Date?) -> String {
        guard let date = date else { return "Unknown Date" }
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, yyyy"
        return formatter.string(from: date)
    }
    
    private var isValidEntry: Bool {
        // Check if this is a valid entry (not corrupted data)
        return entry.date != nil && entry.entryNumber > 0
    }
}

struct MetricView: View {
    let title: String
    let value: Int
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(color)
            
            Text("\(value)/10")
                .font(.system(size: 12, weight: .semibold, design: .monospaced))
                .foregroundColor(.primary)
            
            Text(title)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }
}

struct ScaleInputView: View {
    let title: String
    @Binding var value: Int
    let minLabel: String
    let maxLabel: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(title)
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Spacer()
                
                Text("\(value)/10")
                    .font(.system(size: 18, weight: .semibold, design: .monospaced))
                    .foregroundColor(color)
            }
            
            VStack(spacing: 8) {
                HStack {
                    ForEach(1...10, id: \.self) { index in
                        Circle()
                            .fill(index <= value ? color : Color.gray.opacity(0.3))
                            .frame(width: 24, height: 24)
                            .scaleEffect(index == value ? 1.2 : 1.0)
                            .onTapGesture {
                                withAnimation(.spring(response: 0.3)) {
                                    value = index
                                }
                            }
                        
                        if index < 10 {
                            Spacer(minLength: 4)
                        }
                    }
                }
                
                HStack {
                    Text(minLabel)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    Text(maxLabel)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
}

struct StatCardView: View {
    let title: String
    let value: Double
    let color: Color
    let maxValue: Double
    
    var body: some View {
        VStack(spacing: 8) {
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
            
            Text(String(format: "%.1f", value))
                .font(.system(size: 24, weight: .bold, design: .monospaced))
                .foregroundColor(color)
            
            ProgressView(value: value / maxValue)
                .progressViewStyle(LinearProgressViewStyle(tint: color))
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
}

struct EmptyHistoryView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "book")
                .font(.system(size: 48))
                .foregroundColor(.orange.opacity(0.6))
            
            Text("No Diary Entries Yet")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            
            Text("Enter a new log")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            
            VStack(spacing: 8) {
                Text("Track your tinnitus symptoms by")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Text("switching to the \"Add\" tab")
                    .font(.caption)
                    .foregroundColor(.orange)
                    .fontWeight(.medium)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
        .padding(.horizontal, 20)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 2, x: 0, y: 1)
        )
    }
}

struct WeeklySummaryCard: View {
    let summary: WeeklySummary
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Week of \(summary.weekOf)")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Spacer()
                
                Text("\(summary.entryCount) entries")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(
                        RoundedRectangle(cornerRadius: 4)
                            .fill(Color.gray.opacity(0.1))
                    )
            }
            
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 2), spacing: 12) {
                SummaryStatView(
                    title: "Avg Loudness",
                    value: String(format: "%.1f", summary.avgLoudness),
                    color: .orange
                )
                
                SummaryStatView(
                    title: "Avg Stress",
                    value: String(format: "%.1f", summary.avgStress),
                    color: .red
                )
                
                SummaryStatView(
                    title: "Avg Comfort",
                    value: String(format: "%.1f", summary.avgComfort),
                    color: .blue
                )
                
                SummaryStatView(
                    title: "Listen Time",
                    value: formatListenTime(summary.totalListeningTime),
                    color: .green
                )
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    private func formatListenTime(_ timeInterval: TimeInterval) -> String {
        let minutes = Int(timeInterval / 60)
        return "\(minutes)m"
    }
}

struct SummaryStatView: View {
    let title: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title3)
                .fontWeight(.semibold)
                .foregroundColor(color)
            
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(color.opacity(0.1))
        )
    }
}

struct ComprehensiveStatCard: View {
    let title: String
    let value: String
    let subtitle: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 8) {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(color)
                .multilineTextAlignment(.center)
            
            Text(title)
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
                .multilineTextAlignment(.center)
            
            Text(subtitle)
                .font(.caption2)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(color.opacity(0.1))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(color.opacity(0.3), lineWidth: 1)
                )
        )
    }
}

#Preview {
    DiaryView()
}