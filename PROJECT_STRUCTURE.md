# TinnitusRelief - Project Structure Documentation

## 📱 Project Overview

**TinnitusRelief** is a comprehensive iOS application designed to help users manage tinnitus symptoms through personalized frequency matching, sound therapy, progress tracking, and evidence-based techniques. Built with SwiftUI and leveraging advanced audio processing capabilities.

## 🛠️ Technology Stack

- **Framework**: SwiftUI (iOS 15.0+)
- **Audio**: AVFoundation, AVAudioEngine
- **Data**: Core Data for persistence
- **Architecture**: MVVM (Model-View-ViewModel) pattern
- **Language**: Swift
- **Platform**: iOS (iPhone/iPad compatible)

## 📁 Directory Structure

```
TinnitusRelief.xcodeproj/          # Xcode project configuration
├── project.pbxproj                # Project build settings and file references
├── project.xcworkspace/           # Workspace configuration
└── xcuserdata/                    # User-specific Xcode settings

TinnitusRelief/                    # Main application source
├── TinnitusReliefApp.swift        # App entry point and configuration
├── ContentView.swift              # Root view container
├── Info.plist                     # App configuration and permissions
│
├── Models/                        # Data models and persistence
│   ├── Persistence.swift          # Core Data stack configuration
│   └── TinnitusRelief.xcdatamodeld/ # Core Data model definition
│
├── Views/                         # UI components and screens
│   ├── Components/                # Reusable UI components
│   │   ├── MainTabView.swift      # Tab navigation and home screens
│   │   └── FrequencyController.swift # Frequency control interface
│   ├── FrequencyMatching/         # Frequency matching feature
│   │   ├── FrequencyMatchingView.swift # Main frequency matching UI
│   │   └── FrequencyControlPoint.swift # Interactive control point
│   └── Diary/                     # Progress tracking feature
│       └── DiaryView.swift        # Diary entry and tracking UI
│
├── ViewModels/                    # Business logic layer
│   ├── FrequencyMatchingViewModel.swift # Frequency matching logic
│   └── DiaryViewModel.swift       # Diary data management
│
├── Services/                      # Core services and engines
│   └── AudioEngine/               # Audio processing services
│       ├── AudioEngineService.swift # Legacy audio service
│       └── UnifiedAudioEngineManager.swift # Unified audio engine (NEW)
│
├── Extensions/                    # Swift extensions
│   ├── Color+Extensions.swift     # Color utility extensions
│   └── View+Extensions.swift      # SwiftUI view extensions
│
├── Utilities/                     # Helper utilities
│   └── HapticFeedback.swift       # Haptic feedback management
│
└── Resources/                     # Assets and resources
    ├── Assets.xcassets/           # App icons and visual assets
    └── Preview Content/           # SwiftUI preview assets

Documentation/                     # Project documentation
├── README.md                      # Basic project description
├── AUDIO_TESTING.md              # Audio system testing guide
├── PROJECT_RECOVERY.md           # Project restoration documentation
└── PROJECT_STRUCTURE.md         # This documentation file

build/                            # Build artifacts (generated)
```

## 🏗️ Architecture Overview

### MVVM Pattern Implementation

The app follows the Model-View-ViewModel architectural pattern:

- **Models**: Core Data entities and data structures
- **Views**: SwiftUI views and UI components  
- **ViewModels**: ObservableObject classes managing business logic
- **Services**: Singleton services for audio, persistence, and utilities

### Key Architectural Components

#### 1. Audio System (`UnifiedAudioEngineManager`)
- **Purpose**: Centralized audio engine for both frequency matching and sound library
- **Features**: Real-time audio processing, volume mixing, frequency generation
- **Technology**: AVAudioEngine with custom source nodes and EQ processing

#### 2. Tab-Based Navigation (`MainTabView`)
- **Home Tab**: Dashboard with quick actions and recent activity
- **Frequency Tab**: Interactive frequency matching interface
- **Diary Tab**: Progress tracking and symptom logging
- **Profile Tab**: User preferences and progress overview
- **Settings Tab**: App configuration and preferences

#### 3. Data Persistence (`Persistence.swift`)
- **Core Data Stack**: Manages local data storage
- **Entities**: User preferences, diary entries, frequency sessions
- **Privacy-First**: All data stored locally on device

## 🎵 Key Features

### 1. Frequency Matching
- **Interactive 2D Control**: Drag-based frequency and volume adjustment
- **Real-time Audio**: Immediate sine wave generation and filtering
- **Range**: 20Hz - 16kHz frequency spectrum
- **Technology**: White noise with bandpass EQ filtering

### 2. Sound Library (Referenced but not fully visible in structure)
- **Categories**: Noise types, nature sounds, meditation tones
- **Mixing**: Multiple simultaneous sound playback
- **Volume Control**: Individual sound level management

### 3. Progress Tracking
- **Daily Diary**: Symptom severity and stress level tracking
- **Analytics**: Trend analysis and progress visualization
- **Export**: Data export for healthcare providers

