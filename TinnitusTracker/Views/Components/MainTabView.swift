import SwiftUI
import UserNotifications
import CoreData
import Foundation

// MARK: - Localization Manager

class LocalizationManager: ObservableObject {
    static let shared = LocalizationManager()
    
    @Published var currentLanguage: String {
        didSet {
            UserDefaults.standard.set(currentLanguage, forKey: "selectedLanguage")
            UserDefaults.standard.set([currentLanguage], forKey: "AppleLanguages")
            UserDefaults.standard.synchronize()
        }
    }
    
    private init() {
        self.currentLanguage = UserDefaults.standard.string(forKey: "selectedLanguage") ?? "en"
        // Set the app language on init
        UserDefaults.standard.set([currentLanguage], forKey: "AppleLanguages")
        UserDefaults.standard.synchronize()
    }
    
    func setLanguage(_ language: String) {
        currentLanguage = language
    }
    
    var displayLanguage: String {
        switch currentLanguage {
        case "ko":
            return localizedString(for: "profile.language.korean")
        default:
            return localizedString(for: "profile.language.english")
        }
    }
    
    private func localizedString(for key: String) -> String {
        // Get the localized string for the current language
        if let path = Bundle.main.path(forResource: currentLanguage, ofType: "lproj"),
           let bundle = Bundle(path: path) {
            return bundle.localizedString(forKey: key, value: nil, table: nil)
        }
        return NSLocalizedString(key, comment: "")
    }
}

// Custom Text extension for localization
extension Text {
    init(localized key: String) {
        let manager = LocalizationManager.shared
        if let path = Bundle.main.path(forResource: manager.currentLanguage, ofType: "lproj"),
           let bundle = Bundle(path: path) {
            self.init(bundle.localizedString(forKey: key, value: nil, table: nil))
        } else {
            self.init(NSLocalizedString(key, comment: ""))
        }
    }
}

// MARK: - Tutorial System

enum TutorialStep: Int, CaseIterable {
    case welcome = 0
    case frequencyDemo
    case logDemo
    case progressDemo
    case historyDemo
    case profileDemo
    
    var title: String {
        switch self {
        case .welcome:
            return "Welcome to Tinnitus Tracker"
        case .frequencyDemo:
            return "Match Your Tinnitus Frequency"
        case .logDemo:
            return "Track Your Progress"
        case .progressDemo:
            return "View Your Progress"
        case .historyDemo:
            return "Review Your History"
        case .profileDemo:
            return "Setup Reminders"
        }
    }
    
    var description: String {
        switch self {
        case .welcome:
            return "This app helps reduce your tinnitus through sound therapy. Set volume to 70% of your tinnitus loudness and listen for about 2 hours daily for best results."
        case .frequencyDemo:
            return "Match your tinnitus frequency and volume levels to reduce tinnitus symptoms."
        case .logDemo:
            return "Add entries, tinnitus loudness and stress levels, and therapy duration to track improvement over time."
        case .progressDemo:
            return "View your therapy progress with weekly summaries, session calendar, and comprehensive statistics to track your improvement."
        case .historyDemo:
            return "Review all your individual session entries to see your detailed therapy history and symptom patterns over time."
        case .profileDemo:
            return "Set up daily reminders to help you maintain a consistent therapy routine for maximum effectiveness."
        }
    }
    
