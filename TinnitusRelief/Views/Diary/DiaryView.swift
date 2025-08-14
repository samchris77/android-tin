import SwiftUI
import Charts

struct DiaryView: View {
    @StateObject private var viewModel = DiaryViewModel()
    @State private var selectedTab = 0
    @State private var refreshTrigger = 0
    @State private var selectedTime = Date()
    @State private var selectedMonth = Date()
    @State private var weekOffset = 0 // 0 = current week, -1 = previous week, etc.
    @State private var showSaveConfirmation = false
    @State private var lastSavedEntry: (tinnitus: Int, stress: Int, duration: String, entryNumber: Int)?
    
    private let calendar = Calendar.current
    private let weekdaySymbols = ["S", "M", "T", "W", "T", "F", "S"]
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Static navigation row
                HStack(spacing: 0) {
                    Button(action: { navigateTo(0) }) {
                        Text("Add")
                            .font(.system(size: 16, weight: selectedTab == 0 ? .semibold : .medium))
                            .foregroundColor(selectedTab == 0 ? .primary : .secondary)
                            .animation(nil, value: selectedTab)
                    }
                    .frame(maxWidth: .infinity)
                    
                    Button(action: { navigateTo(1) }) {
                        Text("Progress Tracking")
                            .font(.system(size: 16, weight: selectedTab == 1 ? .semibold : .medium))
                            .foregroundColor(selectedTab == 1 ? .primary : .secondary)
                            .animation(nil, value: selectedTab)
                    }
                    .frame(maxWidth: .infinity)
                    
                    Button(action: { navigateTo(2) }) {
                        Text("History")
                            .font(.system(size: 16, weight: selectedTab == 2 ? .semibold : .medium))
                            .foregroundColor(selectedTab == 2 ? .primary : .secondary)
                            .animation(nil, value: selectedTab)
                    }
                    .frame(maxWidth: .infinity)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(Color.gray.opacity(0.05))
                
                // Swipeable TabView
                TabView(selection: $selectedTab) {
                    logView
                        .tag(0)
                    
                    progressTrackingView
                        .tag(1)
                    
                    historyView
                        .tag(2)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .onChange(of: selectedTab) { _, newTab in
                    // Reset Progress page to current month and week when user returns
                    if newTab == 1 {
                        selectedMonth = Date()
                        weekOffset = 0
                    }
                }
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            viewModel.fetchEntries()
        }
    }
    
    private var logView: some View {
        VStack(spacing: 12) {
                // Compact Header
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        DatePicker("", selection: $viewModel.selectedDate, displayedComponents: .date)
                            .datePickerStyle(.compact)
                            .labelsHidden()
                        DatePicker("", selection: $selectedTime, displayedComponents: .hourAndMinute)
                            .datePickerStyle(.compact)
                            .labelsHidden()
                    }
                    
                    Spacer()
                    
                    VStack(alignment: .trailing, spacing: 2) {
                        Text("Entry")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        Text("#\(getEntryNumberForDate(viewModel.selectedDate, trigger: refreshTrigger))")
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color.gray.opacity(0.1))
                )
                
                // Always create new entries to support multiple entries per day
                EntryFormView(
                    entry: nil,
                    onSave: { loudness, comfort, stress, sessionDuration in
                        let combinedDateTime = combineDateAndTime(date: viewModel.selectedDate, time: selectedTime)
                        let entryNumber = getEntryNumberForDate(viewModel.selectedDate, trigger: refreshTrigger)
                        
                        viewModel.createEntry(
                            loudness: loudness, 
                            comfort: comfort, 
                            stress: stress, 
                            notes: nil, 
                            sessionDuration: sessionDuration,
                            createdAt: combinedDateTime
                        )
                        
                        // Format duration for display
                        let minutes = Int(sessionDuration / 60)
                        let durationText = minutes > 0 ? "\(minutes) min" : "0 min"
                        
                        // Store saved entry details and show confirmation
                        lastSavedEntry = (
                            tinnitus: Int(loudness),
                            stress: Int(stress), 
                            duration: durationText,
                            entryNumber: entryNumber
                        )
                        showSaveConfirmation = true
                        
                        // Auto-dismiss popup after 3 seconds
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                            showSaveConfirmation = false
                        }
                        
                        // Reset session timer if audio is still playing
                        let audioManager = UnifiedAudioEngineManager.shared
                        if audioManager.isFrequencyPlaying {
                            audioManager.sessionStartTime = Date()
                            audioManager.currentSessionDuration = 0
                        }
                        
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
        .overlay(
            // Save confirmation popup
            Group {
                if showSaveConfirmation, let entry = lastSavedEntry {
                    SaveConfirmationPopup(
                        entryNumber: entry.entryNumber,
                        tinnitus: entry.tinnitus,
                        stress: entry.stress,
                        duration: entry.duration
                    )
                    .transition(.opacity.combined(with: .scale(scale: 0.9)))
                    .animation(.easeInOut(duration: 0.3), value: showSaveConfirmation)
                }
            }
        )
    }
    
