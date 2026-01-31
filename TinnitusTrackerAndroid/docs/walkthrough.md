# Tinnitus Tracker Android Port - Walkthrough

I have successfully created a native Android version of the Tinnitus Tracker app using **Kotlin** and **Jetpack Compose**.

## Project Location
The new project is located at:
`~/Documents/Projects/android/tin/TinnitusTrackerAndroid`

## 🏗️ Architecture & Features

### 1. Audio Engine (Core Feature)
-   **Implementation**: `AudioEngine.kt` uses the native `AudioTrack` API for low-latency audio generation.
-   **Signal Processing**: I implemented a **Biquad BandPass Filter** (`BiquadFilter.kt`) that mathematically matches the iOS `AVAudioUnitEQ`.
-   **Real-time Control**: dragging the pad on the "Frequency" tab updates the filter coefficients and white noise volume instantly utilizing a dedicated audio thread.

### 2. Data Persistence (Diary)
-   **Database**: Uses **Room** (SQLite) to store diary entries locally.
-   **Entities**: `DiaryEntry` supports storing loudness, stress, and comfort levels.
-   **Architecture**: Uses a `DiaryDao` with Kotlin Coroutines (`Flow`) to reactively update the UI when data changes.

### 3. User Interface
-   **Framework**: **Jetpack Compose** (Material 3).
-   **Navigation**: Bottom Navigation Bar with 3 tabs: Frequency, Diary, Profile.
-   **Screens**:
    -   **Frequency**: Interactive 2D drag pad with gradient background.
    -   **Diary**: List of entries with ability to add new ones via a form.
    -   **Profile**: Placeholder for future settings.

## 🚀 How to Run

1.  Open **Android Studio**.
2.  Select **Open** and navigate to `~/Documents/Projects/android/tin/TinnitusTrackerAndroid`.
3.  Wait for Gradle to sync.
4.  Connect an Android device or start an Emulator.
5.  Click the **Run** (▶️) button.

## ✅ Verification Steps

### Test Frequency Matching
1.  Go to the **Frequency** tab.
2.  Tap the **Play** button. You should hear white noise.
3.  Drag the circle around.
    -   **Right/Left**: Changes Pitch (Frequency).
    -   **Up/Down**: Changes Volume.
4.  Verify the sound changes smoothly without clicking/popping (handled by the interpolation logic in `AudioEngine`).

### Test Diary
1.  Go to the **Diary** tab.
2.  Click the **+** (FAB) button.
3.  Adjust sliders for Loudness/Stress and add a Note.
4.  Click **Save**.
5.  Verify the new entry appears in the list.
6.  Restart the app and verify the entry is still there (Persistence check).

## Known Differences from iOS
-   **Profile**: The profile screen is currently a placeholder (on iOS it has advanced analytics).
-   **Charts**: The iOS app has weekly summary charts. The Android version currently lists the raw entries.
