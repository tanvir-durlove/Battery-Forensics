# Battery Forensics (Android)

**Every conclusion traced to evidence.**  
Battery Forensics is an evidence-based Android battery diagnostic application built with Kotlin, Jetpack Compose (Material 3), and Room.

---

## 📲 How to Download the APK from GitHub

Whenever you push this repository to GitHub, the included GitHub Actions workflow (`.github/workflows/android-build.yml`) automatically builds an installable Android APK (`Battery-Forensics.apk`) and makes it available in **two ways**:

### Option 1: Direct 1-Tap Download on Your Android Phone (Recommended)
1. Open your GitHub repository in your phone's browser.
2. Tap **Releases** (on the repository home page).
3. Under **Battery Forensics — Latest APK Build (`latest-apk`)**, tap **`Battery-Forensics.apk`** to download and install directly (no `.zip` extraction needed).

### Option 2: Download from the GitHub Actions Tab
1. Open your GitHub repository and click the **Actions** tab at the top.
2. Click the latest **Build & Download Android APK** workflow run (or click **Run workflow** to trigger a fresh build manually).
3. Scroll down to the **Artifacts** section at the bottom of the run page and click **`Battery-Forensics-APK`** to download.

---

## 🔍 Key Features
- **4-Step Textured SVG Onboarding**: Introduces overnight drain comparisons, the 4-step evidence chain, honest telemetry labeling (`Measured`, `Estimated`, `Unavailable`), and 100% on-device privacy.
- **True Battery Health & Calibration**: Calibrates across 3–5 charge sessions, smooths the last 5–10 readings, and caps health & capacity at 100% of factory design capacity.
- **Smart Custom Cycle Counter**: Automatically tracks accumulated charge (`mAh`) and increments cycles when hardware cycle counts are hidden by the device manufacturer.
- **38°C Live Thermal Guard**: Highlights temperatures above `38.0°C` and warns when heat may impact performance or charging speed.
- **Drain Detective & Contextual `(i)` Guides**: Breaks down active vs. standby drain (`%/hr`), app wakeups, and charging taper behavior, with subtle `(i)` icons explaining what every statistic measured.
