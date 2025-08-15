import Foundation
import SwiftUI
import CoreData

class TutorialManager: ObservableObject {
    @Published var isShowingTutorial: Bool = false
    @Published var currentStep: TutorialStep = .welcome
    @Published var isAnimating: Bool = false
    
    private let persistenceController: PersistenceController
    
    init(persistenceController: PersistenceController) {
        self.persistenceController = persistenceController
        checkOnboardingStatus()
    }
    
    func checkOnboardingStatus() {
        let context = persistenceController.container.viewContext
        let request: NSFetchRequest<UserProfile> = UserProfile.fetchRequest()
        
        do {
            let profiles = try context.fetch(request)
            if let profile = profiles.first {
                isShowingTutorial = !profile.hasCompletedOnboarding
            } else {
                // Create new user profile if none exists
                let newProfile = UserProfile(context: context)
                newProfile.hasCompletedOnboarding = false
                newProfile.id = UUID()
                try context.save()
                isShowingTutorial = true
            }
        } catch {
            print("Error checking onboarding status: \(error)")
            isShowingTutorial = true
        }
    }
    
    func nextStep() {
        withAnimation(.easeInOut(duration: 0.3)) {
            if let nextStepRawValue = currentStep.rawValue + 1,
               let nextStep = TutorialStep(rawValue: nextStepRawValue) {
                currentStep = nextStep
            } else {
                completeTutorial()
            }
        }
    }
    
    func skipTutorial() {
        withAnimation(.easeInOut(duration: 0.3)) {
            completeTutorial()
        }
    }
    
    func completeTutorial() {
        let context = persistenceController.container.viewContext
        let request: NSFetchRequest<UserProfile> = UserProfile.fetchRequest()
        
        do {
            let profiles = try context.fetch(request)
            if let profile = profiles.first {
                profile.hasCompletedOnboarding = true
                try context.save()
            }
        } catch {
            print("Error completing tutorial: \(error)")
        }
        
        isShowingTutorial = false
    }
    
    func resetTutorial() {
        let context = persistenceController.container.viewContext
        let request: NSFetchRequest<UserProfile> = UserProfile.fetchRequest()
        
        do {
            let profiles = try context.fetch(request)
            if let profile = profiles.first {
                profile.hasCompletedOnboarding = false
                try context.save()
            }
        } catch {
            print("Error resetting tutorial: \(error)")
        }
        
        currentStep = .welcome
        isShowingTutorial = true
    }
}