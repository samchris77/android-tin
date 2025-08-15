import SwiftUI
import CoreData
import Combine

struct WeeklySummary {
    let weekOf: String
    let entryCount: Int
    let avgLoudness: Double
    let avgStress: Double
    let totalListeningTime: TimeInterval
    
    // Previous week comparison data
    let previousWeekLoudness: Double?
    let previousWeekStress: Double?
    let previousWeekListeningTime: TimeInterval?
    
    // Computed properties for change calculations
    var loudnessChange: Double? {
        guard let previous = previousWeekLoudness, previous > 0 else { return nil }
        return ((avgLoudness - previous) / previous) * 100
    }
    
    var stressChange: Double? {
        guard let previous = previousWeekStress, previous > 0 else { return nil }
        return ((avgStress - previous) / previous) * 100
    }
    
    var listeningTimeChange: Double? {
        guard let previous = previousWeekListeningTime, previous > 0 else { return nil }
        return ((totalListeningTime - previous) / previous) * 100
    }
}

class DiaryViewModel: ObservableObject {
    @Published var diaryEntries: [DiaryEntry] = []
    @Published var selectedDate = Date()
    @Published var isShowingEntryForm = false
    
    private let persistenceController = PersistenceController.shared
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        fetchEntries()
        setupDateObserver()
    }
    
    private func setupDateObserver() {
        $selectedDate
            .debounce(for: .milliseconds(300), scheduler: DispatchQueue.main)
            .sink { [weak self] _ in
                self?.fetchEntries()
            }
            .store(in: &cancellables)
    }
    
    func fetchEntries() {
        let request: NSFetchRequest<DiaryEntry> = DiaryEntry.fetchRequest()
        request.sortDescriptors = [NSSortDescriptor(keyPath: \DiaryEntry.date, ascending: false)]
        
        do {
            let allEntries = try persistenceController.container.viewContext.fetch(request)
            
            // Filter out invalid entries and clean up database
            let validEntries = allEntries.filter { entry in
                return entry.date != nil && entry.entryNumber > 0
            }
            
            // Remove invalid entries from database
            let invalidEntries = allEntries.filter { entry in
                return entry.date == nil || entry.entryNumber <= 0
            }
            
            if !invalidEntries.isEmpty {
                print("Cleaning up \(invalidEntries.count) invalid entries")
                for invalidEntry in invalidEntries {
                    persistenceController.container.viewContext.delete(invalidEntry)
                }
                
                do {
                    try persistenceController.container.viewContext.save()
                    print("Invalid entries cleaned up successfully")
                } catch {
                    print("Error cleaning up invalid entries: \(error)")
                }
            }
            
            diaryEntries = validEntries
        } catch {
            print("Error fetching diary entries: \(error)")
        }
    }
    
    func createEntry(loudness: Int16, comfort: Int16, stress: Int16, notes: String?, sessionDuration: TimeInterval? = nil, createdAt: Date? = nil) {
        let context = persistenceController.container.viewContext
        let audioManager = UnifiedAudioEngineManager.shared
        
        let entry = DiaryEntry(context: context)
        entry.id = UUID()
        entry.date = Calendar.current.startOfDay(for: selectedDate)
        entry.createdAt = createdAt ?? Date() // Use provided time or current time
        entry.loudnessLevel = loudness
        entry.comfortLevel = comfort
        entry.stressLevel = stress
        entry.notes = notes
        
        // Auto-populate frequency and volume data
        entry.currentFrequency = audioManager.currentFrequency
        entry.currentVolume = audioManager.currentFrequencyVolume
        
        // Use provided session duration, or fall back to tracked duration from audio manager
        entry.sessionDuration = sessionDuration ?? audioManager.lastSessionDuration
        
        // Calculate entry number for this date
        entry.entryNumber = Int16(getNextEntryNumberForDate(selectedDate))
        
        do {
            try context.save()
            print("DiaryEntry #\(entry.entryNumber) created successfully for \(selectedDate)")
        } catch {
            print("Error saving diary entry: \(error)")
        }
    }
    
    func updateEntry(_ entry: DiaryEntry, loudness: Int16, comfort: Int16, stress: Int16, notes: String?) {
        let audioManager = UnifiedAudioEngineManager.shared
        
        entry.loudnessLevel = loudness
        entry.comfortLevel = comfort
        entry.stressLevel = stress
        entry.notes = notes
        
        // Update frequency and volume data if audio is currently playing
        if audioManager.isFrequencyPlaying {
            entry.currentFrequency = audioManager.currentFrequency
            entry.currentVolume = audioManager.currentFrequencyVolume
        }
        
        do {
            try persistenceController.container.viewContext.save()
            print("DiaryEntry updated successfully")
        } catch {
            print("Error updating diary entry: \(error)")
        }
    }
    
    func deleteEntry(_ entry: DiaryEntry) {
        let context = persistenceController.container.viewContext
        let entryDate = entry.date
        
        context.delete(entry)
        
        do {
            try context.save()
            
            // Renumber entries for this date after deletion
            if let date = entryDate {
                renumberEntriesForDate(date)
            }
            
            print("DiaryEntry deleted successfully")
        } catch {
            print("Error deleting diary entry: \(error)")
        }
    }
    
    private func renumberEntriesForDate(_ date: Date) {
        let context = persistenceController.container.viewContext
        let startOfDay = Calendar.current.startOfDay(for: date)
        
        // Get all entries for this date, sorted by creation order
        let entriesForDate = diaryEntries
            .filter { entry in
                guard let entryDate = entry.date else { return false }
                return Calendar.current.isDate(entryDate, inSameDayAs: startOfDay)
            }
            .sorted { ($0.date ?? Date()) < ($1.date ?? Date()) }
        
        // Renumber entries sequentially
        for (index, entry) in entriesForDate.enumerated() {
            entry.entryNumber = Int16(index + 1)
        }
        
        do {
            try context.save()
        } catch {
            print("Error renumbering entries: \(error)")
        }
    }
    
    func entryForDate(_ date: Date) -> DiaryEntry? {
        let startOfDay = Calendar.current.startOfDay(for: date)
        return diaryEntries.first { entry in
            guard let entryDate = entry.date else { return false }
            return Calendar.current.isDate(entryDate, inSameDayAs: startOfDay)
        }
    }
    
    func averageLoudnessForWeek() -> Double {
        let calendar = Calendar.current
        let weekAgo = calendar.date(byAdding: .day, value: -7, to: Date()) ?? Date()
        
        let recentEntries = diaryEntries.filter { entry in
            guard let date = entry.date else { return false }
            return date >= weekAgo
        }
        
        guard !recentEntries.isEmpty else { return 0 }
        
        let total = recentEntries.reduce(0) { $0 + Int($1.loudnessLevel) }
        return Double(total) / Double(recentEntries.count)
    }
    
    func averageStressForWeek() -> Double {
        let calendar = Calendar.current
        let weekAgo = calendar.date(byAdding: .day, value: -7, to: Date()) ?? Date()
        
        let recentEntries = diaryEntries.filter { entry in
            guard let date = entry.date else { return false }
            return date >= weekAgo
        }
        
        guard !recentEntries.isEmpty else { return 0 }
        
        let total = recentEntries.reduce(0) { $0 + Int($1.stressLevel) }
        return Double(total) / Double(recentEntries.count)
    }
    
    private func getNextEntryNumberForDate(_ date: Date) -> Int {
        let startOfDay = Calendar.current.startOfDay(for: date)
        let entriesForDate = diaryEntries.filter { entry in
            guard let entryDate = entry.date else { return false }
            return Calendar.current.isDate(entryDate, inSameDayAs: startOfDay)
        }
        return entriesForDate.count + 1
    }
    
    func getWeeklySummaries() -> [WeeklySummary] {
        let calendar = Calendar.current
        var summaries: [WeeklySummary] = []
        
        // Group entries by week with date tracking
        var weeklyGroups: [Date: [DiaryEntry]] = [:]
        
        for entry in diaryEntries {
            guard let date = entry.date else { continue }
            let weekOfYear = calendar.dateInterval(of: .weekOfYear, for: date)
            guard let weekStart = weekOfYear?.start else { continue }
            
            if weeklyGroups[weekStart] == nil {
                weeklyGroups[weekStart] = []
            }
            weeklyGroups[weekStart]?.append(entry)
        }
        
        // Generate last 4 weeks, including weeks with no data
        let currentDate = Date()
        var weekStarts: [Date] = []
        
        for i in 0..<4 {
            if let weekStart = calendar.dateInterval(of: .weekOfYear, for: calendar.date(byAdding: .weekOfYear, value: -i, to: currentDate) ?? currentDate)?.start {
                weekStarts.append(weekStart)
            }
        }
        
        // Sort oldest to newest for left-to-right display
        weekStarts.sort { $0 < $1 }
        
        for weekStart in weekStarts {
            let entries = weeklyGroups[weekStart] ?? []
            let avgLoudness = entries.isEmpty ? 0 : Double(entries.reduce(0) { $0 + Int($1.loudnessLevel) }) / Double(entries.count)
            let avgStress = entries.isEmpty ? 0 : Double(entries.reduce(0) { $0 + Int($1.stressLevel) }) / Double(entries.count)
            let totalListenTime = entries.reduce(0.0) { $0 + $1.sessionDuration }
            
            // Find previous week data (one week earlier)
            let previousWeekStart = calendar.date(byAdding: .weekOfYear, value: -1, to: weekStart)
            var previousWeekLoudness: Double? = nil
            var previousWeekStress: Double? = nil
            var previousWeekListeningTime: TimeInterval? = nil
            
            if let prevWeekStart = previousWeekStart,
               let prevWeekEntries = weeklyGroups[prevWeekStart], !prevWeekEntries.isEmpty {
                previousWeekLoudness = Double(prevWeekEntries.reduce(0) { $0 + Int($1.loudnessLevel) }) / Double(prevWeekEntries.count)
                previousWeekStress = Double(prevWeekEntries.reduce(0) { $0 + Int($1.stressLevel) }) / Double(prevWeekEntries.count)
                previousWeekListeningTime = prevWeekEntries.reduce(0.0) { $0 + $1.sessionDuration }
            }
            
            let weekKey = weekStart.formatted(date: .abbreviated, time: .omitted)
            let summary = WeeklySummary(
                weekOf: weekKey,
                entryCount: entries.count,
                avgLoudness: avgLoudness,
                avgStress: avgStress,
                totalListeningTime: totalListenTime,
                previousWeekLoudness: previousWeekLoudness,
                previousWeekStress: previousWeekStress,
                previousWeekListeningTime: previousWeekListeningTime
            )
            summaries.append(summary)
        }
        
        return summaries
    }
    
    func getWeeklySummariesForOffset(_ weekOffset: Int) -> [WeeklySummary] {
        let calendar = Calendar.current
        var summaries: [WeeklySummary] = []
        
        // Group entries by week with date tracking
        var weeklyGroups: [Date: [DiaryEntry]] = [:]
        
        for entry in diaryEntries {
            guard let date = entry.date else { continue }
            let weekOfYear = calendar.dateInterval(of: .weekOfYear, for: date)
            guard let weekStart = weekOfYear?.start else { continue }
            
            if weeklyGroups[weekStart] == nil {
                weeklyGroups[weekStart] = []
            }
            weeklyGroups[weekStart]?.append(entry)
        }
        
        // Generate the two weeks to display based on offset
        let currentDate = Date()
        var weekStarts: [Date] = []
        
        // Get previous week and current week relative to the offset
        for i in 0..<2 {
            let weekIndex = weekOffset - 1 + i // -1 for previous week, 0 for current week
            if let weekStart = calendar.dateInterval(of: .weekOfYear, for: calendar.date(byAdding: .weekOfYear, value: weekIndex, to: currentDate) ?? currentDate)?.start {
                weekStarts.append(weekStart)
            }
        }
        
        // Sort oldest to newest for left-to-right display
        weekStarts.sort { $0 < $1 }
        
        for weekStart in weekStarts {
            let entries = weeklyGroups[weekStart] ?? []
            let avgLoudness = entries.isEmpty ? 0 : Double(entries.reduce(0) { $0 + Int($1.loudnessLevel) }) / Double(entries.count)
            let avgStress = entries.isEmpty ? 0 : Double(entries.reduce(0) { $0 + Int($1.stressLevel) }) / Double(entries.count)
            let totalListenTime = entries.reduce(0.0) { $0 + $1.sessionDuration }
            
            // Find previous week data (one week earlier)
            let previousWeekStart = calendar.date(byAdding: .weekOfYear, value: -1, to: weekStart)
            var previousWeekLoudness: Double? = nil
            var previousWeekStress: Double? = nil
            var previousWeekListeningTime: TimeInterval? = nil
            
            if let prevWeekStart = previousWeekStart,
               let prevWeekEntries = weeklyGroups[prevWeekStart], !prevWeekEntries.isEmpty {
                previousWeekLoudness = Double(prevWeekEntries.reduce(0) { $0 + Int($1.loudnessLevel) }) / Double(prevWeekEntries.count)
                previousWeekStress = Double(prevWeekEntries.reduce(0) { $0 + Int($1.stressLevel) }) / Double(prevWeekEntries.count)
                previousWeekListeningTime = prevWeekEntries.reduce(0.0) { $0 + $1.sessionDuration }
            }
            
            let weekKey = weekStart.formatted(date: .abbreviated, time: .omitted)
            let summary = WeeklySummary(
                weekOf: weekKey,
                entryCount: entries.count,
                avgLoudness: avgLoudness,
                avgStress: avgStress,
                totalListeningTime: totalListenTime,
                previousWeekLoudness: previousWeekLoudness,
                previousWeekStress: previousWeekStress,
                previousWeekListeningTime: previousWeekListeningTime
            )
            summaries.append(summary)
        }
        
        return summaries
    }
    
    func getTotalListeningTime() -> TimeInterval {
        return diaryEntries.reduce(0.0) { $0 + $1.sessionDuration }
    }
    
    func getAverageListeningTime() -> TimeInterval {
        let validEntries = diaryEntries.filter { $0.sessionDuration > 0 }
        guard !validEntries.isEmpty else { return 0 }
        return getTotalListeningTime() / Double(validEntries.count)
    }
    
    func getOverallAverageStress() -> Double {
        guard !diaryEntries.isEmpty else { return 0 }
        let total = diaryEntries.reduce(0) { $0 + Int($1.stressLevel) }
        return Double(total) / Double(diaryEntries.count)
    }
    
    func getOverallAverageLoudness() -> Double {
        guard !diaryEntries.isEmpty else { return 0 }
        let total = diaryEntries.reduce(0) { $0 + Int($1.loudnessLevel) }
        return Double(total) / Double(diaryEntries.count)
    }
    
    
    func getMostCommonFrequency() -> Float {
        let frequencies = diaryEntries.compactMap { $0.currentFrequency > 0 ? $0.currentFrequency : nil }
        guard !frequencies.isEmpty else { return 0 }
        
        // Group frequencies in ranges and find most common
        var frequencyGroups: [String: Int] = [:]
        for freq in frequencies {
            let key = formatFrequency(freq)
            frequencyGroups[key, default: 0] += 1
        }
        
        // Find the most common frequency group
        guard let mostCommonGroup = frequencyGroups.max(by: { $0.value < $1.value }) else {
            return 0
        }
        
        // Extract the frequency value from the most common group key (e.g., "1000Hz" -> 1000)
        let frequencyString = mostCommonGroup.key.replacingOccurrences(of: "Hz", with: "")
        return Float(frequencyString) ?? 0
    }
    
    func formatFrequency(_ frequency: Float) -> String {
        let rounded = round(frequency / 100) * 100
        return "\(Int(rounded))Hz"
    }
    
    
    // MARK: - Calendar Session Aggregation
    
    func getSessionMinutesForDate(_ date: Date) -> Double {
        let startOfDay = Calendar.current.startOfDay(for: date)
        let endOfDay = Calendar.current.date(byAdding: .day, value: 1, to: startOfDay) ?? startOfDay
        
        let entriesForDate = diaryEntries.filter { entry in
            guard let entryDate = entry.date else { return false }
            return entryDate >= startOfDay && entryDate < endOfDay
        }
        
        let totalSeconds = entriesForDate.reduce(0.0) { $0 + $1.sessionDuration }
        return totalSeconds / 60.0 // Convert to minutes
    }
    
    func getMonthlySessionMinutes(for month: Date) -> Double {
        let calendar = Calendar.current
        guard let monthInterval = calendar.dateInterval(of: .month, for: month) else {
            return 0
        }
        
        let entriesForMonth = diaryEntries.filter { entry in
            guard let entryDate = entry.date else { return false }
            return entryDate >= monthInterval.start && entryDate < monthInterval.end
        }
        
        let totalSeconds = entriesForMonth.reduce(0.0) { $0 + $1.sessionDuration }
        return totalSeconds / 60.0 // Convert to minutes
    }
}