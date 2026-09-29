# Pace Testing Architecture & Guide 🧪

This document outlines the testing strategy, tooling, and execution instructions for the **Pace** Android application (`ch.simibu.pace`).

---

## 🏛️ Testing Pyramid & Architecture

The testing suite is designed around fast, hermetic, local JVM execution that runs without requiring an emulator or physical device:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Engine & Math Tests (Pure Kotlin)                       │
│    TimerEngineTest.kt                                       │
│    • Mathematical phase calculations                        │
│    • Interval transitions & state logic                     │
├─────────────────────────────────────────────────────────────┤
│ 2. Coroutine Flow Tests (Turbine + Coroutines Test)         │
│    TimerEngineFlowTest.kt                                   │
│    • StateFlow reactive emissions                           │
│    • SharedFlow event pipeline (CountdownTick, Transitions) │
├─────────────────────────────────────────────────────────────┤
│ 3. Persistence & Repositories (Robolectric)                 │
│    RoutineRepositoryTest.kt, SettingsRepositoryTest.kt      │
│    • SharedPreferences & JSON roundtrip                     │
│    • Default routines initialization & CRUD operations      │
│    • Sound schemes, repeats (1–5x), screen awake toggles    │
├─────────────────────────────────────────────────────────────┤
│ 4. Localization Parity (Automated XML Audit)                │
│    LocalizationTest.kt                                      │
│    • Ensures 100% key parity across all 20 European locales │
│    • Detects missing or blank translations automatically    │
├─────────────────────────────────────────────────────────────┤
│ 5. Compose UI & Tactile Components (Robolectric + Compose)  │
│    TactileComponentTest.kt, QuickStartScreenTest.kt         │
│    • TactileCard, TactileSunkenWell, TactilePillButton      │
│    • Screen rendering, scrolling, button click callbacks   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧰 Libraries & Tooling

| Tool / Library | Purpose |
| :--- | :--- |
| **JUnit 4** (`junit:4.13.2`) | Test runner and assertion framework |
| **Robolectric** (`robolectric:4.14.1`) | High-fidelity local Android environment (SDK 34 sandbox) |
| **Turbine** (`turbine:1.2.0`) | Idiomatic assertions on Kotlin `Flow`, `StateFlow`, and `SharedFlow` |
| **Kotlinx Coroutines Test** (`1.9.0`) | Virtual time control (`runTest`, `StandardTestDispatcher`) |
| **Compose UI Test JUnit4** | Headless Compose semantics tree verification |

---

## 🚀 Running Tests Locally

### Run All Unit & UI Tests
```bash
./gradlew testDebugUnitTest
```

### Run a Specific Test Suite
```bash
# Run only Localization parity checks
./gradlew testDebugUnitTest --tests ch.simibu.pace.LocalizationTest

# Run only Timer Engine Flow tests
./gradlew testDebugUnitTest --tests ch.simibu.pace.TimerEngineFlowTest

# Run only Compose UI tests
./gradlew testDebugUnitTest --tests ch.simibu.pace.QuickStartScreenTest
```

### View HTML Test Reports
After running tests, Gradle generates an interactive HTML report at:
```
app/build/reports/tests/testDebugUnitTest/index.html
```
Open this file in any browser to inspect pass/fail counts, duration per test, and stacktraces.

---

## 🤖 Continuous Integration (GitHub Actions)

Automated testing is configured in [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

- **Triggers:** Automatically on every `push` and `pull_request` targeting the `main` branch.
- **Environment:** Ubuntu with Java 21 (Temurin) and Gradle dependency caching.
- **Artifacts:** Automatically uploads HTML test reports if tests fail or finish.
