import SwiftUI

struct TutorialOverlayView: View {
    @ObservedObject var tutorialManager: TutorialManager
    let highlightFrame: CGRect?
    
    init(tutorialManager: TutorialManager, highlightFrame: CGRect? = nil) {
        self.tutorialManager = tutorialManager
        self.highlightFrame = highlightFrame
    }
    
    var body: some View {
        ZStack {
            // Semi-transparent background
            Color.black.opacity(0.7)
                .ignoresSafeArea()
                .onTapGesture {
                    // Prevent dismissing tutorial by tapping background
                }
            
            // Highlight cutout if frame is provided
            if let frame = highlightFrame {
                Rectangle()
                    .frame(width: frame.width + 20, height: frame.height + 20)
                    .position(x: frame.midX, y: frame.midY)
                    .blendMode(.destinationOut)
            }
            
            // Tutorial content
            VStack(spacing: 0) {
                Spacer()
                
                tutorialContentView
                    .padding(.horizontal, 24)
                    .padding(.bottom, 50)
            }
        }
        .compositingGroup()
    }
    
    private var tutorialContentView: some View {
        VStack(spacing: 24) {
            // Step indicator
            stepIndicator
            
            // Content card
            VStack(spacing: 20) {
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
                
                // Action buttons
                VStack(spacing: 12) {
                    // Primary action button
                    Button(action: {
                        if tutorialManager.currentStep == .completion {
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
                    
                    // Skip button (only on welcome)
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
            .background(.ultraThinMaterial)
            .cornerRadius(16)
        }
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

#Preview {
    TutorialOverlayView(
        tutorialManager: TutorialManager(persistenceController: PersistenceController.preview),
        highlightFrame: CGRect(x: 100, y: 200, width: 200, height: 100)
    )
}