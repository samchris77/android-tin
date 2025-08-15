import SwiftUI
import UserNotifications
import CoreData

// MARK: - Tutorial System

enum TutorialStep: Int, CaseIterable {
    case welcome = 0
    case frequencyDemo
    case logDemo
    case profileDemo
    
    var title: String {
        switch self {
        case .welcome:
            return "Welcome to Tinnitus Relief"
        case .frequencyDemo:
            return "Match Your Tinnitus Frequency"
        case .logDemo:
            return "Track Your Progress"
        case .profileDemo:
            return "Setup Reminders"
        }
    }
    
    var description: String {
        switch self {
        case .welcome:
            return "This app helps reduce your tinnitus through sound therapy. Set volume to 70% of your tinnitus loudness and listen for about 2 hours daily for best results."
        case .frequencyDemo:
            return "Drag the control ball to match your tinnitus frequency and sound type. The arrows show you can move it in any direction."
        case .logDemo:
            return "Add entries, view your progress charts, and review your therapy history to track improvement over time."
        case .profileDemo:
            return "Set up daily reminders to help you maintain a consistent 2-hour therapy routine for maximum effectiveness."
        }
    }
    
    var buttonText: String {
        switch self {
        case .welcome, .frequencyDemo, .logDemo:
            return "Next"
        case .profileDemo:
            return "Start Therapy"
        }
    }
    
    var hasSkipOption: Bool {
        switch self {
        case .welcome:
            return true
        default:
            return false
        }
    }
    
    var targetTab: Int? {
        switch self {
        case .welcome:
            return nil // Stay on current tab
        case .frequencyDemo:
            return 0 // Frequency tab
        case .logDemo:
            return 1 // Log tab
        case .profileDemo:
            return 2 // Profile tab
        }
    }
}

class TutorialManager: ObservableObject {
    @Published var isShowingTutorial: Bool = false
    @Published var currentStep: TutorialStep = .welcome
    
    private let persistenceController: PersistenceController
    private var onTabChange: ((Int) -> Void)?
    
    init(persistenceController: PersistenceController) {
        self.persistenceController = persistenceController
        checkOnboardingStatus()
    }
    
    func setTabChangeCallback(_ callback: @escaping (Int) -> Void) {
        self.onTabChange = callback
    }
    
    func checkOnboardingStatus() {
        let context = persistenceController.container.viewContext
        let request: NSFetchRequest<UserProfile> = UserProfile.fetchRequest()
        
        do {
            let profiles = try context.fetch(request)
            if let profile = profiles.first {
                isShowingTutorial = !profile.hasCompletedOnboarding
            } else {
                let newProfile = UserProfile(context: context)
                newProfile.hasCompletedOnboarding = false
                newProfile.id = UUID()
                try context.save()
                isShowingTutorial = true
            }
        } catch {
            print("Error checking onboarding status: \(error)")
            isShowingTutorial = true
        }
    }
    
    func nextStep() {
        withAnimation(.easeInOut(duration: 0.3)) {
            let nextStepRawValue = currentStep.rawValue + 1
            if let nextStep = TutorialStep(rawValue: nextStepRawValue) {
                currentStep = nextStep
                // Navigate to appropriate tab if needed
                if let targetTab = nextStep.targetTab {
                    onTabChange?(targetTab)
                }
            } else {
                completeTutorial()
            }
        }
    }
    
    func skipTutorial() {
        withAnimation(.easeInOut(duration: 0.3)) {
            completeTutorial()
        }
    }
    
    func completeTutorial() {
        let context = persistenceController.container.viewContext
        let request: NSFetchRequest<UserProfile> = UserProfile.fetchRequest()
        
        do {
            let profiles = try context.fetch(request)
            if let profile = profiles.first {
                profile.hasCompletedOnboarding = true
                try context.save()
            }
        } catch {
            print("Error completing tutorial: \(error)")
        }
        
        isShowingTutorial = false
    }
    
