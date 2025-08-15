// PrivacyInfoView.swift
import SwiftUI

struct PrivacyInfoView: View {
    @Environment(\.presentationMode) var presentationMode

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    HeaderView()
                    
                    // Main Privacy Points
                    PrivacySection(
                        icon: "xmark.shield.fill",
                        iconColor: .red,
                        title: "No Personal Data Collection",
                        content: "TinnitusTracker operates with a strict no-data-collection policy. We do not require user accounts, emails, or any personal identifiers. Your use of the app is completely anonymous."
                    )
                    
                    PrivacySection(
                        icon: "iphone.homebutton.badge.play",
                        iconColor: .blue,
                        title: "Local Storage Only",
                        content: "All diary entries, frequency settings, and preferences are stored exclusively on your device using Apple's secure Core Data framework. This data is protected by your device's security (e.g., Face ID, Passcode) and is permanently deleted if you uninstall the app."
                    )
                    
                    PrivacySection(
                        icon: "network.slash",
                        iconColor: .green,
                        title: "No Third-Party Services",
                        content: "This app does not include any third-party analytics, advertising networks, or crash reporting services. It functions entirely offline, and no data ever leaves your device."
                    )
                    
                    PrivacySection(
                        icon: "waveform.path.ecg",
                        iconColor: .purple,
                        title: "Audio Permissions",
                        content: "Audio session permissions are requested solely to generate therapeutic sounds. The app only outputs audio; it never records and does not use the microphone."
                    )
                    
                    PrivacySection(
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

// MARK: - Subviews for PrivacyInfoView

private struct HeaderView: View {
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

private struct PrivacySection: View {
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

// MARK: - Preview

#Preview {
    PrivacyInfoView()
}