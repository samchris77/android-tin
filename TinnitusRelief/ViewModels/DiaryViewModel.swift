import SwiftUI
import CoreData
import Combine

struct WeeklySummary {
    let weekOf: String
    let entryCount: Int
    let avgLoudness: Double
    let avgStress: Double
    let avgComfort: Double
    let totalListeningTime: TimeInterval
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
    
    func createEntry(loudness: Int16, comfort: Int16, stress: Int16, notes: String?) {
        let context = persistenceController.container.viewContext
        let audioManager = UnifiedAudioEngineManager.shared
        
        let entry = DiaryEntry(context: context)
        entry.id = UUID()
        entry.date = Calendar.current.startOfDay(for: selectedDate)
        entry.loudnessLevel = loudness
        entry.comfortLevel = comfort
        entry.stressLevel = stress
        entry.notes = notes
        
        // Auto-populate frequency and volume data
        entry.currentFrequency = audioManager.currentFrequency
        entry.currentVolume = audioManager.currentFrequencyVolume
        
        // Calculate session duration (placeholder for now - would need session tracking)
        entry.sessionDuration = 0.0
        
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
        
        // Group entries by week
        var weeklyGroups: [String: [DiaryEntry]] = [:]
        
        for entry in diaryEntries {
            guard let date = entry.date else { continue }
            let weekOfYear = calendar.dateInterval(of: .weekOfYear, for: date)
            let weekKey = weekOfYear?.start.formatted(date: .abbreviated, time: .omitted) ?? ""
            
            if weeklyGroups[weekKey] == nil {
                weeklyGroups[weekKey] = []
            }
            weeklyGroups[weekKey]?.append(entry)
        }
        
        // Create summaries
        for (weekKey, entries) in weeklyGroups.sorted(by: { $0.key > $1.key }).prefix(4) {
            let avgLoudness = entries.isEmpty ? 0 : Double(entries.reduce(0) { $0 + Int($1.loudnessLevel) }) / Double(entries.count)
            let avgStress = entries.isEmpty ? 0 : Double(entries.reduce(0) { $0 + Int($1.stressLevel) }) / Double(entries.count)
            let avgComfort = entries.isEmpty ? 0 : Double(entries.reduce(0) { $0 + Int($1.comfortLevel) }) / Double(entries.count)
            let totalListenTime = entries.reduce(0.0) { $0 + $1.sessionDuration }
            
            let summary = WeeklySummary(
                weekOf: weekKey,
                entryCount: entries.count,
                avgLoudness: avgLoudness,
                avgStress: avgStress,
                avgComfort: avgComfort,
                totalListeningTime: totalListenTime
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
    
    func getOverallAverageComfort() -> Double {
        guard !diaryEntries.isEmpty else { return 0 }
        let total = diaryEntries.reduce(0) { $0 + Int($1.comfortLevel) }
        return Double(total) / Double(diaryEntries.count)
    }
    
    func getMostCommonFrequency() -> Float {
        let frequencies = diaryEntries.compactMap { $0.currentFrequency > 0 ? $0.currentFrequency : nil }
        guard !frequencies.isEmpty else { return 0 }
        
        // Group frequencies in ranges and find most common
        var frequencyGroups: [String: Int] = [:]
        for freq in frequencies {
            let key = formatFrequencyRange(freq)
            frequencyGroups[key, default: 0] += 1
        }
        
        let _ = frequencyGroups.max { $0.value < $1.value }
        return frequencies.reduce(0, +) / Float(frequencies.count) // Return average for now
    }
    
    private func formatFrequencyRange(_ frequency: Float) -> String {
        let rounded = round(frequency / 100) * 100
        return "\(Int(rounded))Hz"
    }
    
    func exportToCSV() -> String {
        var csvContent = "Date,Loudness Level,Comfort Level,Stress Level,Notes\n"
        
        for entry in diaryEntries {
            let dateString = entry.date?.formatted(date: .numeric, time: .omitted) ?? ""
            let notes = entry.notes?.replacingOccurrences(of: ",", with: ";") ?? ""
            
            csvContent += "\(dateString),\(entry.loudnessLevel),\(entry.comfortLevel),\(entry.stressLevel),\"\(notes)\"\n"
        }
        
        return csvContent
    }
}