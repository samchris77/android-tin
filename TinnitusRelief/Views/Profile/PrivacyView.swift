import SwiftUI

struct PrivacyView: View {
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Header
                    VStack(alignment: .leading, spacing: 16) {
                        Image(systemName: "lock.shield.fill")
                            .font(.system(size: 64))
                            .foregroundColor(.blue)
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Privacy Policy")
                                .font(.largeTitle)
                                .fontWeight(.bold)
                                .foregroundColor(.primary)
                            
                            Text("Your privacy is our priority")
                                .font(.title3)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.top, 20)
                    
                    // Privacy sections
                    PrivacySection(
                        title: "Data Collection",
                        content: "TinnitusRelief collects minimal data to provide you with the best possible experience. We only collect information that is necessary for the app to function properly, including your tinnitus tracking data, frequency preferences, and usage patterns."
                    )
                    
                    PrivacySection(
                        title: "Local Storage",
                        content: "All your personal health data is stored locally on your device. This means your sensitive information never leaves your device unless you explicitly choose to export it. We believe your health data should remain under your control."
                    )
                    
                    PrivacySection(
                        title: "Data Export",
                        content: "You have the right to export your data at any time. When you use the export feature, you can choose what data to include and where to share it. This is particularly useful for sharing progress with healthcare providers."
                    )
                    
                    PrivacySection(
                        title: "No Third-Party Sharing",
                        content: "We do not share, sell, or transfer your personal data to third parties. Your tinnitus tracking information, audio preferences, and usage patterns remain completely private."
                    )
                    
                    PrivacySection(
                        title: "Analytics",
                        content: "We may collect anonymous usage analytics to improve the app's performance and features. This data is aggregated and cannot be used to identify individual users. You can opt out of analytics in the app settings."
                    )
                    
                    PrivacySection(
                        title: "Contact Information",
                        content: "If you have any questions about this privacy policy or your data, please contact our support team. We are committed to transparency and will respond to your inquiries promptly."
                    )
                    
                    // Medical Disclaimer
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Medical Disclaimer")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .foregroundColor(.primary)
                        
                        Text("TinnitusRelief is designed to help you manage tinnitus symptoms through personalized frequency matching and progress tracking. This app is not a substitute for professional medical advice, diagnosis, or treatment. Please consult with a qualified healthcare provider for proper medical care.")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .padding(16)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(.orange.opacity(0.1))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 12)
                                            .stroke(.orange.opacity(0.3), lineWidth: 1)
                                    )
                            )
                    }
                    
                    Spacer(minLength: 20)
                }
                .padding(.horizontal, 20)
            }
            .navigationTitle("Privacy")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct PrivacySection: View {
    let title: String
    let content: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            
            Text(content)
                .font(.body)
                .foregroundColor(.secondary)
                .lineSpacing(4)
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(.ultraThinMaterial)
        )
    }
}

#Preview {
    PrivacyView()
}