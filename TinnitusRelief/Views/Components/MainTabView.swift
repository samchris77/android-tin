import SwiftUI
import UserNotifications

// MARK: - Reminder Data Models
enum WeekDay: String, CaseIterable, Codable {
    case monday = "Monday"
    case tuesday = "Tuesday"
    case wednesday = "Wednesday"
    case thursday = "Thursday"
    case friday = "Friday"
    case saturday = "Saturday"
    case sunday = "Sunday"
    
    var shortName: String {
        switch self {
        case .monday: return "Mon"
        case .tuesday: return "Tue"
        case .wednesday: return "Wed"
        case .thursday: return "Thu"
        case .friday: return "Fri"
        case .saturday: return "Sat"
        case .sunday: return "Sun"
        }
    }
    
    var weekdayNumber: Int {
        switch self {
        case .sunday: return 1
        case .monday: return 2
        case .tuesday: return 3
        case .wednesday: return 4
        case .thursday: return 5
        case .friday: return 6
        case .saturday: return 7
        }
    }
}

struct ReminderItem: Identifiable, Codable {
    var id = UUID()
    var time: Date
    var isEnabled: Bool
    var selectedDays: Set<WeekDay>
    var title: String
    
    init(id: UUID = UUID(), time: Date = Date(), isEnabled: Bool = true, selectedDays: Set<WeekDay> = Set(WeekDay.allCases), title: String = "Track your tinnitus") {
        self.id = id
        self.time = time
        self.isEnabled = isEnabled
        self.selectedDays = selectedDays
        self.title = title
    }
    
    var formattedTime: String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: time)
    }
    
    var formattedDays: String {
        if selectedDays.count == 7 {
            return "Every day"
        } else if selectedDays.count == 5 && !selectedDays.contains(.saturday) && !selectedDays.contains(.sunday) {
            return "Weekdays"
        } else if selectedDays.count == 2 && selectedDays.contains(.saturday) && selectedDays.contains(.sunday) {
            return "Weekends"
        } else {
            return selectedDays.sorted { $0.weekdayNumber < $1.weekdayNumber }
                .map { $0.shortName }
                .joined(separator: ", ")
        }
    }
}

class ReminderManager: ObservableObject {
    @Published var reminders: [ReminderItem] = []
    
    private let userDefaults = UserDefaults.standard
    private let reminderKey = "savedReminders"
    
    init() {
        loadReminders()
    }
    
    func addReminder(_ reminder: ReminderItem) {
        reminders.append(reminder)
        saveReminders()
        if reminder.isEnabled {
            scheduleNotifications(for: reminder)
        }
    }
    
    func updateReminder(_ reminder: ReminderItem) {
        if let index = reminders.firstIndex(where: { $0.id == reminder.id }) {
            reminders[index] = reminder
            saveReminders()
            
            // Cancel existing notifications for this reminder
            cancelNotifications(for: reminder.id)
            
            // Schedule new notifications if enabled
            if reminder.isEnabled {
                scheduleNotifications(for: reminder)
            }
        }
    }
    
    func deleteReminder(_ reminder: ReminderItem) {
        cancelNotifications(for: reminder.id)
        reminders.removeAll { $0.id == reminder.id }
        saveReminders()
    }
    
    func toggleReminder(_ reminder: ReminderItem) {
        var updatedReminder = reminder
        updatedReminder.isEnabled.toggle()
        updateReminder(updatedReminder)
    }
    
    private func saveReminders() {
        if let encoded = try? JSONEncoder().encode(reminders) {
            userDefaults.set(encoded, forKey: reminderKey)
        }
    }
    
    private func loadReminders() {
        if let data = userDefaults.data(forKey: reminderKey),
           let decoded = try? JSONDecoder().decode([ReminderItem].self, from: data) {
            reminders = decoded
        }
    }
    
    private func scheduleNotifications(for reminder: ReminderItem) {
        let content = UNMutableNotificationContent()
        content.title = "Tinnitus Tracking Reminder"
        content.body = reminder.title
        content.sound = .default
        
        let calendar = Calendar.current
        let timeComponents = calendar.dateComponents([.hour, .minute], from: reminder.time)
        
        for day in reminder.selectedDays {
            var dateComponents = DateComponents()
            dateComponents.hour = timeComponents.hour
            dateComponents.minute = timeComponents.minute
            dateComponents.weekday = day.weekdayNumber
            
            let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
            let identifier = "\(reminder.id.uuidString)_\(day.rawValue)"
            let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
            
            UNUserNotificationCenter.current().add(request) { error in
                if let error = error {
                    print("Failed to schedule notification: \(error)")
                }
            }
        }
    }
    
