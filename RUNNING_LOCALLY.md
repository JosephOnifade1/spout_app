# Running Perennial locally (and where the build artifacts are)

Two ways to see the app: **install the APK CI already built** (fastest), or **build it yourself**
(needed if you want to change code and see it immediately).

---

## 1. The `build-output` artifact

Every push runs `.github/workflows/screenshots.yml`, which builds the app and attaches artifacts
to that run.

**Where:** `https://github.com/JosephOnifade1/spout_app/actions` → pick the newest green run →
scroll to the bottom of the run summary page → **Artifacts**.

| Artifact | Size | What's in it | Retention |
|---|---|---|---|
| `build-output` | ~30 MB | `app-debug.apk` | 14 days |
| `screenshots` | ~2.4 MB | every Roborazzi PNG (`today_*`, `habit_detail_*`, design system) | 30 days |
| `gradle-log` | ~2 KB | Gradle output, for when a build fails | 14 days |

You must be signed in to GitHub to download — artifacts are private to the repo.

### Install it on a phone

1. Download `build-output.zip` and unzip → `app-debug.apk`.
2. Transfer to the device (cable, Drive, or `adb`).
3. Enable **Install unknown apps** for whatever app you're installing from, then tap the APK.
4. Or over USB: `adb install -r app-debug.apk`

**Caveats that matter**

- It is a **debug** build, signed with a throwaway debug keystore generated inside CI. It will
  install fine; it won't be accepted by Play Console. That's expected.
- `BuildConfig` values come from **`.env.example`** in CI, because the real `.env` is gitignored.
  So `GOOGLE_WEB_CLIENT_ID` is a placeholder — **Google sign-in will fail** on this APK. Email
  sign-in and all local habit data (Room, on-device) work normally.
- Two CI-built artifacts can't both be installed over each other if the keystore changes; uninstall
  first if you hit `INSTALL_FAILED_UPDATE_INCOMPATIBLE`.

### The zero-setup option

If you just want to check the design, you don't need to install anything:
**`app/src/test/screenshots/`** in the repo has every rendered screen, committed by CI.

---

## 2. Build and run it yourself

### Prerequisites

| Thing | Version | Notes |
|---|---|---|
| JDK | **17** | AGP 8.7.3 + Gradle 8.9 both require 17 |
| Android Studio | Ladybug or newer | bundles the SDK + emulator |
| Android SDK | **compileSdk 35**, minSdk 24 | Studio will prompt to install |

The Gradle wrapper is committed (it was missing until PR #3), so you do **not** need Gradle
installed — `./gradlew` pulls the right version.

### Steps

```bash
git clone https://github.com/JosephOnifade1/spout_app.git
cd spout_app

# 1. secrets — the build fails without this file
cp .env.example .env

# 2. the debug build type signs with a keystore that is gitignored, so create it once
keytool -genkeypair -v \
  -keystore debug.keystore \
  -storepass android -keypass android \
  -alias androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"

# 3. run
./gradlew :app:installDebug      # device/emulator must be running
```

Or open the folder in Android Studio, let it sync, pick an emulator and press Run.

**Emulator choice:** the screenshots use Robolectric's `Pixel8` qualifier, so a **Pixel 8 /
390×844 dp** emulator will match them closely.

### If something fails

- `Keystore file not found` → step 2 above.
- `secrets.properties not found` / missing `BuildConfig` fields → step 1 above.
- `Unsupported class file major version` → you're on JDK 21/24; set Gradle's JDK to 17
  (Studio: Settings → Build → Build Tools → Gradle → Gradle JDK).
- Nothing appears on the emulator → `adb devices` should list it before `installDebug`.

### Seeing the design system without a device

```bash
./gradlew :app:recordRoborazziDebug
```

renders every screen in `app/src/test/screenshots/` on the JVM via Robolectric — no emulator. This
is exactly what CI does, and it's the fastest loop for UI work.

---

## What is *not* wired up yet

- **Six colour tags** — every plant is Moss art today; `habit.colorTag` is stored but unrendered.
- **Dark mode** — the schemes exist, but there is no night art set, so plants stay daylight paper.
- Shared-element transition from habit card → detail (Phase 6).