    var buttonText: String {
        switch self {
        case .welcome, .frequencyDemo, .logDemo, .progressDemo, .historyDemo:
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
        case .logDemo, .progressDemo, .historyDemo:
            return 1 // Log tab (with different internal tabs)
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
    private var onDiaryTabChange: ((Int) -> Void)?
    
    init(persistenceController: PersistenceController) {
        self.persistenceController = persistenceController
        checkOnboardingStatus()
    }
    
    func setTabChangeCallback(_ callback: @escaping (Int) -> Void) {
        self.onTabChange = callback
    }
    
    func setDiaryTabChangeCallback(_ callback: @escaping (Int) -> Void) {
        self.onDiaryTabChange = callback
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
                
                // Handle DiaryView internal tab navigation with delay to ensure DiaryView is loaded
                switch nextStep {
                case .logDemo, .progressDemo, .historyDemo:
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                        switch nextStep {
                        case .logDemo:
                            self.onDiaryTabChange?(0) // Add tab
                        case .progressDemo:
                            self.onDiaryTabChange?(1) // Progress Tracking tab
                        case .historyDemo:
                            self.onDiaryTabChange?(2) // History tab
                        default:
                            break
                        }
                    }
                default:
                    break
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
                    
                    DiaryView(tutorialManager: tutorialManager)
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
            TransparentTutorialOverlayView(tutorialManager: tutorialManager)
                .transition(.opacity)
        case .progressDemo:
            ProgressDemoOverlayView(tutorialManager: tutorialManager)
                .transition(.opacity)
        case .historyDemo:
            HistoryDemoOverlayView(tutorialManager: tutorialManager)
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
    @State private var showingPrivacyInfo = false
    @StateObject private var localizationManager = LocalizationManager.shared
    @State private var showingLanguageSelection = false
    let tutorialManager: TutorialManager
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    settingsSection
                    
                    languageSection
                    
                    tutorialSection
                    
                    aboutSection
                    
                    privacySection
                }
                .padding()
            }
            .navigationTitle(Text(localized: "profile.title"))
            .navigationBarTitleDisplayMode(.large)
            .id(localizationManager.currentLanguage)
            .sheet(isPresented: $showingReminderEdit) {
                ReminderEditView(reminderManager: reminderManager, existingReminder: editingReminder)
            }
            .sheet(isPresented: $showingPrivacyInfo) {
                PrivacyInfoView()
            }
        }
    }
    
    private func openPrivacyPolicy() {
        showingPrivacyInfo = true
    }
    
    
    
    private var settingsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(localized: "profile.reminders.title")
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
    
    private var languageSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(localized: "profile.language.title")
                .font(.headline)
                .foregroundColor(.primary)
            
            Button(action: {
                showingLanguageSelection = true
            }) {
                HStack(spacing: 12) {
                    Image(systemName: "globe")
                        .font(.system(size: 20))
                        .foregroundColor(.blue)
                        .frame(width: 32)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(localized: "profile.language.button.title")
                            .font(.body)
                            .foregroundColor(.primary)
                            .multilineTextAlignment(.leading)
                        
                        Text(localizationManager.displayLanguage)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
            )
            .actionSheet(isPresented: $showingLanguageSelection) {
                ActionSheet(
                    title: Text(localized: "profile.language.selection.title"),
                    buttons: [
                        .default(Text(localized: "profile.language.english")) {
                            localizationManager.setLanguage("en")
                        },
                        .default(Text(localized: "profile.language.korean")) {
                            localizationManager.setLanguage("ko")
                        },
                        .cancel(Text(localized: "common.cancel"))
                    ]
                )
            }
        }
    }
    
    private var tutorialSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(localized: "profile.tutorial.title")
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
                            Text(localized: "profile.tutorial.restart.title")
                                .font(.body)
                                .foregroundColor(.primary)
                                .multilineTextAlignment(.leading)
                            
                            Text(localized: "profile.tutorial.restart.description")
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
            Text(localized: "profile.about.title")
                .font(.headline)
                .foregroundColor(.primary)
            
            Text(localized: "profile.about.description")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.leading)
            
            Text(localized: "profile.about.disclaimer")
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
            Text(localized: "profile.privacy.title")
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
                        Text(localized: "profile.privacy.policy.title")
                            .font(.body)
                            .foregroundColor(.primary)
                            .multilineTextAlignment(.leading)
                        
                        Text(localized: "profile.privacy.policy.button.description")
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
    
    @State private var showingActionSheet = false
    @State private var showingDeleteAlert = false
    
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
                
                Button(action: onToggle) {
                    Toggle("", isOn: .constant(reminder.isEnabled))
                        .labelsHidden()
                        .toggleStyle(SwitchToggleStyle(tint: .orange))
                        .allowsHitTesting(false)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color.clear)
            .contentShape(Rectangle()) // Ensure entire area is tappable
            .onTapGesture {
                showingActionSheet = true
            }
            .confirmationDialog("Reminder Options", isPresented: $showingActionSheet, titleVisibility: .visible) {
                Button("Edit") {
                    onEdit()
                }
                Button("Delete", role: .destructive) {
                    showingDeleteAlert = true
                }
                Button("Cancel", role: .cancel) { }
            }
            .alert("Delete Reminder", isPresented: $showingDeleteAlert) {
                Button("Cancel", role: .cancel) { }
                Button("Delete", role: .destructive) {
                    onDelete()
                }
            } message: {
                Text("Are you sure you want to delete this reminder? This action cannot be undone.")
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


struct FrequencyDemoOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    let controlPosition: CGPoint
    
    var body: some View {
        ZStack {
            // Very light background to show frequency view
            Color.black.opacity(0.3)
                .ignoresSafeArea()
            
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


struct TransparentTutorialOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    
    var body: some View {
        ZStack {
            // Very light background to show underlying content
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .allowsHitTesting(false)
            
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
                        
                        Text("Tinnitus Tracker")
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
                    
                    TutorialFeatureRow(
                        icon: "calendar",
                        title: "track your progress",
                        description: "Your data, your journey"
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

struct ProgressDemoOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    
    var body: some View {
        ZStack {
            // Light background to show underlying content
            Color.black.opacity(0.4)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                Spacer()
                
                // Tutorial content at bottom
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

struct HistoryDemoOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    
    var body: some View {
        ZStack {
            // Light background to show underlying content
            Color.black.opacity(0.4)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                Spacer()
                
                // Tutorial content at bottom
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

private struct PrivacyInfoView: View {
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    PrivacyHeaderView()
                    
                    // Main Privacy Points
                    PrivacySectionView(
                        icon: "xmark.shield.fill",
                        iconColor: .red,
                        title: "No Personal Data Collection",
                        content: "TinnitusTracker operates with a strict no-data-collection policy. We do not require user accounts, emails, or any personal identifiers. Your use of the app is completely anonymous."
                    )
                    
                    PrivacySectionView(
                        icon: "iphone.homebutton.badge.play",
                        iconColor: .blue,
                        title: "Local Storage Only",
                        content: "All diary entries, frequency settings, and preferences are stored exclusively on your device using Apple's secure Core Data framework. This data is protected by your device's security (e.g., Face ID, Passcode) and is permanently deleted if you uninstall the app."
                    )
                    
                    PrivacySectionView(
                        icon: "network.slash",
                        iconColor: .green,
                        title: "No Third-Party Services",
                        content: "This app does not include any third-party analytics, advertising networks, or crash reporting services. It functions entirely offline, and no data ever leaves your device."
                    )
                    
                    PrivacySectionView(
                        icon: "waveform.path.ecg",
                        iconColor: .purple,
                        title: "Audio Permissions",
                        content: "Audio session permissions are requested solely to generate therapeutic sounds. The app only outputs audio; it never records and does not use the microphone."
                    )
                    
                    PrivacySectionView(
                        icon: "hand.raised.fill",
                        iconColor: .orange,
                        title: "You Are in Control",
                        content: "You have full control over your data. You can export your diary for personal use or delete individual entries at any time through the app's settings."
                    )
                    
                    // Final Assurance
                    VStack(alignment: .center, spacing: 10) {
                        Text("Complete Privacy Guarantee")
                            .font(.headline)
                            .fontWeight(.bold)
                        Text("TinnitusTracker is designed with privacy-by-design principles. We cannot access your data because we never collect it. Your health information remains exclusively yours.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.ultraThinMaterial)
                    )
                    
                }
                .padding()
            }
            .navigationTitle("Privacy Policy")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
    }
}

private struct PrivacyHeaderView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Your Privacy is Our Priority")
                .font(.title)
                .fontWeight(.bold)
            
            Text("Last Updated: August 15, 2025")
                .font(.caption)
                .foregroundColor(.secondary)
            
            Text("TinnitusTracker is built to be a private, secure, and offline-first application. We believe your health data is your own.")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }
}

private struct PrivacySectionView: View {
    let icon: String
    let iconColor: Color
    let title: String
    let content: String

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(iconColor)
                .frame(width: 30, alignment: .center)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Text(content)
                    .font(.body)
                    .foregroundColor(.secondary)
            }
        }
    }
}

#Preview {
    MainTabView()
}