    func resetTutorial() {
        let context = persistenceController.container.viewContext
        let request: NSFetchRequest<UserProfile> = UserProfile.fetchRequest()
        
        do {
            let profiles = try context.fetch(request)
            if let profile = profiles.first {
                profile.hasCompletedOnboarding = false
                try context.save()
            }
        } catch {
            print("Error resetting tutorial: \(error)")
        }
        
        currentStep = .welcome
        isShowingTutorial = true
    }
}

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
    @Environment(\.managedObjectContext) private var viewContext
    @StateObject private var tutorialManager: TutorialManager
    @StateObject private var frequencyViewModel = FrequencyMatchingViewModel()
    
    init() {
        let persistenceController = PersistenceController.shared
        self._tutorialManager = StateObject(wrappedValue: TutorialManager(persistenceController: persistenceController))
    }
    
    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                // Frequency Controller positioned at top (shown on all pages)
                FrequencyController()
                    .zIndex(1)
                
                TabView(selection: $selectedTab) {
                    FrequencyMatchingView(viewModel: frequencyViewModel, selectedTab: $selectedTab)
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
                    
                    CombinedProfileView(tutorialManager: tutorialManager)
                        .tabItem {
                            Image(systemName: selectedTab == 2 ? "person.fill" : "person")
                            Text("Profile")
                        }
                        .tag(2)
                }
                .accentColor(.orange)
            }
            
            // Tutorial overlay
            if tutorialManager.isShowingTutorial {
                tutorialOverlay
            }
        }
        .onAppear {
            // Set up tutorial tab navigation callback
            tutorialManager.setTabChangeCallback { newTab in
                selectedTab = newTab
            }
        }
    }
    
    @ViewBuilder
    private var tutorialOverlay: some View {
        switch tutorialManager.currentStep {
        case .welcome:
            WelcomeTutorialView(tutorialManager: tutorialManager)
                .transition(.opacity)
        case .frequencyDemo:
            GeometryReader { geometry in
                // Account for FrequencyController height (~56px) + padding
                let frequencyControllerHeight: CGFloat = 56
                let safeArea = geometry.safeAreaInsets
                let tabViewStartY = safeArea.top + frequencyControllerHeight
                
                // Use the same calculations as FrequencyMatchingView but adjusted for FrequencyController offset
                let availableHeight = geometry.size.height - tabViewStartY - safeArea.bottom - 60
                let availableWidth = geometry.size.width - 20
                let controlAreaFrame = CGRect(
                    x: 10,
                    y: tabViewStartY + 40,
                    width: availableWidth,
                    height: availableHeight
                )
                
                let controlPosition = CGPoint(
                    x: controlAreaFrame.minX + controlAreaFrame.width * frequencyViewModel.controlPosition.x,
                    y: controlAreaFrame.minY + controlAreaFrame.height * frequencyViewModel.controlPosition.y
                )
                FrequencyDemoOverlayView(tutorialManager: tutorialManager, controlPosition: controlPosition)
            }
            .transition(.opacity)
        case .logDemo:
            LogDemoOverlayView(tutorialManager: tutorialManager)
                .transition(.opacity)
        case .profileDemo:
            TransparentTutorialOverlayView(tutorialManager: tutorialManager)
                .transition(.opacity)
        }
    }
}

struct CombinedProfileView: View {
    @StateObject private var reminderManager = ReminderManager()
    @State private var showingReminderEdit = false
    @State private var editingReminder: ReminderItem?
    let tutorialManager: TutorialManager
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    settingsSection
                    
                    tutorialSection
                    
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
    
    private var tutorialSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Help & Tutorial")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 0) {
                Button(action: {
                    tutorialManager.resetTutorial()
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: "graduationcap")
                            .font(.system(size: 20))
                            .foregroundColor(.orange)
                            .frame(width: 32)
                        
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Restart Tutorial")
                                .font(.body)
                                .foregroundColor(.primary)
                                .multilineTextAlignment(.leading)
                            
                            Text("Learn how to use the app effectively")
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .multilineTextAlignment(.leading)
                        }
                        
                        Spacer()
                        
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .contentShape(Rectangle())
                }
                .buttonStyle(PlainButtonStyle())
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

// MARK: - Tutorial UI Components

struct ArrowIndicatorView: View {
    let direction: ArrowDirection
    @State private var isAnimating = false
    
    enum ArrowDirection {
        case up, down, left, right
        
        var angle: Double {
            switch self {
            case .up: return 0
            case .right: return 90
            case .down: return 180
            case .left: return 270
            }
        }
        
