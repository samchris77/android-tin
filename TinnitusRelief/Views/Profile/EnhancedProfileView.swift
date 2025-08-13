import SwiftUI
import UserNotifications

struct EnhancedProfileView: View {
    @State private var showingSettings = false
    @State private var showingPrivacy = false
    @State private var dailyReminderEnabled = false
    @State private var reminderTime = Date()
    @State private var showingTimePicker = false
    @State private var showingPrivacyView = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    profileHeader
                    
                    settingsSection
                    
                    progressSection
                    
                    aboutSection
                    
                    privacySection
                }
                .padding()
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
            .sheet(isPresented: $showingPrivacyView) {
                PrivacyView()
            }
        }
    }
    
    private var profileHeader: some View {
        VStack(spacing: 16) {
            Image(systemName: "person.circle.fill")
                .font(.system(size: 80))
                .foregroundColor(.orange)
            
            VStack(spacing: 4) {
                Text("Welcome to TinnitusRelief")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Text("Your personalized tinnitus management companion")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 4, x: 0, y: 2)
        )
    }
    
    private var progressSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Your Progress")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 12) {
                ProfileProgressCard(
                    title: "Days Tracked",
                    value: "7",
                    subtitle: "Keep up the good work!",
                    color: .green
                )
                
                ProfileProgressCard(
                    title: "Average Severity",
                    value: "4.2",
                    subtitle: "Down from last week",
                    color: .orange
                )
                
                ProfileProgressCard(
                    title: "Therapy Sessions",
                    value: "12",
                    subtitle: "This month",
                    color: .blue
                )
            }
        }
    }
    
    private var settingsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Button(action: {
                showingSettings.toggle()
            }) {
                HStack {
                    Image(systemName: "gearshape.fill")
                        .font(.title2)
                        .foregroundColor(.orange)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Daily Reminders")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        Text("Set up notifications to track your progress")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    Image(systemName: showingSettings ? "chevron.up" : "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(.ultraThinMaterial)
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            if showingSettings {
                VStack(spacing: 0) {
                    DailyReminderRow(
                        isEnabled: $dailyReminderEnabled,
                        reminderTime: $reminderTime,
                        showingTimePicker: $showingTimePicker
                    )
                }
                .padding(.vertical, 8)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(.ultraThinMaterial)
                )
                .transition(.opacity.combined(with: .scale(scale: 0.95)))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showingSettings)
    }
    
    private var privacySection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Button(action: {
                showingPrivacy.toggle()
            }) {
                HStack {
                    Image(systemName: "lock.fill")
                        .font(.title2)
                        .foregroundColor(.blue)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Privacy & Data")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        Text("Your data stays secure on your device")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    Image(systemName: showingPrivacy ? "chevron.up" : "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(.ultraThinMaterial)
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            if showingPrivacy {
                VStack(spacing: 0) {
                    Group {
                        ProfileSettingsRow(
                            title: "Export Data",
                            subtitle: "Save your progress for healthcare providers",
                            icon: "square.and.arrow.up",
                            action: {}
                        )
                        
                        Divider()
                            .padding(.horizontal)
                        
                        ProfileSettingsRow(
                            title: "Data Storage",
                            subtitle: "All data stored locally on your device",
                            icon: "externaldrive",
                            action: {}
                        )
                        
                        Divider()
                            .padding(.horizontal)
                        
                        ProfileSettingsRow(
                            title: "Privacy Policy",
                            subtitle: "Learn how we protect your information",
                            icon: "doc.text",
                            action: {
                                showingPrivacyView = true
                            }
                        )
                    }
                }
                .padding(.vertical, 8)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(.ultraThinMaterial)
                )
                .transition(.opacity.combined(with: .scale(scale: 0.95)))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showingPrivacy)
    }
    
    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("About TinnitusRelief")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 12) {
                ProfileSettingsRow(
                    title: "Help & FAQ",
                    subtitle: "Get answers to common questions",
                    icon: "questionmark.circle",
                    action: {}
                )
                
                ProfileSettingsRow(
                    title: "Contact Support",
                    subtitle: "Get help from our team",
                    icon: "envelope",
                    action: {}
                )
                
                ProfileSettingsRow(
                    title: "Rate the App",
                    subtitle: "Share your experience",
                    icon: "star",
                    action: {}
                )
            }
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
            )
            
            VStack(alignment: .leading, spacing: 12) {
                Text("Medical Disclaimer")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Text("TinnitusRelief is designed to help you manage tinnitus symptoms through personalized frequency matching, progress tracking, and evidence-based techniques.")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.leading)
                
                Text("This app is not a substitute for professional medical advice. Please consult with a healthcare provider for proper diagnosis and treatment.")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.top, 8)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.orange.opacity(0.1))
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.orange.opacity(0.3), lineWidth: 1)
                            )
                    )
            }
        }
    }
}

