# Privacy Policy for Battery Forensics

**Effective Date:** October 4, 2026  
**Last Updated:** October 4, 2026  
**Application Name:** Battery Forensics  
**Package Name:** `com.nextgen.batteryforensics`  
**Developer Contact:** [durlovetanvir@gmail.com](mailto:durlovetanvir@gmail.com)  

---

## 1. Introduction & Overview

Welcome to **Battery Forensics** ("we", "our", or "the app"). We respect your privacy and are committed to protecting your personal data. This Privacy Policy explains our practices regarding the collection, use, and disclosure of information when you use our Android application available on Google Play.

**Our Core Privacy Principle:**  
Battery Forensics is engineered with a **"Private by Architecture"** design. All diagnostic metrics, battery health records, charging logs, and hardware telemetry remain strictly stored **on your local device**. We do not operate remote servers, do not require user account registration, and do not transmit your device diagnostics to any cloud database.

---

## 2. Google Play Data Safety Summary

| Data Type | Collected | Shared | Storage Location | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Personal Identifiers** (Name, Email, Phone) | **No** | **No** | None | Not collected |
| **User Content** (Photos, Messages, Contacts) | **No** | **No** | None | Not collected |
| **Device Battery & Hardware Telemetry** | Yes (locally only) | **No** | Local SQLite Database on device | Diagnostics & power analysis |
| **App Diagnostics & Crash Logs** | Yes (locally only) | **No** | Local SQLite Database on device | User troubleshooting |
| **Advertising Identifier (AAID)** | Yes (by Google AdMob) | Shared with Google | Google AdMob SDK | Ad display & fraud prevention |

---

## 3. Information We Process on Your Device

Battery Forensics queries Android system APIs to provide real-time battery and power diagnostics. All data collected is stored exclusively in your device's private application sandbox:

1. **Battery State & Health Metrics:**
   - Battery charge level (%), temperature (°C / °F), voltage (mV), and health status.
   - Charging speed, charging technology (USB, AC, Wireless), and charge duration cycles.
   - Calculated battery degradation estimations based on charging delta benchmarks.

2. **Hardware Power Telemetry:**
   - Active CPU frequencies and governor states.
   - System screen-on time vs. screen-off standby power drain.
   - Detected kernel wakelocks and app standby buckets.
   - Connected Wi-Fi, Cellular signal state, and Bluetooth peripheral counts.

3. **User-Generated Diagnostic Tests & Notes:**
   - Custom test benchmarks, benchmark duration, and test notes.
   - User-defined charging session tags and charger profiles.

> **Important:** None of the above data leaves your physical device unless you explicitly choose to export a report via the built-in CSV, JSON, or PDF export feature.

---

## 4. Android Device Permissions & Explanations

The app requests only permissions necessary for battery monitoring and user-initiated alerts. You can grant or revoke these permissions at any time via your device's system settings:

* **`POST_NOTIFICATIONS` (Android 13+):**  
  Used solely to display user-configured battery alerts (e.g., high temperature warning alerts, 80% charge completion notifications, and deep discharge warnings).
* **`BATTERY_STATS` / `PACKAGE_USAGE_STATS` (Optional / Developer Mode):**  
  Used to inspect per-app energy consumption when granted via ADB or system developer access.
* **`READ_PHONE_STATE` (Optional):**  
  Used to correlate radio signal strength (cellular stand-by) with background battery drain. Does not access call logs or contact records.
* **`BLUETOOTH_SCAN` & `BLUETOOTH_CONNECT` (Optional):**  
  Used to count active Bluetooth peripherals and calculate their estimated power draw.
* **`VIBRATE`:**  
  Used to provide haptic feedback when hardware diagnostic tests start or complete.

---

## 5. Third-Party Services & Advertising (Google AdMob)

Battery Forensics displays optional rewarded video advertisements and banner advertisements powered by **Google AdMob**.

Google AdMob may collect and process certain device information in accordance with Google's Privacy Policy, including:
* Android Advertising ID (AAID) / Mobile Ad Identifiers
* IP address (for coarse geographic ad targeting and fraud detection)
* Device model, operating system version, and screen dimensions
* Ad interaction events (views, clicks, rewarded completion signals)

You can manage or reset your Android Advertising ID at any time on your device by navigating to:  
**Settings > Google > Ads > Reset advertising ID / Delete advertising ID**.

For more information on how Google processes ad data, please review:
* [Google Privacy Policy](https://policies.google.com/privacy)
* [How Google uses information from sites or apps that use our services](https://policies.google.com/technologies/partner-sites)

---

## 6. AI Battery Doctor Feature

Battery Forensics includes an **AI Battery Doctor** tool. 
* All prompt generation and diagnostic rule evaluations occur **locally on your device**.
* The generated diagnostic prompts and recommendations are produced using deterministic on-device analysis without sending your battery history to any remote AI server.
* If you choose to copy a generated prompt to paste into external AI services (such as Google Gemini or ChatGPT), you do so voluntarily and under that third-party service's respective privacy policy.

---

## 7. Data Retention, Portability & Deletion Rights

You have complete control over your data:

1. **Data Portability:** You can export your entire diagnostic history, charger logs, and baseline benchmarks at any time in standard **CSV**, **JSON**, or **PDF** format using the built-in Data Management tools.
2. **One-Tap Data Deletion:** You can permanently erase all recorded sessions, charger profiles, timeline events, and test records directly from **Settings > Data Management > Delete All Data**.
3. **App Uninstallation:** Uninstalling the app from your device automatically and permanently removes all stored SQLite database files and shared preferences managed by Battery Forensics.

---

## 8. Children's Privacy

Battery Forensics is a general utility diagnostic tool and is **not directed at children under the age of 13** (or the applicable minimum age in your jurisdiction). We do not knowingly collect personal identifiable information from children. If you believe that a child has provided us with personal information, please contact us at [durlovetanvir@gmail.com](mailto:durlovetanvir@gmail.com) so that we can take appropriate measures.

---

## 9. Security

We take the security of your device data seriously. Because all diagnostic information is stored locally within the protected Android application sandbox, it is inaccessible to other non-privileged applications installed on your device.

---

## 10. Changes to This Privacy Policy

We may update this Privacy Policy from time to time to reflect changes in our app features or regulatory requirements. Any updates will be posted with a revised "Effective Date" at the top of this document. Continued use of Battery Forensics after updates constitutes acceptance of the revised policy.

---

## 11. Contact Us

If you have any questions, feedback, or concerns regarding this Privacy Policy, please contact the developer:

* **Developer Email:** [durlovetanvir@gmail.com](mailto:durlovetanvir@gmail.com)  
* **Application:** Battery Forensics (`com.nextgen.batteryforensics`)  
* **Google Play Store Developer Console ID:** Available on the Google Play Store listing page.