    private func cancelNotifications(for reminderID: UUID) {
        let identifiers = WeekDay.allCases.map { "\(reminderID.uuidString)_\($0.rawValue)" }
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: identifiers)
    }
    
    func cancelAllNotifications() {
        for reminder in reminders {
            cancelNotifications(for: reminder.id)
        }
    }
}

// MARK: - Main Tab View
struct MainTabView: View {
    @State private var selectedTab = 0
    
    var body: some View {
        VStack(spacing: 0) {
            // Frequency Controller positioned at top (shown on all pages)
            FrequencyController()
                .zIndex(1)
            
            TabView(selection: $selectedTab) {
                FrequencyMatchingView(selectedTab: $selectedTab)
                    .tabItem {
                        Image(systemName: selectedTab == 0 ? "waveform.path" : "waveform.path")
                        Text("Frequency")
                    }
                    .tag(0)
                
                DiaryView()
                    .tabItem {
                        Image(systemName: selectedTab == 1 ? "book.fill" : "book")
                        Text("Log")
                    }
                    .tag(1)
                
                CombinedProfileView()
                    .tabItem {
                        Image(systemName: selectedTab == 2 ? "person.fill" : "person")
                        Text("Profile")
                    }
                    .tag(2)
            }
            .accentColor(.orange)
        }
    }
}

struct CombinedProfileView: View {
    @StateObject private var reminderManager = ReminderManager()
    @State private var showingReminderEdit = false
    @State private var editingReminder: ReminderItem?
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    settingsSection
                    
                    progressSection
                    
                    aboutSection
                    
                    privacySection
                }
                .padding()
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
            .sheet(isPresented: $showingReminderEdit) {
                ReminderEditView(reminderManager: reminderManager, existingReminder: editingReminder)
            }
        }
    }
    
    private func openPrivacyPolicy() {
        if let url = URL(string: "https://tinweb-3544e.web.app/privacy") {
            UIApplication.shared.open(url)
        }
    }
    
    private var progressSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Your Progress")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 12) {
                ProgressCardView(
                    title: "Days Tracked",
                    value: "7",
                    subtitle: "Keep up the good work!",
                    color: .green
                )
                
                ProgressCardView(
                    title: "Average Loudness",
                    value: "4.2",
                    subtitle: "Down from last week",
                    color: .orange
                )
                
                ProgressCardView(
                    title: "Frequency Sessions",
                    value: "12",
                    subtitle: "This month",
                    color: .blue
                )
            }
        }
    }
    
    
    private var settingsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Daily Reminders")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 0) {
                AdvancedReminderSettingsSection(
                    reminderManager: reminderManager,
                    showingReminderEdit: $showingReminderEdit,
                    editingReminder: $editingReminder
                )
            }
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
            )
        }
    }
    
    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("About")
                .font(.headline)
                .foregroundColor(.primary)
            
            Text("TinnitusRelief is designed to help you manage tinnitus symptoms through personalized frequency matching, progress tracking, and evidence-based techniques.")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.leading)
            
            Text("Remember: This app is not a substitute for professional medical advice. Please consult with a healthcare provider for proper diagnosis and treatment.")
                .font(.caption)
                .foregroundColor(.secondary)
                .padding(.top, 8)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
    
    private var privacySection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Privacy")
                .font(.headline)
                .foregroundColor(.primary)
            
            Button(action: {
                openPrivacyPolicy()
            }) {
                HStack(spacing: 12) {
                    Image(systemName: "lock")
                        .font(.system(size: 20))
                        .foregroundColor(.orange)
                        .frame(width: 32)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Privacy Policy")
                            .font(.body)
                            .foregroundColor(.primary)
                            .multilineTextAlignment(.leading)
                        
                        Text("Learn how we protect your information")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.leading)
                    }
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding(.vertical, 4)
            }
            .buttonStyle(PlainButtonStyle())
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
            )
        }
    }
}