    private var progressTrackingView: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Session Calendar
                sessionCalendarView
                
                weeklySummariesView
                
                comprehensiveStatsView
                
            }
            .padding(12)
        }
    }
    
    private var groupedEntries: [Date: [DiaryEntry]] {
        let calendar = Calendar.current
        return Dictionary(grouping: viewModel.diaryEntries) { entry in
            guard let date = entry.date else { return Date() }
            return calendar.startOfDay(for: date)
        }
    }
    
    private var historyView: some View {
        ScrollView {
            if viewModel.diaryEntries.isEmpty {
                EmptyHistoryView()
                    .padding()
            } else {
                LazyVStack(spacing: 8) {
                    ForEach(groupedEntries.keys.sorted(by: >), id: \.self) { date in
                        DateSeparatorView(date: date)
                        
                        ForEach(groupedEntries[date] ?? [], id: \.id) { entry in
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
                }
                .padding()
            }
        }
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
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Weekly Summaries")
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Spacer()
                
                // Week navigation dots
                HStack(spacing: 4) {
                    ForEach(-3...0, id: \.self) { offset in
                        Circle()
                            .fill(offset == weekOffset ? .orange : Color.secondary.opacity(0.3))
                            .frame(width: 6, height: 6)
                    }
                }
            }
            
            // Two weeks side by side
            HStack(spacing: 12) {
                let summaries = viewModel.getWeeklySummariesForOffset(weekOffset)
                ForEach(Array(summaries.enumerated()), id: \.element.weekOf) { index, summary in
                    WeeklySummaryCard(
                        summary: summary,
                        showChangeIndicators: index == 1 // Only show indicators on the second (right) card
                    )
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
        .gesture(
            DragGesture()
                .onEnded { gesture in
                    let threshold: CGFloat = 50
                    if gesture.translation.width > threshold && weekOffset > -3 {
                        withAnimation(.easeInOut) {
                            weekOffset -= 1 // Swipe right to go back in time
                        }
                    } else if gesture.translation.width < -threshold && weekOffset < 0 {
                        withAnimation(.easeInOut) {
                            weekOffset += 1 // Swipe left to go forward in time
                        }
                    }
                }
        )
    }
    
    private var sessionCalendarView: some View {
        VStack(spacing: 12) {
            // Header with title and month navigation
            HStack {
                Text("Session Calendar")
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Spacer()
                
                HStack(spacing: 16) {
                    Button(action: previousMonth) {
                        Image(systemName: "chevron.left")
                            .font(.title3)
                            .foregroundColor(.orange)
                    }
                    
                    Text(monthYearString)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.primary)
                    
                    Button(action: nextMonth) {
                        Image(systemName: "chevron.right")
                            .font(.title3)
                            .foregroundColor(.orange)
                    }
                }
            }
            .padding(.horizontal, 12)
            
            // Weekday headers
            HStack(spacing: 0) {
                ForEach(weekdaySymbols, id: \.self) { symbol in
                    Text(symbol)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal, 12)
            
            // Calendar grid
            LazyVGrid(columns: Array(repeating: GridItem(.fixed(43), spacing: 4), count: 7), spacing: 4) {
                ForEach(calendarDays, id: \.date) { day in
                    CalendarDayView(
                        day: day,
                        sessionMinutes: sessionMinutesForDate(day.date),
                        isCurrentMonth: day.isCurrentMonth
                    )
                }
            }
            .padding(.horizontal, 12)
            
            // Compact legend and monthly stats
            HStack {
                // Color legend
                HStack(spacing: 8) {
                    Text("Less")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    
                    HStack(spacing: 1) {
                        ForEach(0..<5, id: \.self) { intensity in
                            Rectangle()
                                .fill(colorForIntensity(intensity))
                                .frame(width: 10, height: 10)
                                .cornerRadius(2)
                        }
                    }
                    
                    Text("More")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                // Monthly total
                HStack(spacing: 6) {
                    Text("This Month:")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    
                    Text(formatMonthlyTotal())
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.orange)
                }
            }
            .padding(.horizontal, 12)
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.gray.opacity(0.1))
                .shadow(radius: 2, x: 0, y: 1)
        )
    }
    
    private var comprehensiveStatsView: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Overall Statistics")
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            
            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 8),
                GridItem(.flexible(), spacing: 8)
            ], spacing: 8) {
                EnhancedStatCard(
                    icon: "speaker.wave.2.fill",
                    title: "Avg Tinnitus",
                    value: String(format: "%.1f", viewModel.getOverallAverageLoudness()),
                    unit: "/10",
                    color: .orange
                )
                
                EnhancedStatCard(
                    icon: "exclamationmark.triangle.fill",
                    title: "Avg Stress",
                    value: String(format: "%.1f", viewModel.getOverallAverageStress()),
                    unit: "/10",
                    color: .red
                )
                
                EnhancedStatCard(
                    icon: "clock.fill",
                    title: "Total Time",
                    value: formatTimeInterval(viewModel.getTotalListeningTime()),
                    unit: "",
                    color: .green
                )
                
                EnhancedStatCard(
                    icon: "chart.bar.fill",
                    title: "Avg Session",
                    value: formatTimeInterval(viewModel.getAverageListeningTime()),
                    unit: "",
                    color: .blue
                )
                
                EnhancedStatCard(
                    icon: "list.bullet",
                    title: "Total Entries",
                    value: "\(viewModel.diaryEntries.count)",
                    unit: "",
                    color: .purple
                )
                
                EnhancedStatCard(
                    icon: "waveform",
                    title: "Most Used",
                    value: viewModel.formatFrequency(viewModel.getMostCommonFrequency()),
                    unit: "",
                    color: .indigo
                )
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
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
    
    private func combineDateAndTime(date: Date, time: Date) -> Date {
        let calendar = Calendar.current
        let dateComponents = calendar.dateComponents([.year, .month, .day], from: date)
        let timeComponents = calendar.dateComponents([.hour, .minute], from: time)
        
        var combined = DateComponents()
        combined.year = dateComponents.year
        combined.month = dateComponents.month
        combined.day = dateComponents.day
        combined.hour = timeComponents.hour
        combined.minute = timeComponents.minute
        
        return calendar.date(from: combined) ?? Date()
    }
    
    
    // MARK: - Calendar Methods
    
    private var monthYearString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
        return formatter.string(from: selectedMonth)
    }
    
    private var calendarDays: [CalendarDay] {
        guard let monthInterval = calendar.dateInterval(of: .month, for: selectedMonth) else {
            return []
        }
        
        let monthStart = monthInterval.start
        
        // Find the first day of the calendar grid (may be from previous month)
        let firstWeekday = calendar.component(.weekday, from: monthStart)
        let daysFromPreviousMonth = (firstWeekday - 1) % 7
        let gridStart = calendar.date(byAdding: .day, value: -daysFromPreviousMonth, to: monthStart)!
        
        var days: [CalendarDay] = []
        var currentDate = gridStart
        
        // Generate 42 days (6 weeks) to fill the calendar grid
        for _ in 0..<42 {
            let isCurrentMonth = calendar.isDate(currentDate, equalTo: monthStart, toGranularity: .month)
            days.append(CalendarDay(date: currentDate, isCurrentMonth: isCurrentMonth))
            currentDate = calendar.date(byAdding: .day, value: 1, to: currentDate)!
        }
        
        return days
    }
    
    private func sessionMinutesForDate(_ date: Date) -> Double {
        return viewModel.getSessionMinutesForDate(date)
    }
    
    private func colorForIntensity(_ intensity: Int) -> Color {
        switch intensity {
        case 0: return Color.gray.opacity(0.1)
        case 1: return Color.orange.opacity(0.3)
        case 2: return Color.orange.opacity(0.5)
        case 3: return Color.orange.opacity(0.7)
        case 4: return Color.orange
        default: return Color.orange
        }
    }
    
    private func formatMonthlyTotal() -> String {
        let totalMinutes = viewModel.getMonthlySessionMinutes(for: selectedMonth)
        let hours = Int(totalMinutes) / 60
        let minutes = Int(totalMinutes) % 60
        
        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }
    
    private func previousMonth() {
        withAnimation(.easeInOut(duration: 0.3)) {
            selectedMonth = calendar.date(byAdding: .month, value: -1, to: selectedMonth) ?? selectedMonth
        }
    }
    
    private func nextMonth() {
        withAnimation(.easeInOut(duration: 0.3)) {
            selectedMonth = calendar.date(byAdding: .month, value: 1, to: selectedMonth) ?? selectedMonth
        }
    }
    
    private func isSelected(_ tab: String) -> Bool {
        let tabs = ["Add", "Progress Tracking", "History"]
        return selectedTab == tabs.firstIndex(of: tab)
    }
    
    private func navigateTo(_ tab: Int) {
        withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
            selectedTab = tab
        }
        
        // Delay data refresh to avoid animation conflicts
        if tab == 2 {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                viewModel.fetchEntries()
                refreshTrigger += 1
            }
        }
    }
}