        var offset: (x: CGFloat, y: CGFloat) {
            switch self {
            case .up: return (0, -40)
            case .right: return (40, 0)
            case .down: return (0, 40)
            case .left: return (-40, 0)
            }
        }
    }
    
    var body: some View {
        Image(systemName: "arrow.up")
            .font(.title2)
            .foregroundColor(.orange)
            .rotationEffect(.degrees(direction.angle))
            .offset(x: direction.offset.x, y: direction.offset.y)
            .scaleEffect(isAnimating ? 1.2 : 1.0)
            .opacity(isAnimating ? 1.0 : 0.7)
            .animation(.easeInOut(duration: 1.0).repeatForever(autoreverses: true), value: isAnimating)
            .onAppear {
                isAnimating = true
            }
    }
}

struct FrequencyDemoOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    let controlPosition: CGPoint
    
    var body: some View {
        ZStack {
            // Very light background to show frequency view
            Color.black.opacity(0.3)
                .ignoresSafeArea()
            
            // Arrow indicators around control ball
            ZStack {
                ForEach(ArrowIndicatorView.ArrowDirection.allCases, id: \.self) { direction in
                    ArrowIndicatorView(direction: direction)
                }
            }
            .position(controlPosition)
            
            // Tutorial content at bottom
            VStack(spacing: 0) {
                Spacer()
                
                tutorialContentView
                    .padding(.horizontal, 24)
                    .padding(.bottom, 50)
            }
        }
    }
    
    private var tutorialContentView: some View {
        VStack(spacing: 20) {
            stepIndicator
            
            VStack(spacing: 12) {
                Text(tutorialManager.currentStep.title)
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                    .multilineTextAlignment(.center)
                
                Text(tutorialManager.currentStep.description)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .lineLimit(nil)
            }
            
            Button(action: {
                tutorialManager.nextStep()
            }) {
                Text(tutorialManager.currentStep.buttonText)
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.orange)
                    .cornerRadius(12)
            }
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThickMaterial)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                )
        )
    }
    
    private var stepIndicator: some View {
        HStack(spacing: 8) {
            ForEach(TutorialStep.allCases, id: \.rawValue) { step in
                Circle()
                    .fill(step.rawValue <= tutorialManager.currentStep.rawValue ? Color.orange : Color.gray.opacity(0.3))
                    .frame(width: 8, height: 8)
                    .scaleEffect(step == tutorialManager.currentStep ? 1.2 : 1.0)
                    .animation(.easeInOut(duration: 0.2), value: tutorialManager.currentStep)
            }
        }
        .padding(.bottom, 8)
    }
}

// Extension to make ArrowDirection CaseIterable and Hashable
extension ArrowIndicatorView.ArrowDirection: CaseIterable, Hashable {
    static var allCases: [ArrowIndicatorView.ArrowDirection] {
        return [.up, .right, .down, .left]
    }
}

struct TransparentTutorialOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    
    var body: some View {
        ZStack {
            // Very light background to show underlying content
            Color.black.opacity(0.4)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                Spacer()
                
                tutorialContentView
                    .padding(.horizontal, 24)
                    .padding(.bottom, 50)
            }
        }
    }
    
    private var tutorialContentView: some View {
        VStack(spacing: 20) {
            stepIndicator
            
            VStack(spacing: 12) {
                Text(tutorialManager.currentStep.title)
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                    .multilineTextAlignment(.center)
                
                Text(tutorialManager.currentStep.description)
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .lineLimit(nil)
            }
            
            VStack(spacing: 12) {
                Button(action: {
                    if tutorialManager.currentStep == .profileDemo {
                        tutorialManager.completeTutorial()
                    } else {
                        tutorialManager.nextStep()
                    }
                }) {
                    Text(tutorialManager.currentStep.buttonText)
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.orange)
                        .cornerRadius(12)
                }
                
                if tutorialManager.currentStep.hasSkipOption {
                    Button(action: {
                        tutorialManager.skipTutorial()
                    }) {
                        Text("Skip Tutorial")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThickMaterial)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                )
        )
    }
    
    private var stepIndicator: some View {
        HStack(spacing: 8) {
            ForEach(TutorialStep.allCases, id: \.rawValue) { step in
                Circle()
                    .fill(step.rawValue <= tutorialManager.currentStep.rawValue ? Color.orange : Color.gray.opacity(0.3))
                    .frame(width: 8, height: 8)
                    .scaleEffect(step == tutorialManager.currentStep ? 1.2 : 1.0)
                    .animation(.easeInOut(duration: 0.2), value: tutorialManager.currentStep)
            }
        }
        .padding(.bottom, 8)
    }
}

