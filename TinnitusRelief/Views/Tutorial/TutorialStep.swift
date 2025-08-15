import Foundation

enum TutorialStep: Int, CaseIterable {
    case welcome = 0
    case frequencyMatching
    case volumeSetting
    case usageGuidelines
    case navigationOverview
    case completion
    
    var title: String {
        switch self {
        case .welcome:
            return "Welcome to Tinnitus Relief"
        case .frequencyMatching:
            return "Match Your Tinnitus Frequency"
        case .volumeSetting:
            return "Set the Right Volume"
        case .usageGuidelines:
            return "Daily Usage Guidelines"
        case .navigationOverview:
            return "Explore the Features"
        case .completion:
            return "You're All Set!"
        }
    }
    
    var description: String {
        switch self {
        case .welcome:
            return "This app helps reduce your tinnitus through sound therapy. Let's get you started with a quick tutorial."
        case .frequencyMatching:
            return "Use the control area to match the frequency and sound type to your tinnitus. Drag to find the best match."
        case .volumeSetting:
            return "Set the therapy volume to about 70% of your tinnitus loudness. It should be noticeable but not overwhelming."
        case .usageGuidelines:
            return "For best results, listen for about 2 hours daily. You can break this into multiple sessions throughout the day."
        case .navigationOverview:
            return "Use the tabs to track your progress, manage settings, and access all features."
        case .completion:
            return "You're ready to start your tinnitus relief journey. Tap 'Start Therapy' to begin your first session."
        }
    }
    
    var buttonText: String {
        switch self {
        case .welcome, .frequencyMatching, .volumeSetting, .usageGuidelines, .navigationOverview:
            return "Next"
        case .completion:
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
}