struct EntryFormView: View {
    let entry: DiaryEntry?
    let onSave: (Int16, Int16, Int16, TimeInterval) -> Void
    let currentDate: Date
    let entryNumber: Int
    
    @ObservedObject private var audioManager = UnifiedAudioEngineManager.shared
    @State private var loudnessLevel: Int = 5
    @State private var stressLevel: Int = 5
    @State private var sessionDurationMinutes: Double = 0.0
    @State private var isManuallyEdited: Bool = false
    
    // Computed property to determine which duration to display
    private var effectiveSessionDuration: Double {
        if !isManuallyEdited && audioManager.isFrequencyPlaying && audioManager.currentSessionDuration > 0 {
            return round(audioManager.currentSessionDuration / 60)
        }
        return sessionDurationMinutes
    }
    
    // Check if we're in live tracking mode
    private var isLiveTracking: Bool {
        return !isManuallyEdited && audioManager.isFrequencyPlaying && audioManager.currentSessionDuration > 0
    }
    
    // Toggle between live and manual mode
    private func toggleMode() {
        if isLiveTracking {
            // Switch to manual mode
            isManuallyEdited = true
            sessionDurationMinutes = effectiveSessionDuration
        } else {
            // Switch back to live mode (if audio is playing)
            if audioManager.isFrequencyPlaying && audioManager.currentSessionDuration > 0 {
                isManuallyEdited = false
            }
        }
    }
    
