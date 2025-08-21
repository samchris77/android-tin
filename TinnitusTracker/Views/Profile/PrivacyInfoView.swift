// PrivacyInfoView.swift
import SwiftUI

struct PrivacyInfoView: View {
    @Environment(\.presentationMode) var presentationMode
    @StateObject private var localizationManager = LocalizationManager.shared

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    HeaderView()
                    
                    // Main Privacy Points
                    PrivacySection(
                        icon: "xmark.shield.fill",
                        iconColor: .red,
                        title: LocalizedString("privacy.section.no.data.title"),
                        content: LocalizedString("privacy.section.no.data.content")
                    )
                    
                    PrivacySection(
                        icon: "iphone.homebutton.badge.play",
                        iconColor: .blue,
                        title: LocalizedString("privacy.section.local.storage.title"),
                        content: LocalizedString("privacy.section.local.storage.content")
                    )
                    
                    PrivacySection(
                        icon: "network.slash",
                        iconColor: .green,
                        title: LocalizedString("privacy.section.no.third.party.title"),
                        content: LocalizedString("privacy.section.no.third.party.content")
                    )
                    
                    PrivacySection(
                        icon: "waveform.path.ecg",
                        iconColor: .purple,
                        title: LocalizedString("privacy.section.audio.permissions.title"),
                        content: LocalizedString("privacy.section.audio.permissions.content")
                    )
                    
                    PrivacySection(
                        icon: "hand.raised.fill",
                        iconColor: .orange,
                        title: LocalizedString("privacy.section.user.control.title"),
                        content: LocalizedString("privacy.section.user.control.content")
                    )
                    
                    // Final Assurance
                    VStack(alignment: .center, spacing: 10) {
                        Text(localized: "privacy.guarantee.title")
                            .font(.headline)
                            .fontWeight(.bold)
                        Text(localized: "privacy.guarantee.description")
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
            .navigationTitle(LocalizedString("privacy.navigation.title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(LocalizedString("privacy.navigation.done")) {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
        }
        .id(localizationManager.currentLanguage)
    }
}

// MARK: - Subviews for PrivacyInfoView

private struct HeaderView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(localized: "privacy.header.title")
                .font(.title)
                .fontWeight(.bold)
            
            Text(localized: "privacy.header.last.updated")
                .font(.caption)
                .foregroundColor(.secondary)
            
            Text(localized: "privacy.header.description")
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