### 4. User Experience
- **Professional UI**: Clean, accessible design with Material effects
- **Haptic Feedback**: Tactile interaction responses
- **Dark/Light Mode**: System appearance adaptation
- **Accessibility**: VoiceOver and accessibility support

## 🔧 Core Components

### Audio Engine (`UnifiedAudioEngineManager.swift`)
```swift
class UnifiedAudioEngineManager: ObservableObject {
    static let shared = UnifiedAudioEngineManager()
    private let audioEngine = AVAudioEngine()
    private var frequencySourceNode: AVAudioSourceNode!
    private let frequencyEQ = AVAudioUnitEQ(numberOfBands: 1)
}
```

**Responsibilities:**
- Audio session management and configuration
- Real-time frequency generation with white noise + EQ
- Volume and frequency parameter updates
- Audio interruption handling (calls, etc.)
- Singleton pattern for app-wide audio coordination

### Main Navigation (`MainTabView.swift`)
**Structure:**
- **HomeView**: Welcome dashboard with quick actions
- **FrequencyMatchingView**: Interactive frequency matching
- **DiaryView**: Progress tracking interface
- **ProfileView**: User information and preferences
- **SettingsView**: App configuration options
- **SleepTimerView**: Gradual audio fade-out functionality

### Data Models (`Persistence.swift`)
- Core Data stack initialization
- Managed object context configuration
- Data model version management
- Privacy-compliant local storage

## 🔄 Recent Changes & Improvements

### Audio System Redesign
- **✅ Unified Engine**: Replaced multiple audio engines with single `UnifiedAudioEngineManager`
- **✅ Volume Control**: Fixed real-time volume updates for sound mixing
- **✅ Session Management**: Single, centralized audio session prevents conflicts
- **✅ Simultaneous Audio**: Both frequency matching AND sound library work together

### Project Recovery
- **✅ UUID Conflicts Resolved**: Clean project structure with unique identifiers
- **✅ Build Configuration**: Proper iOS 15.0+ deployment target
- **✅ File References**: All Swift files properly included and referenced
- **✅ Group Structure**: Organized project navigator hierarchy

## 📱 User Interface Components

### Reusable Components
- **QuickActionCard**: Navigation cards with icons and descriptions
- **ActivityRowView**: Recent activity list items
- **ProgressCardView**: Metric display cards
- **SettingsRowView**: Configuration option rows

### Styling Approach
- **Material Design**: `.ultraThinMaterial` backgrounds
- **Color Scheme**: Orange accent color with system adaptability
- **Typography**: System fonts with semantic sizing
- **Spacing**: Consistent 12-24pt spacing grid

## 🗃️ File Descriptions

| File | Purpose | Key Features |
|------|---------|-------------|
| `TinnitusReliefApp.swift` | App entry point | Core Data integration, window configuration |
| `ContentView.swift` | Root view container | Environment setup, navigation root |
| `MainTabView.swift` | Tab navigation system | 5-tab structure, home dashboard, settings |
| `UnifiedAudioEngineManager.swift` | Audio engine service | Frequency generation, volume mixing, session management |
| `FrequencyMatchingView.swift` | Frequency matching UI | Interactive controls, real-time audio feedback |
| `FrequencyMatchingViewModel.swift` | Frequency matching logic | Audio parameter management, user interaction handling |
| `DiaryView.swift` | Progress tracking interface | Symptom logging, trend visualization |
| `Persistence.swift` | Core Data configuration | Local database setup, privacy compliance |
| `Color+Extensions.swift` | Color utilities | Brand colors, accessibility colors |
| `HapticFeedback.swift` | Tactile feedback | User interaction feedback |

## 🚀 Getting Started

### Prerequisites
- Xcode 13.0+ 
- iOS 15.0+ target device/simulator
- macOS 12.0+ development machine

### Building the Project
```bash
cd /Users/midnight/Documents/Projects/tin
open TinnitusRelief.xcodeproj
```

### Testing Audio Features
Refer to `AUDIO_TESTING.md` for comprehensive audio system testing procedures.

## 🔐 Privacy & Security

- **Local Data Only**: All user data stored on device using Core Data
- **No Network Requests**: Completely offline functionality
- **Health Privacy**: Compliant with health data privacy standards
- **Audio Permissions**: Proper microphone/audio session management

## 📋 Technical Specifications

- **Minimum iOS Version**: 15.0
- **Audio Sample Rate**: 44.1 kHz
- **Frequency Range**: 20 Hz - 16,000 Hz
- **Audio Latency**: Optimized for real-time interaction
- **Memory Usage**: Efficient Core Data and audio buffer management
- **Battery Impact**: Optimized audio processing for minimal drain

---

**Note**: This is a therapeutic application designed to complement, not replace, professional medical treatment for tinnitus. Users should consult healthcare providers for proper diagnosis and treatment planning.