    // Helper function for updating session duration from drag gesture
    private func updateSessionDuration(from dragValue: DragGesture.Value) {
        let sensitivity: Double = 0.02
        let change = Double(dragValue.translation.height) * sensitivity
        let newValue = max(0, min(120, sessionDurationMinutes + change))
        
        if abs(newValue - sessionDurationMinutes) >= 1.0 {
            sessionDurationMinutes = round(newValue)
            isManuallyEdited = true
            
            // Haptic feedback
            let impactFeedback = UIImpactFeedbackGenerator(style: .light)
            impactFeedback.impactOccurred()
        }
    }
    
    // Computed property for the drag gesture
    private var sessionDurationDragGesture: some Gesture {
        DragGesture()
            .onChanged { value in
                updateSessionDuration(from: value)
            }
    }
    
    var body: some View {
        VStack(spacing: 16) {
            SymptomSlider(
                title: "Tinnitus Level",
                value: $loudnessLevel,
                color: .orange
            )
            SymptomSlider(
                title: "Current Stress",
                value: $stressLevel,
                color: .red
            )
            
            // Session Duration Input - Compact
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Session Duration")
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)
                    
                    Spacer()
                    
                    if isLiveTracking {
                        HStack(spacing: 4) {
                            Circle()
                                .fill(Color.green)
                                .frame(width: 6, height: 6)
                            Text("Live")
                                .font(.caption2)
                                .fontWeight(.medium)
                                .foregroundColor(.green)
                        }
                    }
                }
                
