import SwiftUI
import CoreData

struct ContentView: View {
    @Environment(\.managedObjectContext) private var viewContext
    @Environment(\.scenePhase) private var scenePhase
    @EnvironmentObject private var localizationManager: LocalizationManager
    private let audioManager = UnifiedAudioEngineManager.shared

    var body: some View {
        MainTabView()
            .environment(\.managedObjectContext, viewContext)
            .onChange(of: scenePhase) { oldPhase, newPhase in
                if newPhase == .inactive || newPhase == .background {
                    audioManager.saveAudioState()
                }
            }
            .id(localizationManager.currentLanguage)
            .environment(\.locale, Locale(identifier: localizationManager.currentLanguage))
    }
}