struct WelcomeTutorialView: View {
    @ObservedObject var tutorialManager: TutorialManager
    
    var body: some View {
        ZStack {
            // Completely opaque background with app branding colors
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.orange,
                    Color.blue,
                    Color.orange
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            // Solid overlay for better text readability
            Color.black.opacity(0.3)
                .ignoresSafeArea()
            
            VStack(spacing: 40) {
                Spacer()
                
                VStack(spacing: 24) {
                    Image(systemName: "ear")
                        .font(.system(size: 80))
                        .foregroundColor(.orange)
                        .symbolEffect(.pulse.byLayer, options: .repeating)
                    
                    VStack(spacing: 8) {
                        Text("Welcome to")
                            .font(.title2)
                            .foregroundColor(.white.opacity(0.9))
                        
                        Text("Tinnitus Relief")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                    }
                }
                
                VStack(spacing: 20) {
                    TutorialFeatureRow(
                        icon: "waveform",
                        title: "Match Your Tinnitus",
                        description: "Find the exact frequency and sound type"
                    )
                    
                    TutorialFeatureRow(
                        icon: "speaker.wave.2",
                        title: "Set Optimal Volume",
                        description: "70% of your tinnitus loudness for best results"
                    )
                    
                    TutorialFeatureRow(
                        icon: "clock",
                        title: "2 Hours Daily",
                        description: "Consistent therapy for effective relief"
                    )
                }
                .padding(.horizontal, 32)
                
                Spacer()
                
                VStack(spacing: 16) {
                    Button(action: {
                        tutorialManager.nextStep()
                    }) {
                        Text("Start Tutorial")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color.orange)
                            .cornerRadius(16)
                    }
                    
                    Button(action: {
                        tutorialManager.skipTutorial()
                    }) {
                        Text("Skip for Now")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.horizontal, 32)
                .padding(.bottom, 40)
            }
        }
    }
}

private struct TutorialFeatureRow: View {
    let icon: String
    let title: String
    let description: String
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.orange)
                .frame(width: 24)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(.white)
                
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.8))
            }
            
            Spacer()
        }
        .padding(.vertical, 8)
    }
}