                HStack {
                    if isLiveTracking {
                        Text("\(Int(effectiveSessionDuration)) min")
                            .font(.title3)
                            .fontWeight(.semibold)
                            .foregroundColor(.green)
                    } else {
                        VStack(spacing: 2) {
                            Image(systemName: "chevron.up")
                                .font(.caption2)
                                .foregroundColor(.gray.opacity(0.6))
                            
                            Text("\(Int(sessionDurationMinutes)) min")
                                .font(.title3)
                                .fontWeight(.semibold)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(
                                    RoundedRectangle(cornerRadius: 6)
                                        .fill(Color.blue.opacity(0.1))
                                )
                            
                            Image(systemName: "chevron.down")
                                .font(.caption2)
                                .foregroundColor(.gray.opacity(0.6))
                        }
                        .gesture(sessionDurationDragGesture)
                    }
                    
                    Spacer()
                    
                    Button(action: toggleMode) {
                        HStack(spacing: 6) {
                            Image(systemName: isLiveTracking ? "largecircle.fill.circle" : "circle")
                                .font(.body)
                                .foregroundColor(isLiveTracking ? .green : .gray)
                            
                            Text("Live")
                                .font(.body)
                                .foregroundColor(.primary)
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(.ultraThinMaterial)
            )
            
            Button(action: {
                // Convert minutes to seconds for TimeInterval
                let durationInSeconds = effectiveSessionDuration * 60
                onSave(Int16(loudnessLevel), Int16(5), Int16(stressLevel), durationInSeconds) // Default comfort value
                
                // Reset form
                loudnessLevel = 5
                stressLevel = 5
                sessionDurationMinutes = 0.0
                isManuallyEdited = false
            }) {
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
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
        )
        .onAppear {
            // Check if there's a live session first
            if audioManager.isFrequencyPlaying && audioManager.currentSessionDuration > 0 {
                // Live session detected - don't set manual values, let live tracking take over
                isManuallyEdited = false
                sessionDurationMinutes = 0.0 // Will be overridden by effectiveSessionDuration
            } else {
                // No live session - populate with stored values
                if let entry = entry {
                    // Editing existing entry
                    loudnessLevel = Int(entry.loudnessLevel)
                    stressLevel = Int(entry.stressLevel)
                    sessionDurationMinutes = round(entry.sessionDuration / 60)
                    isManuallyEdited = true // Existing entries are always manual
                } else {
                    // New entry - check for previous session duration
                    if audioManager.lastSessionDuration > 0 {
                        sessionDurationMinutes = round(audioManager.lastSessionDuration / 60)
                    } else {
                        sessionDurationMinutes = 0.0
                    }
                    isManuallyEdited = false // Allow live tracking for new entries
                }
            }
        }
        .onChange(of: audioManager.isFrequencyPlaying) { _, isPlaying in
            // Reset manual edit state when audio starts/stops for new entries
            if entry == nil && !isPlaying {
                isManuallyEdited = false
            }
        }
    }
    
}

struct DiaryEntryRowView: View {
    let entry: DiaryEntry
    let onDelete: () -> Void
    
    @State private var showingDeleteAlert = false
    
    var body: some View {
        if isValidEntry {
            VStack(alignment: .leading, spacing: 4) {
            // Header with entry number and created time
            HStack {
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
                
                Text(formatCreationTime(entry.createdAt))
                    .font(.caption2)
                    .foregroundColor(.secondary)
                
                Spacer()
            }
            
            // Combined session info and metrics
            HStack(alignment: .center) {
                // Session info
                VStack(alignment: .leading, spacing: 1) {
                    Text(formatSessionDuration(entry.sessionDuration))
                        .font(.subheadline)
                        .foregroundColor(.green)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 1)
                        .background(
                            RoundedRectangle(cornerRadius: 3)
                                .fill(Color.green.opacity(0.1))
                        )
                }
                
                Spacer()
                
                // Metrics display
                HStack(spacing: 12) {
                    MetricView(
                        title: "Loudness",
                        value: Int(entry.loudnessLevel),
                        icon: "speaker.wave.2.fill",
                        color: .orange.opacity(0.6)
                    )
                    
                    MetricView(
                        title: "Stress",
                        value: Int(entry.stressLevel),
                        icon: "exclamationmark.triangle.fill",
                        color: .red.opacity(0.6)
                    )
                }
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
        .frame(height: 60)
        .padding(.horizontal, 12)
        .padding(.vertical, 3)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
                .shadow(radius: 2, x: 0, y: 1)
        )
        .onTapGesture {
            showingDeleteAlert = true
        }
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
    
    private func formatSessionDuration(_ duration: TimeInterval) -> String {
        let minutes = Int(duration / 60)
        let seconds = Int(duration.truncatingRemainder(dividingBy: 60))
        if minutes > 0 {
            return "\(minutes)m \(seconds)s"
        } else if duration > 0 {
            return "\(seconds)s"
        } else {
            return "0 min"
        }
    }
    
    private func formatCreationTime(_ date: Date?) -> String {
        guard let date = date else { return "Time unknown" }
        let formatter = DateFormatter()
        formatter.dateFormat = "h:mm a"
        return "Created \(formatter.string(from: date))"
    }
}

struct DateSeparatorView: View {
    let date: Date
    
    var body: some View {
        HStack {
            VStack {
                Divider()
            }
            
            Text(formatSeparatorDate(date))
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(.secondary)
                .padding(.horizontal, 12)
                .padding(.vertical, 4)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(.ultraThinMaterial)
                )
            
            VStack {
                Divider()
            }
        }
        .padding(.horizontal)
    }
    
    private func formatSeparatorDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, yyyy"
        return formatter.string(from: date)
    }
}

struct MetricView: View {
    let title: String
    let value: Int
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 2) {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(color)
            
            Text("\(value)/10")
                .font(.system(size: 11, weight: .semibold, design: .monospaced))
                .foregroundColor(.primary)
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
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
                
                Spacer()
                
                Text("\(value)")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(color)
                    .frame(width: 32)
            }
            
