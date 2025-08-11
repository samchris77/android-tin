import SwiftUI

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
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    progressSection
                    
                    settingsSection
                    
                    aboutSection
                }
                .padding()
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
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
            Text("Settings")
                .font(.headline)
                .foregroundColor(.primary)
            
            VStack(spacing: 0) {
                SettingsRowView(
                    title: "Background Audio",
                    subtitle: "Continue playing when app is minimized",
                    icon: "speaker.wave.3",
                    action: {}
                )
                
                Divider()
                    .padding(.horizontal)
                
                SettingsRowView(
                    title: "Daily Reminders",
                    subtitle: "Track your tinnitus daily",
                    icon: "bell",
                    action: {}
                )
                
                Divider()
                    .padding(.horizontal)
                
                SettingsRowView(
                    title: "Export Data",
                    subtitle: "Save your progress",
                    icon: "square.and.arrow.up",
                    action: {}
                )
                
                Divider()
                    .padding(.horizontal)
                
                SettingsRowView(
                    title: "Privacy",
                    subtitle: "Your data stays on your device",
                    icon: "lock",
                    action: {}
                )
                
                Divider()
                    .padding(.horizontal)
                
                SettingsRowView(
                    title: "Help & FAQ",
                    subtitle: "Get answers to common questions",
                    icon: "questionmark.circle",
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


#Preview {
    MainTabView()
}