struct ProgressCardView: View {
    let title: String
    let value: String
    let subtitle: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Text(value)
                .font(.system(size: 24, weight: .bold, design: .monospaced))
                .foregroundColor(color)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
}

struct PreferenceRowView: View {
    let title: String
    let value: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Text(title)
                    .font(.body)
                    .foregroundColor(.primary)
                
                Spacer()
                
                Text(value)
                    .font(.body)
                    .foregroundColor(.secondary)
                
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding()
        }
        .buttonStyle(PlainButtonStyle())
    }
}


struct SettingsRowView: View {
    let title: String
    let subtitle: String
    let icon: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(.orange)
                    .frame(width: 32)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.body)
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.leading)
                    
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.leading)
                }
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding(.vertical, 4)
        }
        .buttonStyle(PlainButtonStyle())
    }
}


struct AdvancedReminderSettingsSection: View {
    @ObservedObject var reminderManager: ReminderManager
    @Binding var showingReminderEdit: Bool
    @Binding var editingReminder: ReminderItem?
    
    var body: some View {
        VStack(spacing: 12) {
            // Add Reminder Button
            Button(action: {
                editingReminder = nil
                showingReminderEdit = true
            }) {
                HStack(spacing: 12) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 20))
                        .foregroundColor(.orange)
                        .frame(width: 32)
                    
                    Text("Add New Reminder")
                        .font(.body)
                        .foregroundColor(.orange)
                        .multilineTextAlignment(.leading)
                    
                    Spacer()
                }
                .padding(.vertical, 12)
                .padding(.horizontal, 16)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(.orange.opacity(0.5), lineWidth: 1)
                        .fill(.orange.opacity(0.1))
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            // Existing Reminders List
            if reminderManager.reminders.isEmpty {
                VStack(spacing: 8) {
                    Image(systemName: "bell.slash")
                        .font(.system(size: 32))
                        .foregroundColor(.secondary)
                    
                    Text("No reminders set")
                        .font(.body)
                        .foregroundColor(.secondary)
                    
                    Text("Tap 'Add New Reminder' to get started")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding(.vertical, 20)
            } else {
                ForEach(reminderManager.reminders) { reminder in
                    ReminderListItem(
                        reminder: reminder,
                        onToggle: {
                            reminderManager.toggleReminder(reminder)
                        },
                        onEdit: {
                            editingReminder = reminder
                            showingReminderEdit = true
                        },
                        onDelete: {
                            reminderManager.deleteReminder(reminder)
                        }
                    )
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(.ultraThinMaterial)
                    )
                }
            }
        }
        .onAppear {
            requestNotificationPermissionIfNeeded()
        }
    }
    
    private func requestNotificationPermissionIfNeeded() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            if settings.authorizationStatus == .notDetermined {
                UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
                    if let error = error {
                        print("Failed to request notification permission: \(error)")
                    }
                }
            }
        }
    }
}