            HStack(spacing: 3) {
                ForEach(1...10, id: \.self) { index in
                    Circle()
                        .fill(index <= value ? color : Color.gray.opacity(0.3))
                        .frame(width: 16, height: 16)
                        .onTapGesture {
                            withAnimation(.spring(response: 0.2)) {
                                value = index
                            }
                        }
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(.ultraThinMaterial)
        )
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
    let showChangeIndicators: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Compact header
            VStack(alignment: .leading, spacing: 2) {
                Text("Week of \(summary.weekOf)")
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundColor(.secondary)
                
                Text("\(summary.entryCount) entries")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
            
            // Compact stats or no data message
            if summary.entryCount > 0 {
                VStack(spacing: 6) {
                    CompactStatRow(
                        icon: "speaker.wave.2.fill",
                        title: "Tinnitus",
                        value: String(format: "%.1f", summary.avgLoudness),
                        color: .orange,
                        changeValue: showChangeIndicators ? summary.loudnessChange : nil,
                        isImprovement: showChangeIndicators ? summary.loudnessChange.map { $0 < 0 } : nil // Lower tinnitus is improvement
                    )
                    
                    CompactStatRow(
                        icon: "exclamationmark.triangle.fill",
                        title: "Stress",
                        value: String(format: "%.1f", summary.avgStress),
                        color: .red,
                        changeValue: showChangeIndicators ? summary.stressChange : nil,
                        isImprovement: showChangeIndicators ? summary.stressChange.map { $0 < 0 } : nil // Lower stress is improvement
                    )
                    
                    CompactStatRow(
                        icon: "clock.fill",
                        title: "Listen",
                        value: formatListenTime(summary.totalListeningTime),
                        color: .green,
                        changeValue: showChangeIndicators ? summary.listeningTimeChange : nil,
                        isImprovement: showChangeIndicators ? summary.listeningTimeChange.map { $0 > 0 } : nil // More listening time is improvement
                    )
                }
            } else {
                // No data message
                VStack(spacing: 4) {
                    Image(systemName: "chart.bar.doc.horizontal")
                        .font(.title2)
                        .foregroundColor(.secondary)
                    
                    Text("No data")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .fontWeight(.medium)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
            }
        }
        .padding(8)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(.ultraThinMaterial)
        )
    }
    
    private func formatListenTime(_ timeInterval: TimeInterval) -> String {
        let minutes = Int(timeInterval / 60)
        return "\(minutes)m"
    }
}

struct CompactStatRow: View {
    let icon: String
    let title: String
    let value: String
    let color: Color
    let changeValue: Double?
    let isImprovement: Bool?
    
    init(icon: String, title: String, value: String, color: Color, changeValue: Double? = nil, isImprovement: Bool? = nil) {
        self.icon = icon
        self.title = title
        self.value = value
        self.color = color
        self.changeValue = changeValue
        self.isImprovement = isImprovement
    }
    
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundColor(color)
                .frame(width: 12)
            
            Text(title)
                .font(.caption2)
                .foregroundColor(.secondary)
            
            Spacer()
            
