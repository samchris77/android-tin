import SwiftUI

struct WelcomeTutorialView: View {
    @ObservedObject var tutorialManager: TutorialManager
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.orange.opacity(0.1),
                    Color.blue.opacity(0.1)
                ]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            VStack(spacing: 40) {
                Spacer()
                
                // App icon and title
                VStack(spacing: 24) {
                    Image(systemName: "ear")
                        .font(.system(size: 80))
                        .foregroundColor(.orange)
                        .symbolEffect(.pulse.byLayer, options: .repeating)
                    
                    VStack(spacing: 8) {
                        Text("Welcome to")
                            .font(.title2)
                            .foregroundColor(.secondary)
                        
                        Text("Tinnitus Relief")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.primary)
                    }
                }
                
                // Feature highlights
                VStack(spacing: 20) {
                    FeatureRow(
                        icon: "waveform",
                        title: "Match Your Tinnitus",
                        description: "Find the exact frequency and sound type"
                    )
                    
                    FeatureRow(
                        icon: "speaker.wave.2",
                        title: "Set Optimal Volume",
                        description: "70% of your tinnitus loudness for best results"
                    )
                    
                    FeatureRow(
                        icon: "clock",
                        title: "2 Hours Daily",
                        description: "Consistent therapy for effective relief"
                    )
                }
                .padding(.horizontal, 32)
                
                Spacer()
                
                // Action buttons
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

private struct FeatureRow: View {
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
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
        .padding(.vertical, 8)
    }
}

#Preview {
    WelcomeTutorialView(
        tutorialManager: TutorialManager(persistenceController: PersistenceController.preview)
    )
}