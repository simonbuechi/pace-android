# Pace Amigo (Pace) ⏱️

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12.01-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203%20Expressive%20%2B%20Tactile-9123A6.svg)](DESIGN.md)

**Pace Amigo** (or **Pace** for short) is a modern interval timer Android application tailored for workouts (HIIT, Tabata, Boxing, Circuit Training), productivity sprints (Pomodoro), and mobility/stretch sessions. Designed from the ground up with Jetpack Compose, Material 3 Expressive, and soft tactile neumorphic surfaces.

---

## 🌟 Key Features

- **⚡ Quick Starter**:
  - Set custom **Focus duration** (minutes & seconds via tactile wheel pickers).
  - Set custom **Break duration** (minutes & seconds).
  - Select custom **Rounds / Iterations** with dedicated `+` / `-` steppers.
  - Immediate 1-tap **"Start Session"** with signature brand gradient.
- **🖥️ Fullscreen Timer with Maximal Font Size**:
  - **Auto-Sizing Typography**: Digits scale dynamically to fill the entire screen (`140sp`–`170sp+`) without clipping, legible from across a large room.
  - **Background Countdown Circle**: The tactile countdown ring renders smoothly in the background behind the digits, freeing up the full screen.
  - **Distraction-Free Auto-Hide Controls**: All controls hide while the timer runs and smoothly fade in upon tapping the screen.
  - Full landscape / widescreen optimization.
- **🔔 Background Foreground Service**:
  - Seamless background execution via [`PaceTimerService`](file:///d:/dev/antigravity/pace-android/app/src/main/java/ch/simibu/pace/service/PaceTimerService.kt).
  - Ongoing interactive notification with live countdown and media-style action buttons (**Pause/Resume**, **Skip Phase**, **Stop**).
  - Android 14+ (`API 34`) compatible foreground service type (`specialUse`).
- **📋 Routine Library & Custom Presets**:
  - Pre-seeded presets out of the box: **Tabata HIIT**, **Classic Pomodoro**, **Boxing Rounds**, and **Mobility & Stretch**.
  - Create, customize, edit, and delete personal routines with custom sound schemes, color palettes, and optional Warm-up and Cool-down phases.
- **🎨 Brand Identity & Soft Tactile Design**:
  - Signature brand gradient: **Pace Magenta (`#9123A6`)** to **Pace Raspberry (`#D7195F`)**.
  - Soft tactile / neumorphic surfaces ([`TactileCard`](file:///d:/dev/antigravity/pace-android/app/src/main/java/ch/simibu/pace/ui/components/TactileSurface.kt), [`TactileSunkenWell`](file:///d:/dev/antigravity/pace-android/app/src/main/java/ch/simibu/pace/ui/components/TactileSurface.kt), [`TactilePillButton`](file:///d:/dev/antigravity/pace-android/app/src/main/java/ch/simibu/pace/ui/components/TactileSurface.kt)).
  - Full **Light Theme** (`#F4F1F7`) and **Dark Theme** (`#16141B`) support with automatic luminance-based adaptation.
- **🔊 Low-Latency Audio & Haptic Feedback**:
  - Native `SoundPool` engine with bundled high-quality sound cues: `Beep`, `Temple Bell`, `Gentle Chime`, `Marimba Pop`, `Singing Bowl`, `Zen Gong`, and `Digital Pulse`.
  - Independent focus and break sound selection with configurable repeats (1–5x).
  - Independent audio and haptic toggles in settings.
- **🌐 20 European Languages Supported**:
  - Full native localization for English, German, French, Spanish, Italian, Portuguese, Dutch, Polish, Ukrainian, Russian, Turkish, Swedish, Czech, Greek, Romanian, Hungarian, Danish, Finnish, Norwegian Bokmål, and Croatian.
  - Automatically adheres to device language preferences.
- **💡 Screen Awake Management**:
  - Optional toggle to keep the screen active during active workouts.

---

## 🏗️ Architecture & Tech Stack

```
ch.simibu.pace
├── audio/            # SoundManager (SoundPool, Vibrator haptics)
├── data/             # Repositories (RoutineRepository, SettingsRepository)
├── engine/           # TimerEngine (Coroutines, StateFlow, State Machine)
├── model/            # Domain models (Routine, TimerState, TimerPhase, SoundScheme, ColorSchemeOption)
├── service/          # PaceTimerService (Android Foreground Service, Notifications)
└── ui/
    ├── components/   # TactileCard, TactileSunkenWell, TactilePillButton, CircularTimerRing, WheelPicker, PaceNavigation
    ├── screens/      # QuickStartScreen, TimerScreen, RoutinesScreen, RoutineEditorDialog, SettingsScreen
    └── theme/        # Color, Theme, Type
```

- **Language**: Kotlin 2.2.0
- **UI Toolkit**: Jetpack Compose (BOM `2024.12.01`)
- **Design System**: Material 3 Expressive + Custom Tactile Surfaces
- **State Management**: Unidirectional Data Flow (UDF) with `StateFlow` and Coroutines
- **Persistence**: Android `SharedPreferences` + `kotlinx.serialization`
- **Audio Engine**: Low-latency `android.media.SoundPool`
- **Gradle**: 9.1.0 with Android Gradle Plugin `8.12.1`
- **Target SDK**: 35 (Android 15) | **Min SDK**: 26 (Android 8.0)

---

## 🚀 Getting Started

### Prerequisites
- **JDK**: Java 21 (Temurin or OpenJDK 21)
- **Android SDK**: API 35 build tools
- **Android Device or Emulator**: Android 8.0 (API 26) or higher

### Build & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/simibu/pace-android.git
   cd pace-android
   ```

2. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

3. **Assemble Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Build Production Release Bundle (AAB for Google Play)**:
   ```bash
   ./gradlew bundleRelease
   ```
   The signed release bundle will be generated at:
   `app/build/outputs/bundle/release/app-release.aab`

5. **Install to connected device via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

6. **Launch Pace**:
   ```bash
   adb shell am start -n ch.simibu.pace/.MainActivity
   ```

---

## 📖 Further Documentation

- **[`PLAY_STORE.md`](PLAY_STORE.md)** — Complete Google Play Store listing copy (English & German), metadata, character counts, and submission steps.
- **[`PRIVACY_POLICY.md`](PRIVACY_POLICY.md)** — Comprehensive privacy policy and permission disclosures for store review.
- **[`DESIGN.md`](DESIGN.md)** — In-depth design specifications, color tokens, layout formulas, and tactile component guidelines.

---

## 📄 License

Copyright © 2026 Simon Bu. All rights reserved.
Licensed under the Apache License, Version 2.0.
