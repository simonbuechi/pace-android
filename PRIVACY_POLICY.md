# Privacy Policy for Pace

**Last Updated:** September 29, 2026

Pace ("we", "our", or "us"), package name `ch.simibu.pace`, is committed to protecting your privacy. This Privacy Policy explains our practices regarding data collection, usage, and device permissions.

---

### 1. Zero Data Collection & Privacy by Design
- **No Personal Data Collected:** Pace does not collect, transmit, sell, or share any personal information, identifiers, usage analytics, or diagnostic data.
- **No Third-Party SDKs:** The application does not contain advertising networks, third-party analytics (such as Google Analytics or Firebase), or social media tracking libraries.
- **Local-Only Storage:** All user-created routines, timers, and application settings are stored locally on your device using Android Jetpack DataStore / SharedPreferences. No data is ever sent to external servers or cloud services.

---

### 2. Device Permissions & Purpose

Pace requests only the minimum permissions necessary to function as an interval and workout timer:

| Permission | Purpose |
| :--- | :--- |
| `android.permission.FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps the interval timer running reliably without interruption when your screen is locked or while switching between apps. |
| `android.permission.POST_NOTIFICATIONS` | Displays the current interval countdown, phase name (Focus, Break, Warm-up, Cool-down), and media controls directly in your system notification drawer and lock screen. |
| `android.permission.VIBRATE` | Provides haptic pulses when an interval transitions or finishes. |
| `android.permission.WAKE_LOCK` | Keeps the screen on during an active workout or productivity session according to your in-app settings. |

Pace does **not** request access to your location, camera, microphone, contacts, storage files, or device identifiers.

---

### 3. Data Retention and Deletion
Since all configuration data and routines are stored locally on your device:
- You can clear your data at any time through Android System Settings: `Settings > Apps > Pace > Storage > Clear Data`.
- Uninstalling the application immediately and permanently removes all locally stored preferences and routines.

---

### 4. Children’s Privacy
Pace does not target children under the age of 13 and does not collect any personal information from anyone.

---

### 5. Policy Updates
Any future updates to this Privacy Policy will be reflected in this document with an updated revision date.

---

### 6. Contact Us
If you have any questions or feedback regarding this Privacy Policy or the app, please open an issue on GitHub:
- **Repository:** [https://github.com/simonbuechi/pace-android](https://github.com/simonbuechi/pace-android)
- **Developer:** Simon Büchi
