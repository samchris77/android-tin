# TinnitusRelief - iOS App Summary

## 📱 Overview

**TinnitusRelief** is a comprehensive iOS application designed to help users manage tinnitus symptoms through personalized frequency matching, sound therapy, progress tracking, and evidence-based techniques. The app provides a privacy-first approach with all data stored locally on the device.

## 🎯 Core Purpose

The app serves as a therapeutic tool that allows users to:
- Match their tinnitus frequency through interactive audio controls
- Track symptom progression over time
- Monitor stress levels and comfort ratings
- Export data for healthcare providers

## 🏗️ Technical Architecture

### Platform & Framework
- **iOS**: 15.0+ target, built with SwiftUI
- **Architecture**: MVVM (Model-View-ViewModel) pattern
- **Data**: Core Data for local persistence
- **Audio**: AVFoundation with AVAudioEngine for real-time processing

### Key Components

#### Audio Engine (`UnifiedAudioEngineManager.swift`)
- Centralized audio processing using singleton pattern
- Real-time frequency generation (20Hz - 16kHz)
- White noise with bandpass EQ filtering
- Volume control and audio session management
- Handles audio interruptions (calls, notifications)

#### Navigation (`MainTabView.swift`)
- 3-tab interface: Frequency, Log, Profile
- Adaptive frequency controller that appears on non-frequency tabs
- Clean Material Design with orange accent color

#### Data Management
- **Core Data**: Local SQLite database for privacy
- **ViewModels**: Reactive data binding with Combine framework
- **Models**: DiaryEntry entity with frequency, volume, and symptom data

## 🎵 Key Features

### 1. Frequency Matching
- **Interactive 2D Control**: Drag-based frequency and volume adjustment
- **Real-time Audio**: Immediate sine wave generation and filtering
- **Visual Feedback**: Live frequency (Hz/kHz) and volume (%) display
- **Haptic Feedback**: Tactile response during interaction
- **Range**: 20Hz - 16,000Hz with logarithmic scaling

### 2. Progress Tracking (Diary)
- **Daily Logging**: Symptom severity, comfort, and stress levels (1-10 scale)
- **Auto-capture**: Current frequency and volume settings
- **Analytics**: Weekly summaries and trend analysis
- **Export**: CSV export for healthcare provider sharing
- **Metrics**: Average loudness, stress, and listening time calculations

### 3. Profile & Settings
- **Progress Overview**: Days tracked, frequency sessions, average metrics
- **Settings**: Background audio, daily reminders, data export
- **Privacy**: All data remains on device
- **Help**: Built-in FAQ and support information

## 📊 Data Model

### DiaryEntry Entity
- **Core Metrics**: Loudness level, comfort level, stress level (1-10)
- **Audio Data**: Current frequency (Hz), current volume (0-1)
- **Session Info**: Date, entry number, session duration
- **Notes**: Optional text notes for context

### Analytics Features
- Weekly trend summaries
- Average calculations across all metrics
- Most common frequency detection
- Total and average listening time tracking

## 🔒 Privacy & Security

- **Local-Only Storage**: All data stored using Core Data (SQLite)
- **No Network Requests**: Completely offline functionality
- **Health Compliance**: Follows health data privacy standards
- **Audio Permissions**: Proper session management for system integration

## 🎨 User Experience

### Design Language
- **Material Design**: Ultra-thin material backgrounds
- **Color Scheme**: Orange accent with system appearance adaptation
- **Typography**: System fonts with semantic sizing
- **Spacing**: Consistent 12-24pt grid system

### Interaction Patterns
- **Gesture-Based**: Drag controls for frequency matching
- **Haptic Feedback**: Light impact feedback for user actions
- **Accessibility**: VoiceOver and system accessibility support
- **Dark/Light Mode**: Automatic system appearance adaptation

## 🚀 Current State

### Completed Features
- ✅ Interactive frequency matching with real-time audio
- ✅ Comprehensive diary logging system
- ✅ Progress analytics and trend tracking
- ✅ CSV data export functionality
- ✅ Clean tab-based navigation
- ✅ Audio session management and interruption handling

### Code Quality
- **SwiftUI**: Modern declarative UI framework
- **Combine**: Reactive data binding and state management
- **MVVM**: Clean separation of concerns
- **Error Handling**: Proper Core Data error management
- **Memory Management**: Efficient audio buffer and data handling

## 🎯 Target Users

- **Primary**: Individuals experiencing tinnitus symptoms
- **Secondary**: Healthcare providers requiring patient progress data
- **Use Cases**: Daily symptom tracking, frequency therapy sessions, long-term progress monitoring

## 📈 Technical Specifications

- **Audio Sample Rate**: 44.1 kHz
- **Frequency Range**: 20 Hz - 16,000 Hz
- **Audio Latency**: Optimized for real-time interaction
- **Data Storage**: Core Data with SQLite backend
- **Battery Optimization**: Efficient audio processing to minimize drain
- **Memory Usage**: Lightweight with proper resource management

## 🔄 Development Status

The app is currently in active development on the `gemini` branch with recent improvements to:
- Frequency matching user interface
- Diary entry system enhancements
- Audio engine optimization
- Navigation flow improvements

---

**Note**: This application is designed as a therapeutic tool to complement professional medical treatment for tinnitus. It is not intended as a replacement for proper medical diagnosis or treatment. Users should consult healthcare providers for comprehensive tinnitus management.