// Missing UI Components
struct ProfileProgressCard: View {
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

struct ProfileSettingsRow: View {
    let title: String
    let subtitle: String
    let icon: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(.orange)
                    .frame(width: 24)
                
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
                    .font(.caption2)
                    .foregroundColor(.secondary.opacity(0.6))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct DailyReminderRow: View {
    @Binding var isEnabled: Bool
    @Binding var reminderTime: Date
    @Binding var showingTimePicker: Bool
    
    var body: some View {
        VStack(spacing: 0) {
            Button(action: {
                showingTimePicker.toggle()
            }) {
                HStack(spacing: 16) {
                    Image(systemName: "bell")
                        .font(.system(size: 18))
                        .foregroundColor(.orange)
                        .frame(width: 24)
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Daily Reminders")
                            .font(.body)
                            .foregroundColor(.primary)
                            .multilineTextAlignment(.leading)
                        
                        Text(isEnabled ? "Enabled at \(reminderTime, formatter: timeFormatter)" : "Track your tinnitus daily")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.leading)
                    }
                    
                    Spacer()
                    
                    Toggle("", isOn: $isEnabled)
                        .labelsHidden()
                        .toggleStyle(SwitchToggleStyle(tint: .orange))
                        .onChange(of: isEnabled) { enabled in
                            if enabled {
                                requestNotificationPermission()
                                scheduleNotification()
                            } else {
                                cancelNotifications()
                            }
                        }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .buttonStyle(PlainButtonStyle())
            
            if showingTimePicker {
                VStack(spacing: 16) {
                    Divider()
                        .padding(.horizontal)
                    
                    DatePicker("Reminder Time", selection: $reminderTime, displayedComponents: .hourAndMinute)
                        .datePickerStyle(.wheel)
                        .labelsHidden()
                        .onChange(of: reminderTime) { _ in
                            if isEnabled {
                                scheduleNotification()
                            }
                        }
                        .padding(.horizontal)
                }
                .transition(.opacity.combined(with: .scale(scale: 0.95)))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: showingTimePicker)
        .onAppear {
            loadReminderSettings()
        }
    }
    
    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            if !granted {
                DispatchQueue.main.async {
                    isEnabled = false
                }
            }
        }
    }
    
    private func scheduleNotification() {
        cancelNotifications()
        
        let content = UNMutableNotificationContent()
        content.title = "Tinnitus Tracking Reminder"
        content.body = "Time to track your tinnitus symptoms for today"
        content.sound = .default
        
        let calendar = Calendar.current
        let components = calendar.dateComponents([.hour, .minute], from: reminderTime)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
        
        let request = UNNotificationRequest(identifier: "dailyTinnitusReminder", content: content, trigger: trigger)
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Failed to schedule notification: \(error)")
            }
        }
        
        saveReminderSettings()
    }
    
    private func cancelNotifications() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["dailyTinnitusReminder"])
    }
    
    private func saveReminderSettings() {
        UserDefaults.standard.set(isEnabled, forKey: "dailyReminderEnabled")
        UserDefaults.standard.set(reminderTime, forKey: "reminderTime")
    }
    
    private func loadReminderSettings() {
        isEnabled = UserDefaults.standard.bool(forKey: "dailyReminderEnabled")
        if let savedTime = UserDefaults.standard.object(forKey: "reminderTime") as? Date {
            reminderTime = savedTime
        }
    }
}

private let timeFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.timeStyle = .short
    return formatter
}()

#Preview {
    EnhancedProfileView()
}