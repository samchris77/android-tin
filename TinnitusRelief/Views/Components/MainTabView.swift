import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0
    
    var body: some View {
        VStack(spacing: 0) {
            // Frequency Controller positioned at top (hidden on frequency match page)
            if selectedTab != 1 {
                FrequencyController()
                    .zIndex(1)
            }
            
            TabView(selection: $selectedTab) {
            HomeView(selectedTab: $selectedTab)
                .tabItem {
                    Image(systemName: selectedTab == 0 ? "house.fill" : "house")
                    Text("Home")
                }
                .tag(0)
            
            FrequencyMatchingView(selectedTab: $selectedTab)
                .tabItem {
                    Image(systemName: selectedTab == 1 ? "waveform.path" : "waveform.path")
                    Text("Frequency")
                }
                .tag(1)
            
            DiaryView()
                .tabItem {
                    Image(systemName: selectedTab == 2 ? "book.fill" : "book")
                    Text("Diary")
                }
                .tag(2)
            
            ProfileView()
                .tabItem {
                    Image(systemName: selectedTab == 3 ? "person.fill" : "person")
                    Text("Profile")
                }
                .tag(3)
            
            SettingsView()
                .tabItem {
                    Image(systemName: selectedTab == 4 ? "gearshape.fill" : "gearshape")
                    Text("Settings")
                }
                .tag(4)
            }
            .accentColor(.orange)
        }
    }
}

struct HomeView: View {
    @Binding var selectedTab: Int
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    welcomeSection
                    
                    quickActionsSection
                    
                    recentActivitySection
                    
                    tipOfTheDaySection
                }
                .padding()
            }
            .navigationTitle("TinnitusRelief")
            .navigationBarTitleDisplayMode(.large)
        }
    }
    
    private var welcomeSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Good \(timeOfDay)")
                        .font(.title2)
                        .fontWeight(.medium)
                        .foregroundColor(.secondary)
                    
                    Text("How are you feeling today?")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                }
                
                Spacer()
                
                Image(systemName: "sun.max.fill")
                    .font(.system(size: 32))
                    .foregroundColor(.orange)
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(.ultraThinMaterial)
                .shadow(radius: 4, x: 0, y: 2)
        )
    }
    
    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Quick Actions")
                .font(.headline)
                .foregroundColor(.primary)
            
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 2), spacing: 12) {
                QuickActionButton(
                    title: "Frequency Match",
                    subtitle: "Find your tinnitus frequency",
                    icon: "waveform.path",
                    color: .orange,
                    action: { selectedTab = 1 }
                )
                
                QuickActionCard(
                    title: "Progress Tracking",
                    subtitle: "View your tinnitus trends",
                    icon: "chart.line.uptrend.xyaxis",
                    color: .blue,
                    destination: AnyView(DiaryView())
                )
                
                QuickActionCard(
                    title: "Log Entry",
                    subtitle: "Track your daily progress",
                    icon: "plus.circle",
                    color: .green,
                    destination: AnyView(DiaryView())
                )
                
                QuickActionCard(
                    title: "Sleep Timer",
                    subtitle: "Gentle sound fade-out",
                    icon: "moon",
                    color: .purple,
                    destination: AnyView(SleepTimerView())
                )
            }
        }
    }
    
    private var recentActivitySection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Recent Activity")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 12) {
                ActivityRowView(
                    title: "Frequency Matching",
                    subtitle: "Matched at 8.2 kHz",
                    time: "2 hours ago",
                    icon: "waveform.path",
                    color: .orange
                )
                
                ActivityRowView(
                    title: "Progress Review",
                    subtitle: "Weekly trends analyzed",
                    time: "Yesterday",
                    icon: "chart.line.uptrend.xyaxis",
                    color: .blue
                )
                
                ActivityRowView(
                    title: "Diary Entry",
                    subtitle: "Loudness: 4, Stress: 3",
                    time: "2 days ago",
                    icon: "book",
                    color: .green
                )
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
            )
        }
    }
    
    private var tipOfTheDaySection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "lightbulb.fill")
                    .foregroundColor(.yellow)
                
                Text("Tip of the Day")
                    .font(.headline)
                    .foregroundColor(.primary)
            }
            
            Text("Try to maintain a consistent sleep schedule. Good sleep hygiene can significantly reduce tinnitus symptoms and improve your overall well-being.")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.leading)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
                .shadow(radius: 2, x: 0, y: 1)
        )
    }
    
    private var timeOfDay: String {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 0..<12:
            return "Morning"
        case 12..<17:
            return "Afternoon"
        default:
            return "Evening"
        }
    }
}

struct QuickActionCard: View {
    let title: String
    let subtitle: String
    let icon: String
    let color: Color
    let destination: AnyView
    
