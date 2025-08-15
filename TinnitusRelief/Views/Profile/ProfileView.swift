import SwiftUI

struct ProfileView: View {
    @State private var showingPreferences = false
    @State private var showingPrivacyData = false
    @State private var hapticFeedbackEnabled = true
    @State private var backgroundAudioEnabled = false
    @State private var showingPrivacyInfo = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 16) {
                        Image(systemName: "person.circle.fill")
                            .font(.system(size: 64))
                            .foregroundColor(.orange)
                        
                        VStack(spacing: 4) {
                            Text("TinnitusRelief")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                            
                            Text("Your therapeutic companion")
                                .font(.body)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.top, 20)
                    
                    // Preferences section
                    ExpandableSection(
                        title: "Preferences",
                        icon: "gearshape.fill",
                        iconColor: .blue,
                        isExpanded: $showingPreferences
                    ) {
                        VStack(spacing: 0) {
                            PreferenceToggleRow(
                                title: "Haptic Feedback",
                                subtitle: "Feel vibrations during interactions",
                                icon: "hand.tap",
                                isOn: $hapticFeedbackEnabled
                            )
                            
                            Divider()
                                .padding(.leading, 50)
                            
                            PreferenceToggleRow(
                                title: "Background Audio",
                                subtitle: "Continue playing when app is minimized",
                                icon: "speaker.wave.2",
                                isOn: $backgroundAudioEnabled
                            )
                            
                            Divider()
                                .padding(.leading, 50)
                            
                            PreferenceActionRow(
                                title: "Audio Quality",
                                subtitle: "High quality (44.1kHz)",
                                icon: "waveform",
                                action: {}
                            )
                            
                            Divider()
                                .padding(.leading, 50)
                            
                            PreferenceActionRow(
                                title: "Default Frequency",
                                subtitle: "\(Int(UnifiedAudioEngineManager.shared.currentFrequency)) Hz",
                                icon: "dial.max",
                                action: {}
                            )
                        }
                    }
                    
                    // Privacy & Data section
                    ExpandableSection(
                        title: "Privacy & Data",
                        icon: "lock.shield.fill",
                        iconColor: .green,
                        isExpanded: $showingPrivacyData
                    ) {
                        VStack(spacing: 0) {
                            PreferenceActionRow(
                                title: "Data Storage",
                                subtitle: "All data stored locally on your device",
                                icon: "externaldrive.fill",
                                action: {}
                            )
                            
                            Divider()
                                .padding(.leading, 50)
                            
                            PreferenceActionRow(
                                title: "Privacy Policy",
                                subtitle: "Learn how we protect your information",
                                icon: "doc.text",
                                action: {
                                    showingPrivacyInfo = true
                                }
                            )
                        }
                    }
                    
                    // About section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("About")
                            .font(.headline)
                            .foregroundColor(.primary)
                        
                        VStack(spacing: 0) {
                            PreferenceActionRow(
                                title: "Help & FAQ",
                                subtitle: "Get answers to common questions",
                                icon: "questionmark.circle",
                                action: {}
                            )
                            
                            Divider()
                                .padding(.leading, 50)
                            
                            PreferenceActionRow(
                                title: "Contact Support",
                                subtitle: "Get help from our team",
                                icon: "envelope",
                                action: {}
                            )
                            
                            Divider()
                                .padding(.leading, 50)
                            
                            PreferenceActionRow(
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
                    }
                    
                    // Medical disclaimer
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Medical Disclaimer")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                            .foregroundColor(.primary)
                        
                        Text("TinnitusRelief is designed to help manage tinnitus symptoms through personalized frequency therapy and progress tracking. This app is not a substitute for professional medical advice. Please consult with a healthcare provider for proper diagnosis and treatment.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .padding(12)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(.orange.opacity(0.1))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 8)
                                            .stroke(.orange.opacity(0.3), lineWidth: 1)
                                    )
                            )
                    }
                    
                    Spacer(minLength: 20)
                }
                .padding(.horizontal, 20)
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
        }
        .sheet(isPresented: $showingPrivacyInfo) {
            PrivacyInfoView()
        }
    }
}

struct ExpandableSection<Content: View>: View {
    let title: String
    let icon: String
    let iconColor: Color
    @Binding var isExpanded: Bool
    @ViewBuilder let content: Content
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: {
                withAnimation(.easeInOut(duration: 0.3)) {
                    isExpanded.toggle()
                }
                HapticFeedback.light.trigger()
            }) {
                HStack {
                    Image(systemName: icon)
                        .font(.title2)
                        .foregroundColor(iconColor)
                        .frame(width: 30)
                    
                    Text(title)
                        .font(.headline)
                        .foregroundColor(.primary)
                    
                    Spacer()
                    
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .rotationEffect(.degrees(isExpanded ? 90 : 0))
                        .animation(.easeInOut(duration: 0.3), value: isExpanded)
                }
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(.ultraThinMaterial)
                )
            }
            .buttonStyle(PlainButtonStyle())
            
            if isExpanded {
                content
                    .padding(.top, 8)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.ultraThinMaterial)
                    )
                    .transition(.opacity.combined(with: .scale(scale: 0.95)))
            }
        }
    }
}

struct PreferenceActionRow: View {
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

struct PreferenceToggleRow: View {
    let title: String
    let subtitle: String
    let icon: String
    @Binding var isOn: Bool
    
    var body: some View {
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
            
            Toggle("", isOn: $isOn)
                .labelsHidden()
                .toggleStyle(SwitchToggleStyle(tint: .orange))
                .onChange(of: isOn) { _ in
                    HapticFeedback.light.trigger()
                }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}

#Preview {
    ProfileView()
}