            HStack(spacing: 4) {
                if let change = changeValue, let improvement = isImprovement, abs(change) >= 5 {
                    Text("\(change > 0 ? "↑" : "↓")\(Int(abs(change)))%")
                        .font(.system(size: 10))
                        .foregroundColor(improvement ? Color.green.opacity(0.6) : Color.red.opacity(0.6))
                }
                
                Text(value)
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundColor(color)
            }
        }
    }
}

struct EnhancedStatCard: View {
    let icon: String
    let title: String
    let value: String
    let unit: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Image(systemName: icon)
                    .font(.caption)
                    .foregroundColor(color)
                
                Spacer()
            }
            
            HStack(alignment: .firstTextBaseline, spacing: 2) {
                Text(value)
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(color)
                
                if !unit.isEmpty {
                    Text(unit)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            
            Text(title)
                .font(.caption2)
                .foregroundColor(.secondary)
                .lineLimit(1)
        }
        .padding(8)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(color.opacity(0.05))
        )
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

struct CalendarDay {
    let date: Date
    let isCurrentMonth: Bool
}

struct CalendarDayView: View {
    let day: CalendarDay
    let sessionMinutes: Double
    let isCurrentMonth: Bool
    
    private let calendar = Calendar.current
    
    var body: some View {
        VStack(spacing: 2) {
            Text("\(calendar.component(.day, from: day.date))")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(isCurrentMonth ? .secondary.opacity(0.7) : .secondary.opacity(0.3))
        }
        .frame(width: 43, height: 43)
        .background(
            RoundedRectangle(cornerRadius: 4)
                .fill(backgroundColorForSession)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 4)
                .stroke(isToday ? Color.orange : Color.clear, lineWidth: 2)
        )
        .accessibilityElement()
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(isToday ? .isSelected : [])
    }
    
    private var backgroundColorForSession: Color {
        guard isCurrentMonth else { return Color.clear }
        
        guard sessionMinutes > 0 else { return Color.gray.opacity(0.1) }
        
        // Use 0-120 minute scale for color intensity
        if sessionMinutes <= 30 {
            return Color.orange.opacity(0.3)
        } else if sessionMinutes <= 60 {
            return Color.orange.opacity(0.5)
        } else if sessionMinutes <= 90 {
            return Color.orange.opacity(0.7)
        } else {
            // For 90+ minutes, gradually increase to full orange at 120 minutes
            let extraIntensity = min((sessionMinutes - 90) / 30.0, 1.0)
            return Color.orange.opacity(0.7 + (0.3 * extraIntensity))
        }
    }
    
    private var isToday: Bool {
        calendar.isDateInToday(day.date)
    }
    
    private var accessibilityLabel: String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "EEEE, MMMM d"
        let dateString = dateFormatter.string(from: day.date)
        
        let sessionInfo: String
        if sessionMinutes > 0 {
            let hours = Int(sessionMinutes) / 60
            let minutes = Int(sessionMinutes) % 60
            if hours > 0 {
                sessionInfo = "\(hours) hour\(hours == 1 ? "" : "s") and \(minutes) minute\(minutes == 1 ? "" : "s") of listening sessions"
            } else {
                sessionInfo = "\(minutes) minute\(minutes == 1 ? "" : "s") of listening sessions"
            }
        } else {
            sessionInfo = "No listening sessions"
        }
        
        return "\(dateString), \(sessionInfo)\(isToday ? ", Today" : "")"
    }
}

struct SaveConfirmationPopup: View {
    let entryNumber: Int
    let tinnitus: Int
    let stress: Int
    let duration: String
    
    var body: some View {
        VStack(spacing: 12) {
            // Success icon
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 32))
                .foregroundColor(.green)
            
            // Entry saved text
            Text("Entry #\(entryNumber) Saved")
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            
            // Session details
            VStack(spacing: 4) {
                HStack(spacing: 16) {
                    HStack(spacing: 4) {
                        Image(systemName: "speaker.wave.2.fill")
                            .font(.caption)
                            .foregroundColor(.orange)
                        Text("Tinnitus: \(tinnitus)/10")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    HStack(spacing: 4) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.caption)
                            .foregroundColor(.red)
                        Text("Stress: \(stress)/10")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                
                HStack(spacing: 4) {
                    Image(systemName: "clock.fill")
                        .font(.caption)
                        .foregroundColor(.green)
                    Text("Duration: \(duration)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 8, x: 0, y: 4)
        )
        .frame(maxWidth: 280)
    }
}

#Preview {
    DiaryView()
}