    var body: some View {
        NavigationLink(destination: destination) {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Image(systemName: icon)
                        .font(.system(size: 24, weight: .medium))
                        .foregroundColor(color)
                    
                    Spacer()
                    
                    Image(systemName: "arrow.right")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.secondary)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.headline)
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.leading)
                    
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.leading)
                }
                
                Spacer()
            }
            .padding()
            .frame(height: 100)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
                    .shadow(radius: 2, x: 0, y: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct QuickActionButton: View {
    let title: String
    let subtitle: String
    let icon: String
    let color: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Image(systemName: icon)
                        .font(.system(size: 24, weight: .medium))
                        .foregroundColor(color)
                    
                    Spacer()
                    
                    Image(systemName: "arrow.right")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(.secondary)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.headline)
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.leading)
                    
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.leading)
                }
                
                Spacer()
            }
            .padding()
            .frame(height: 100)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
                    .shadow(radius: 2, x: 0, y: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct ActivityRowView: View {
    let title: String
    let subtitle: String
    let time: String
    let icon: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(color)
                .frame(width: 32, height: 32)
                .background(
                    Circle()
                        .fill(color.opacity(0.1))
                )
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(.primary)
                
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Text(time)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

struct ProfileView: View {
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    profileHeader
                    
                    progressSection
                    
                    preferencesSection
                    
                    aboutSection
                }
                .padding()
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
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
    
    private var preferencesSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Preferences")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 0) {
                PreferenceRowView(
                    title: "Tinnitus Frequency",
                    value: "8.2 kHz",
                    action: {}
                )
                
                Divider()
                    .padding(.horizontal)
                
                PreferenceRowView(
                    title: "Preferred Volume",
                    value: "30%",
                    action: {}
                )
                
                Divider()
                    .padding(.horizontal)
                
                PreferenceRowView(
                    title: "Session Duration",
                    value: "45 minutes",
                    action: {}
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

struct SettingsView: View {
    var body: some View {
        NavigationView {
            List {
                Section("Audio") {
                    SettingsRowView(
                        title: "Background Audio",
                        subtitle: "Continue playing when app is minimized",
                        icon: "speaker.wave.3",
                        action: {}
                    )
                    
                    SettingsRowView(
                        title: "Volume Limit",
                        subtitle: "Safe hearing protection",
                        icon: "volume.3",
                        action: {}
                    )
                }
                
                Section("Notifications") {
                    SettingsRowView(
                        title: "Daily Reminders",
                        subtitle: "Track your tinnitus daily",
                        icon: "bell",
                        action: {}
                    )
                    
                    SettingsRowView(
                        title: "Frequency Sessions",
                        subtitle: "Schedule frequency matching",
                        icon: "clock",
                        action: {}
                    )
                }
                
                Section("Data") {
                    SettingsRowView(
                        title: "Export Data",
                        subtitle: "Save your progress",
                        icon: "square.and.arrow.up",
                        action: {}
                    )
                    
                    SettingsRowView(
                        title: "Privacy",
                        subtitle: "Your data stays on your device",
                        icon: "lock",
                        action: {}
                    )
                }
                
                Section("Support") {
                    SettingsRowView(
                        title: "Help & FAQ",
                        subtitle: "Get answers to common questions",
                        icon: "questionmark.circle",
                        action: {}
                    )
                    
                    SettingsRowView(
                        title: "Contact Support",
                        subtitle: "Get help from our team",
                        icon: "envelope",
                        action: {}
                    )
                }
            }
            .navigationTitle("Settings")
        }
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

struct SleepTimerView: View {
    @State private var selectedDuration: TimeInterval = 1800
    @State private var isTimerActive = false
    @State private var customMinutes: String = ""
    @State private var isUsingCustomDuration = false
    
    let durations: [TimeInterval] = [900, 1800, 2700, 3600]
    
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                Spacer()
                
                VStack(spacing: 16) {
                    Image(systemName: "moon.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.purple)
                    
                    Text("Sleep Timer")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                    
                    Text("Gradually fade out sounds for better sleep")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                
                VStack(spacing: 16) {
                    Text("Duration")
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 2), spacing: 12) {
                        ForEach(durations, id: \.self) { duration in
                            Button(action: {
                                selectedDuration = duration
                                isUsingCustomDuration = false
                            }) {
                                VStack(spacing: 8) {
                                    Text(formatDuration(duration))
                                        .font(.title2)
                                        .fontWeight(.semibold)
                                    
                                    Text("minutes")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                .frame(height: 80)
                                .frame(maxWidth: .infinity)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill((selectedDuration == duration && !isUsingCustomDuration) ? Color.purple.opacity(0.2) : Color.gray.opacity(0.1))
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke((selectedDuration == duration && !isUsingCustomDuration) ? Color.purple : Color.clear, lineWidth: 2)
                                        )
                                )
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    
                    VStack(spacing: 12) {
                        Text("Or set custom duration")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        
                        HStack(spacing: 12) {
                            TextField("Enter minutes", text: $customMinutes)
                                .keyboardType(.numberPad)
                                .textFieldStyle(.roundedBorder)
                                .frame(maxWidth: 120)
                                .onChange(of: customMinutes) { _ in
                                    updateCustomDuration()
                                }
                            
                            Text("minutes")
                                .font(.body)
                                .foregroundColor(.secondary)
                            
                            Spacer()
                        }
                        .padding()
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(isUsingCustomDuration ? Color.purple.opacity(0.2) : Color.gray.opacity(0.1))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(isUsingCustomDuration ? Color.purple : Color.clear, lineWidth: 2)
                                )
                        )
                    }
                }
                
                Spacer()
                
                Button(action: {
                    isTimerActive.toggle()
                }) {
                    Text(isTimerActive ? "Stop Timer" : "Start Timer")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(
                            LinearGradient(
                                colors: isTimerActive ? [Color.red, Color.orange] : [Color.purple, Color.blue],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .cornerRadius(12)
                        .shadow(radius: 4, x: 0, y: 2)
                }
                .padding(.bottom)
            }
            .padding()
            .navigationTitle("Sleep Timer")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
    
    private func updateCustomDuration() {
        guard let minutes = Int(customMinutes), minutes > 0, minutes <= 120 else {
            isUsingCustomDuration = false
            return
        }
        
        selectedDuration = TimeInterval(minutes * 60)
        isUsingCustomDuration = true
    }
    
    private func formatDuration(_ duration: TimeInterval) -> String {
        let minutes = Int(duration / 60)
        return "\(minutes)"
    }
}

#Preview {
    MainTabView()
}