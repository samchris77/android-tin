import SwiftUI
import Foundation

class LocalizationManager: ObservableObject {
    static let shared = LocalizationManager()
    
    @Published var currentLanguage: String {
        didSet {
            UserDefaults.standard.set(currentLanguage, forKey: "selectedLanguage")
            
            // Set the app's language
            if let path = Bundle.main.path(forResource: currentLanguage, ofType: "lproj") {
                Bundle.setLanguage(path)
            }
        }
    }
    
    private init() {
        self.currentLanguage = UserDefaults.standard.string(forKey: "selectedLanguage") ?? "en"
        
        // Set initial language
        if let path = Bundle.main.path(forResource: currentLanguage, ofType: "lproj") {
            Bundle.setLanguage(path)
        }
    }
    
    func setLanguage(_ language: String) {
        currentLanguage = language
    }
    
    func localizedString(for key: String) -> String {
        return NSLocalizedString(key, comment: "")
    }
}

// Extension to support language switching at runtime
extension Bundle {
    private static var bundle: Bundle!
    
    public static func setLanguage(_ path: String) {
        bundle = Bundle(path: path) ?? Bundle.main
    }
    
    public static func localizedBundle() -> Bundle {
        return bundle ?? Bundle.main
    }
}

// Custom localized string function that respects our language manager
func LocalizedString(_ key: String) -> String {
    return Bundle.localizedBundle().localizedString(forKey: key, value: nil, table: nil)
}

// SwiftUI Text extension for easy localization
extension Text {
    init(localized key: String) {
        self.init(LocalizedString(key))
    }
}

// Helper for language codes
extension LocalizationManager {
    var isKorean: Bool {
        return currentLanguage == "ko"
    }
    
    var isEnglish: Bool {
        return currentLanguage == "en"
    }
    
    var displayLanguage: String {
        switch currentLanguage {
        case "ko":
            return LocalizedString("profile.language.korean")
        default:
            return LocalizedString("profile.language.english")
        }
    }
}