struct LogDemoOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    @State private var animationPhase: Int = 0
    @State private var demoTinnitus: Int = 7
    @State private var demoStress: Int = 6
    @State private var showSaveButton = false
    @State private var showSaveConfirmation = false
    
    private let animationTimer = Timer.publish(every: 2.5, on: .main, in: .common).autoconnect()
    
    var body: some View {
        ZStack {
            // Complete background coverage to prevent any UI bleeding
            Rectangle()
                .fill(Color.black.opacity(0.95))
                .ignoresSafeArea(.all)
                .allowsHitTesting(true) // Block all touches to underlying content
            
            VStack(spacing: 0) {
                // Top area with tab highlight
                HStack {
                    if animationPhase >= 1 {
                        TabHighlight()
                            .padding(.leading, 20)
                    }
                    Spacer()
                }
                .frame(height: 60)
                .padding(.top, 40)
                
                // Main demo content area - contained and elevated
                VStack(spacing: 24) {
                    // Demo header
                    VStack(spacing: 8) {
                        Text("Aug 15, 2025")
                            .font(.headline)
                            .foregroundColor(.white)
                        
                        Text("11:29 AM")
                            .font(.subheadline)
                            .foregroundColor(.white.opacity(0.7))
                        
                        HStack {
                            Spacer()
                            VStack(alignment: .trailing, spacing: 2) {
                                Text("Entry")
                                    .font(.caption2)
                                    .foregroundColor(.white.opacity(0.7))
                                Text("#3")
                                    .font(.title3)
                                    .fontWeight(.bold)
                                    .foregroundColor(.orange)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color.white.opacity(0.1))
                    )
                    
                    // Tinnitus demo slider
                    DemoSlider(
                        title: "Tinnitus Level",
                        value: demoTinnitus,
                        color: .orange,
                        isAnimating: animationPhase == 2,
                        showHighlight: animationPhase >= 2
                    )
                    
                    // Stress demo slider
                    DemoSlider(
                        title: "Current Stress", 
                        value: demoStress,
                        color: .red,
                        isAnimating: animationPhase == 3,
                        showHighlight: animationPhase >= 3
                    )
                    
                    // Session duration demo (static)
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Session Duration")
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundColor(.white)
                        
                        HStack {
                            VStack(spacing: 2) {
                                Spacer()
                                    .frame(height: 8)
                                
                                Text("0 min")
                                    .font(.title3)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.blue)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(
                                        RoundedRectangle(cornerRadius: 8)
                                            .fill(Color.blue.opacity(0.2))
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 8)
                                                    .stroke(Color.blue.opacity(0.4), lineWidth: 1)
                                            )
                                    )
                                
                                Spacer()
                                    .frame(height: 8)
                            }
                            .frame(height: 60)
                            
                            Spacer()
                            
                            HStack(spacing: 8) {
                                Text("Live")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.7))
                                
                                Toggle("", isOn: .constant(false))
                                    .toggleStyle(SwitchToggleStyle(tint: .green))
                                    .labelsHidden()
                                    .disabled(true)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color.white.opacity(0.1))
                    )
                    
                    // Demo save button
                    if showSaveButton {
                        DemoSaveButton(isAnimating: animationPhase == 4, showHighlight: animationPhase >= 4)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                
                Spacer()
                
                // Tutorial content at bottom
                tutorialContentView
                    .padding(.horizontal, 24)
                    .padding(.bottom, 50)
            }
            
            // Save confirmation demo (centered overlay)
            if showSaveConfirmation {
                DemoSaveConfirmation()
                    .transition(.opacity.combined(with: .scale(scale: 0.9)))
            }
        }
        .onAppear {
            startAnimation()
        }
        .onReceive(animationTimer) { _ in
            advanceAnimation()
        }
    }
    
    private func startAnimation() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            withAnimation(.easeInOut(duration: 0.5)) {
                animationPhase = 1
            }
        }
    }
    
    private func advanceAnimation() {
        switch animationPhase {
        case 1:
            // Start tinnitus animation
            withAnimation(.easeInOut(duration: 2.0)) {
                animationPhase = 2
                demoTinnitus = 4
            }
        case 2:
            // Start stress animation
            withAnimation(.easeInOut(duration: 2.0)) {
                animationPhase = 3
                demoStress = 2
            }
        case 3:
            // Show save button and start save animation
            withAnimation(.easeInOut(duration: 0.5)) {
                showSaveButton = true
                animationPhase = 4
            }
        case 4:
            // Show save confirmation
            withAnimation(.easeInOut(duration: 0.3)) {
                showSaveConfirmation = true
                animationPhase = 5
            }
        case 5:
            // Hide confirmation and restart
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                withAnimation(.easeInOut(duration: 0.3)) {
                    showSaveConfirmation = false
                    animationPhase = 0
                    demoTinnitus = 7
                    demoStress = 6
                    showSaveButton = false
                }
                // Restart the animation
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                    startAnimation()
                }
            }
        default:
            break
        }
    }
    
    private var tutorialContentView: some View {
        VStack(spacing: 20) {
            stepIndicator
            
            VStack(spacing: 12) {
                Text(tutorialManager.currentStep.title)
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                    .multilineTextAlignment(.center)
                
                Text("Watch the demonstration below to learn how to track your symptoms")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .lineLimit(nil)
            }
            
            Button(action: {
                tutorialManager.nextStep()
            }) {
                Text(tutorialManager.currentStep.buttonText)
                    .font(.headline)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color.orange)
                    .cornerRadius(12)
            }
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThickMaterial)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                )
        )
    }
    
    private var stepIndicator: some View {
        HStack(spacing: 8) {
            ForEach(TutorialStep.allCases, id: \.rawValue) { step in
                Circle()
                    .fill(step.rawValue <= tutorialManager.currentStep.rawValue ? Color.orange : Color.gray.opacity(0.3))
                    .frame(width: 8, height: 8)
                    .scaleEffect(step == tutorialManager.currentStep ? 1.2 : 1.0)
                    .animation(.easeInOut(duration: 0.2), value: tutorialManager.currentStep)
            }
        }
        .padding(.bottom, 8)
    }
}

