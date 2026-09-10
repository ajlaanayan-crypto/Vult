# 🛡️ Vult — High-Security Android App Blocker & Vault

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

**Vult** is an advanced, privacy-first Android application blocker designed to safeguard digital well-being and enforce device discipline. It intercepts restricted apps in real time and challenges the user with an alphanumeric passkey overlay, backed by deep system-level anti-tamper protections.

---

## ✨ Features

- **⚡ Real-Time App Interception:** Leverages background accessibility monitoring to instantly detect and halt foreground launches of vaulted applications.
- **🔐 Alphanumeric Security Overlay:** Draws a persistent system-level window (`SYSTEM_ALERT_WINDOW`) equipped with a secure alphanumeric keypad to verify authorization before access is granted.
- **🛡️ Anti-Tamper & Device Admin Protection:** Integrates Android's `DevicePolicyManager` as an active Device Administrator to prevent unauthorized uninstallation or circumvention.
- **🎨 Material 3 Dashboard:** Modern, sleek interface built with Jetpack Compose featuring edge-to-edge aesthetics, adaptive layouts, and real-time app search & toggle controls.
- **📦 Privacy-First Local Architecture:** All configurations, blocked lists, and passkey credentials are encrypted and stored entirely on-device using Room Database and Jetpack DataStore.

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3
- **Navigation:** Jetpack Navigation 3 (`androidx.navigation3`)
- **State & Concurrency:** Kotlin Coroutines, StateFlow / SharedFlow, AndroidX Lifecycle ViewModel
- **Local Persistence:** Room Database (Blocked Apps list), Jetpack DataStore Preferences (Passkey & Settings)
- **System Services & APIs:**
  - `AccessibilityService` (`VultMonitoringService`): Monitors foreground window changes
  - `SystemAlertWindow` (`SecurityOverlayService`): Renders full-screen security lock layer
  - `DevicePolicyManager` (`VultAdminReceiver`): Enforces anti-uninstall safeguards

---

## 🔒 Permissions & Security Model

To deliver seamless app blocking and tamper-resistance, Vult utilizes the following Android system permissions:

| Permission | Purpose |
| :--- | :--- |
| `BIND_ACCESSIBILITY_SERVICE` | Detects when targeted apps are launched in the foreground |
| `SYSTEM_ALERT_WINDOW` | Displays the security challenge overlay above blocked applications |
| `PACKAGE_USAGE_STATS` | Evaluates foreground app state and usage diagnostics |
| `BIND_DEVICE_ADMIN` | Protects the application against unintended removal |
| `QUERY_ALL_PACKAGES` | Allows users to view and select installed applications to vault |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio:** Ladybug (2024.2+) or later
- **JDK:** Version 17 or 21
- **Minimum SDK:** Android 15 (API level 35)
- **Target SDK:** Android 16 / 15 (API level 37)

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ajlaanayan-crypto/Vult.git
   cd Vult
   ```

2. **Open in Android Studio:**
   - Open Android Studio and choose **Open Project**.
   - Select the `Vult` project root folder and allow Gradle sync to complete.

3. **Build and Run:**
   - Connect a compatible Android device or emulator (API 35+).
   - Press **Run ▶** in Android Studio or build via CLI:
     ```bash
     ./gradlew assembleDebug
     ```

4. **Grant Permissions:**
   - Upon launching Vult, open the **Settings** section.
   - Follow prompts to enable **Accessibility Service**, **Overlay (Display over other apps)**, and **Device Administrator**.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 👤 Author

**Mohammad Ayan**
- GitHub: [@ajlaanayan-crypto](https://github.com/ajlaanayan-crypto)
- Email: [ajlaan.ayan@gmail.com](mailto:ajlaan.ayan@gmail.com)
