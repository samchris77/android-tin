import SwiftUI

@main
struct TinnitusTrackerApp: App {
    let persistenceController = PersistenceController.shared
    @StateObject private var localizationManager = LocalizationManager.shared

    init() {
        UIApplication.shared.beginReceivingRemoteControlEvents()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.managedObjectContext, persistenceController.container.viewContext)
                .environmentObject(localizationManager)
        }
    }
}