struct TabHighlight: View {
    var body: some View {
        Text("Add")
            .font(.system(size: 16, weight: .semibold))
            .foregroundColor(.white)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.orange)
                    .shadow(color: .orange.opacity(0.6), radius: 8, x: 0, y: 0)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color.orange.opacity(0.8), lineWidth: 2)
            )
    }
}

struct DemoSlider: View {
    let title: String
    let value: Int
    let color: Color
    let isAnimating: Bool
    let showHighlight: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            sliderHeader
            sliderContent
        }
        .padding(20)
        .background(sliderBackground)
        .scaleEffect(showHighlight ? 1.05 : 1.0)
        .animation(.easeInOut(duration: 0.3), value: showHighlight)
    }
    
    private var sliderHeader: some View {
        HStack {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.white)
            
            Spacer()
            
            Text("\(value)")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(color)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(color.opacity(0.2))
                )
        }
    }
    
    private var sliderContent: some View {
        VStack(spacing: 8) {
            sliderTrack
            stepMarks
        }
    }
    
    private var sliderTrack: some View {
        GeometryReader { geometry in
            let trackWidth = geometry.size.width - 40
            ZStack(alignment: .leading) {
                // Background track
                RoundedRectangle(cornerRadius: 3)
                    .fill(Color.gray.opacity(0.2))
                    .frame(height: 6)
                
                // Active track
                RoundedRectangle(cornerRadius: 3)
                    .fill(color)
                    .frame(width: CGFloat(value) / 10 * trackWidth, height: 6)
                
                // Slider handle with glow effect
                Circle()
                    .fill(color)
                    .frame(width: 20, height: 20)
                    .shadow(color: color.opacity(0.3), radius: 3, x: 0, y: 2)
                    .shadow(color: isAnimating ? color : .clear, radius: isAnimating ? 12 : 0, x: 0, y: 0)
                    .offset(x: CGFloat(value) / 10 * (trackWidth - 20))
                    .scaleEffect(isAnimating ? 1.3 : 1.0)
                    .animation(.easeInOut(duration: 0.3).repeatForever(autoreverses: true), value: isAnimating)
            }
            .padding(.horizontal, 20)
        }
        .frame(height: 20)
    }
    
    private var stepMarks: some View {
        HStack {
            ForEach(0...10, id: \.self) { step in
                VStack(spacing: 4) {
                    Rectangle()
                        .fill(step == value ? color : Color.gray.opacity(0.4))
                        .frame(width: 2, height: step % 5 == 0 ? 12 : 8)
                    
                    if step % 5 == 0 {
                        Text("\(step)")
                            .font(.caption2)
                            .foregroundColor(step == value ? color : .white.opacity(0.6))
                            .fontWeight(step == value ? .semibold : .regular)
                    }
                }
                
                if step < 10 {
                    Spacer()
                }
            }
        }
        .padding(.horizontal, 20)
    }
    
    private var sliderBackground: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 16)
                .fill(Color.white.opacity(0.1))
            
            if showHighlight {
                RoundedRectangle(cornerRadius: 16)
                    .fill(color.opacity(0.2))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(color, lineWidth: 2)
                    )
            }
        }
    }
}

struct DemoSaveButton: View {
    let isAnimating: Bool
    let showHighlight: Bool
    
    var body: some View {
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
        .scaleEffect(isAnimating ? 1.1 : 1.0)
        .shadow(color: showHighlight ? .orange.opacity(0.6) : .clear, radius: showHighlight ? 8 : 0, x: 0, y: 0)
        .animation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true), value: isAnimating)
    }
}

struct DemoSaveConfirmation: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 32))
                .foregroundColor(.green)
            
            Text("Entry #1 Saved")
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            
            VStack(spacing: 4) {
                HStack(spacing: 16) {
                    HStack(spacing: 4) {
                        Image(systemName: "speaker.wave.2.fill")
                            .font(.caption)
                            .foregroundColor(.orange)
                        Text("Tinnitus: 4/10")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    HStack(spacing: 4) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.caption)
                            .foregroundColor(.red)
                        Text("Stress: 2/10")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
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
    MainTabView()
}