# Android Tinnitus Tracker Implementation Plan

This plan outlines the steps to build a native Android version of the Tinnitus Tracker app, matching the functionality of the existing iOS application.

## Goal
Create a production-ready Android application with the following core features:
1.  **Frequency Matching**: Real-time white noise generation with adjustable bandpass filter (frequency & volume).
2.  **Diary/Journal**: Tracking of tinnitus symptoms, stress, and session duration.
3.  **Data Persistence**: Local storage of user profiles and diary entries.
4.  **UI/UX**: Modern Material Design interface matching the iOS aesthetic.

## Technology Stack
-   **Language**: Kotlin
-   **UI Framework**: Jetpack Compose (Modern, declarative UI like SwiftUI)
-   **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
-   **Dependency Injection**: Hilt (optional, or manual for simplicity in this generated code)
-   **Database**: Room (SQLite abstraction)
-   **Audio**: Android `AudioTrack` API for raw PCM generation (White Noise) and digital signal processing (Biquad Filter).

## Proposed Changes (New Project Structure)

We will create a new directory `TinnitusTrackerAndroid` with the following structure:

### 1. Project Configuration
#### [NEW] `TinnitusTrackerAndroid/build.gradle.kts` (Root build file)
#### [NEW] `TinnitusTrackerAndroid/settings.gradle.kts` (Settings)
#### [NEW] `TinnitusTrackerAndroid/app/build.gradle.kts` (App module build file)

### 2. Core Application
#### [NEW] `TinnitusTrackerAndroid/app/src/main/AndroidManifest.xml`

### 3. Source Code (`app/src/main/java/com/tinnitustracker/`)

#### Audio Engine (`audio/`)
-   `AudioEngine.kt`: Handles `AudioTrack` creation, thread management, and raw PCM data writing.
-   `BiquadFilter.kt`: Implementation of the BandPass filter algorithm.
-   `WhiteNoiseGenerator.kt`: Generates random float/short samples.

#### Data Layer (`data/`)
-   `AppDatabase.kt`: Room database definition.
-   `DiaryEntry.kt`: Entity class for diary logs.
-   `DiaryDao.kt`: Data Access Object for database operations.
-   `UserProfile.kt`: Entity for user settings.

#### Feature: Frequency Matching (`ui/frequency/`)
-   `FrequencyMatchingScreen.kt`: The interactive UI with draggable controls.
-   `FrequencyViewModel.kt`: Manages state (freq, volume) and communicates with `AudioEngine`.

#### Feature: Diary (`ui/diary/`)
-   `DiaryScreen.kt`: List of entries and charts (simplified charts for now).
-   `DiaryEntryScreen.kt`: Form to add/edit entries.
-   `DiaryViewModel.kt`: Business logic for the diary.

#### Feature: Navigation (`ui/`)
-   `MainActivity.kt`: Entry point.
-   `MainScreen.kt`: Bottom navigation implementation (Home, Frequency, Diary, Profile).
-   `Theme.kt`: Application theme (colors, typography).

## Verification Plan

### Manual Verification
1.  **Audio Test**: Verify "Frequency Matching" produces sound that changes pitch when dragging slider/pad.
2.  **Persistence Test**: Add a diary entry, restart app, verify entry persists.
3.  **UI Test**: Check navigation between tabs works.

### Automated Tests
-   Verify database inserts/deletes with JUnit tests (if requested).