// MARK: - Reminder Edit View
struct ReminderEditView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject var reminderManager: ReminderManager
    
    @State private var reminderTime = Date()
    @State private var selectedDays: Set<WeekDay> = Set(WeekDay.allCases)
    @State private var reminderTitle = "Track your tinnitus"
    @State private var isEnabled = true
    
    let existingReminder: ReminderItem?
    
    init(reminderManager: ReminderManager, existingReminder: ReminderItem? = nil) {
        self.reminderManager = reminderManager
        self.existingReminder = existingReminder
        
        if let reminder = existingReminder {
            _reminderTime = State(initialValue: reminder.time)
            _selectedDays = State(initialValue: reminder.selectedDays)
            _reminderTitle = State(initialValue: reminder.title)
            _isEnabled = State(initialValue: reminder.isEnabled)
        }
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Time Selection
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Time")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        DatePicker("Reminder Time", selection: $reminderTime, displayedComponents: .hourAndMinute)
                            .datePickerStyle(.wheel)
                            .labelsHidden()
                    }
                    
                    Divider()
                    
                    // Day Selection
                    WeekDaySelector(selectedDays: $selectedDays)
                    
                    Divider()
                    
                    // Title/Message
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Message")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        TextField("Reminder message", text: $reminderTitle)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    
                    Divider()
                    
                    // Enable/Disable Toggle
                    VStack(alignment: .leading, spacing: 16) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Enable Reminder")
                                    .font(.headline)
                                    .foregroundColor(.primary)
                                
                                Text("Turn on to receive notifications")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $isEnabled)
                                .labelsHidden()
                                .toggleStyle(SwitchToggleStyle(tint: .orange))
                        }
                    }
                    
                    Spacer(minLength: 20)
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
            }
            .navigationTitle(existingReminder == nil ? "New Reminder" : "Edit Reminder")
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarItems(
                leading: Button("Cancel") {
                    presentationMode.wrappedValue.dismiss()
                },
                trailing: Button("Save") {
                    saveReminder()
                }
                .fontWeight(.semibold)
                .disabled(selectedDays.isEmpty || reminderTitle.isEmpty)
            )
        }
    }
    
    private func saveReminder() {
        let reminder: ReminderItem
        
        if let existingReminder = existingReminder {
            // Keep the same ID for existing reminder
            reminder = ReminderItem(
                id: existingReminder.id,
                time: reminderTime,
                isEnabled: isEnabled,
                selectedDays: selectedDays,
                title: reminderTitle
            )
            reminderManager.updateReminder(reminder)
        } else {
            // Create new reminder with new ID
            reminder = ReminderItem(
                time: reminderTime,
                isEnabled: isEnabled,
                selectedDays: selectedDays,
                title: reminderTitle
            )
            reminderManager.addReminder(reminder)
        }
        
        presentationMode.wrappedValue.dismiss()
    }
}

// MARK: - Reminder List Item
struct ReminderListItem: View {
    let reminder: ReminderItem
    let onToggle: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(reminder.formattedTime)
                        .font(.title2)
                        .fontWeight(.semibold)
                        .foregroundColor(.primary)
                    
                    Text(reminder.formattedDays)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    if !reminder.title.isEmpty && reminder.title != "Track your tinnitus" {
                        Text(reminder.title)
                            .font(.body)
                            .foregroundColor(.primary)
                            .padding(.top, 2)
                    }
                }
                
                Spacer()
                
                Toggle("", isOn: .constant(reminder.isEnabled))
                    .labelsHidden()
                    .toggleStyle(SwitchToggleStyle(tint: .orange))
                    .onTapGesture {
                        onToggle()
                    }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color.clear)
            .onTapGesture {
                onEdit()
            }
            .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                Button(role: .destructive) {
                    onDelete()
                } label: {
                    Label("Delete", systemImage: "trash")
                }
                
                Button {
                    onEdit()
                } label: {
                    Label("Edit", systemImage: "pencil")
                }
                .tint(.orange)
            }
        }
    }
}

// MARK: - WeekDay Selector Component
struct WeekDaySelector: View {
    @Binding var selectedDays: Set<WeekDay>
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Repeat on")
                .font(.headline)
                .foregroundColor(.primary)
            
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 4), spacing: 8) {
                ForEach(WeekDay.allCases, id: \.self) { day in
                    WeekDayButton(
                        day: day,
                        isSelected: selectedDays.contains(day)
                    ) {
                        if selectedDays.contains(day) {
                            selectedDays.remove(day)
                        } else {
                            selectedDays.insert(day)
                        }
                    }
                }
            }
            
            HStack(spacing: 16) {
                Button("Every day") {
                    selectedDays = Set(WeekDay.allCases)
                }
                .font(.caption)
                .foregroundColor(.orange)
                
                Button("Weekdays") {
                    selectedDays = Set([.monday, .tuesday, .wednesday, .thursday, .friday])
                }
                .font(.caption)
                .foregroundColor(.orange)
                
                Button("Weekends") {
                    selectedDays = Set([.saturday, .sunday])
                }
                .font(.caption)
                .foregroundColor(.orange)
                
                Spacer()
            }
        }
    }
}

struct WeekDayButton: View {
    let day: WeekDay
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(day.shortName)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(isSelected ? .white : .primary)
                .frame(minWidth: 44, minHeight: 32)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(isSelected ? .orange : .gray.opacity(0.2))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(isSelected ? .orange : .gray.opacity(0.3), lineWidth: 1)
                )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    MainTabView()
}