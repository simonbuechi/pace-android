# Privacy Policy for Pace Amigo

**Last Updated:** October 2, 2026  
**Developer:** Simon Büchi  
**Contact / Project Site:** [https://github.com/simonbuechi/sport-android](https://github.com/simonbuechi/sport-android)  
**Application ID:** `ch.simibu.pace`

---

Pace Amigo ("we", "our", or "us") respects your privacy. This Privacy Policy outlines our strict privacy-first practices regarding data collection, device permissions, and local data storage.

---

### 1. Zero Data Collection & Privacy by Design
* **No Personal Data Collected:** Pace Amigo does not collect, record, track, transmit, sell, or share any personal identifiable information (PII), device identifiers, location data, or usage diagnostics.
* **No Tracking or Third-Party SDKs:** The application contains zero advertising networks, zero third-party analytics libraries (such as Google Analytics or Firebase), and zero tracking cookies or telemetry.
* **No Account Required:** You do not need to register, sign in, or provide an email address to use any feature of the application.
* **100% Local Storage:** All routines, workout interval presets, sound and theme preferences, and routine completion history are stored strictly locally on your device via Android Jetpack DataStore / SharedPreferences. No data is ever transmitted to external servers or cloud services.

---

### 2. Device Permissions & Purpose

Pace Amigo only requests the minimal set of Android system permissions required to provide its core interval timer capabilities:

| Permission | Purpose & Justification |
| :--- | :--- |
| `android.permission.FOREGROUND_SERVICE` & `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Keeps the interval workout/productivity timer running accurately and reliably when the device screen is off, locked, or when switching between applications. |
| `android.permission.POST_NOTIFICATIONS` | Displays a persistent notification with the real-time interval countdown and playback controls (Pause, Resume, Skip, Stop) while a session is active. |
| `android.permission.VIBRATE` | Triggers subtle tactile haptic vibrations during countdowns and interval phase changes (can be disabled in Settings). |
| `android.permission.WAKE_LOCK` | Keeps the display illuminated while a timer is active when the user enables the "Keep Screen Awake" setting. |

Pace Amigo **never** requests sensitive permissions such as Camera, Microphone, GPS Location, Contacts, SMS, Phone State, or External File Storage.

---

### 3. Data Retention and Deletion
Because all data is stored exclusively on your device:
* **Manual Deletion:** You can reset all application data at any time via Android system settings:  
  `Settings > Apps > Pace Amigo > Storage & cache > Clear storage`.
* **Routine Log Clearing:** You can clear routine logs at any time directly inside the app.
* **Uninstallation:** Uninstalling Pace Amigo permanently removes all locally stored configuration, routines, and log history.

---

### 4. Children’s Privacy
Pace Amigo does not address or target anyone under the age of 13, nor does it collect any personal information from any user of any age. The app is safe and family-friendly.

---

### 5. Google Play Policy Compliance
This Privacy Policy complies with Google Play Developer Program Policies, including the User Data policy, Foreground Service policy, and EU General Data Protection Regulation (GDPR) standards.

---

### 6. Changes to This Privacy Policy
If this Privacy Policy is updated in future releases, the changes will be published in this document along with an updated revision date.

---

### 7. Contact Us
If you have questions, feedback, or suggestions regarding this Privacy Policy or Pace Amigo, please open an issue or pull request on GitHub:
* **Project Repository:** [https://github.com/simonbuechi/sport-android](https://github.com/simonbuechi/sport-android)
* **Author:** Simon Büchi © 2026

