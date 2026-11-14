# Perennial — App Audit

**Repo:** `JosephOnifade1/spout_app` · **Branch:** `arena/b4f66e06-spout-app` · **Commit:** `1eac4f2`
**Audit date:** 2026-11-14
**Surface audited:** 6,672 lines of Kotlin across 39 files (Room + Firestore data layer, Compose UI, Auth, RevenueCat, WorkManager reminders), plus Gradle/Manifest/CI config.

### How this was audited (read this first)

This is a **static audit** — careful code review, dependency/API verification against upstream sources, and config analysis. It is **not** a build-and-run audit: the sandbox has no JDK, no Android SDK, and no access to Maven/Google repos, so I could not compile, run, or exercise the app. The project also ships **no Gradle wrapper**, so `./gradlew` doesn't exist. Every finding below is traceable to a specific file:line; anything I could not confirm is marked *(unverified)*.

---

## TL;DR scorecard

| Area | Verdict | One-line summary |
|---|---|---|
| UI / design system | 🟢 **Strong** | 16 screens, coherent brand tokens, dark mode, consistent Compose implementation |
| Auth | 🟢 **Mostly working** | Email/password flow is genuinely complete; Google Sign-In wired via CredentialManager but blocked by config |
| Data layer (Room) | 🟡 **Works, leaky** | Local persistence works; no migration strategy, orphaned rows, dead `User` entity, no per-user wipe |
| Cloud sync (Firestore) | 🟠 **Risky** | Dual-writes everywhere, no rules in repo, one write can silently downgrade a paying user |
| Streaks / growth model | 🔴 **Broken** | `growthPoints` is never incremented; streaks never decay; one status string doesn't map to its enum |
| Reminders / notifications | 🟠 **Half-working** | Full WorkManager plumbing, but seeded habits get no reminders, frequency is ignored, timing is inexact |
| Monetization (RevenueCat) | 🟠 **Wired, unsafe** | Real SDK integration, but a test key, client-trusted tier, and a paywall dead-end when offerings fail |
| Data export | 🔴 **Fake** | Exports a hardcoded JSON object |
| Notifications inbox | 🔴 **Fake** | Two hardcoded rows |
| Build & release | 🔴 **Broken out of the box** | No wrapper, missing signing keystores, test-facing package mismatch |
| Tests | 🔴 **Non-existent in practice** | 4 files; one fails by construction, none cover streak/check-in/tier logic |

---

## 1. What's working

These are complete, coherent implementations — not stubs. (Verified by reading; runtime behavior unverified.)

**Architecture & skeleton**
- Clean-ish layering despite no DI: `Models.kt` → `PerennialDatabase` (DAO) → `HabitRepository` interface → `HabitRepositoryImpl` → Compose screens. Icons/colors/typography are centralized (`AppIcons.kt`, `Color.kt`, `Typography.kt`, `Theme.kt`).
- Room database with 3 entities (`habits`, `check_ins`, `milestones`), `@TypeConverters` for `customDays`, and a proper singleton builder.
- Navigation graph with 16 routes, typed-ish route constants, bottom-bar visibility rules, `launchSingleTop`/`saveState`/`restoreState` tab behavior.

**Authentication — the best-built part of the app**
- Email/password **sign-up, sign-in, and password reset** all implemented with per-field validation, duplicate-account / weak-password / invalid-credential / network error mapping, loading states, and a Firestore user-document bootstrap (`AuthScreen.kt:278-500`). This is production-quality.
- Google Sign-In via `CredentialManager` + `GetGoogleIdOption`, with a `default_web_client_id` fallback and a friendly guard that rejects the placeholder client ID (`AuthScreen.kt:49-76`).
- `AuthStateListener` drives navigation: signed-out → Auth, signed-in → Today, and it re-runs default-seed + subscription sync on user change (`AppNavigation.kt:37-83`).

**Check-in & streak engine**
- `toggleCheckIn` supports check *and* undo, recomputes streaks for the current user only, writes to both Room and Firestore, and emits a `Milestone` at 7 / 30 / 100 (`HabitRepositoryImpl.kt:290-380`).
- The streak math actually respects habit frequency and habit creation date (`isScheduled`, `getPreviousScheduledDate`, `getStreakAt`) — genuinely non-trivial and correct in structure.
- Milestone celebration modal + share flow wired end-to-end (`AppNavigation.kt:85-94`).

**Free-tier gating**
- `canAddHabit()` combines habit count with subscription tier; the 3-habit cap is enforced from Today, Garden, and un-archive paths, each routing to the paywall.

**RevenueCat**
- SDK configured in `Application`, `logIn`/`logOut` tied to the Firebase UID, offerings → purchase → customer-info → Firestore tier propagation, plus the real `CustomerCenter` screen. Entitlement key `"Perennial Pro"`
  The *integration shape* is right (the trust model is not — see §5).

**Reminders plumbing**
- `WorkManager` one-time worker with unique work per habit, notification channel creation, POST_NOTIFICATIONS permission check at post time, self-rescheduling on fire, BOOT_COMPLETED receiver, rotating voice/text copy, and TTS playback when the notification is tapped (`notifications/*.kt`, `MainActivity.kt:51-76`).

**Screens that are visually and functionally complete**
- Onboarding (3-page pager + permission rationale), Today (items, check-in button, greeting by time-of-day), Garden (drag-to-reorder via `reorderable` — I verified the API exists in 2.4.3 — plus 4 sort modes and weekly summary), Milestones timeline, Habit detail growth bed, Add/Edit (icon, color, frequency incl. custom days, reminder toggle, time picker), Archived (restore + confirm-delete), Settings, Account, Paywall, Share (Compose `GraphicsLayer` capture → gallery/ShareSheet; I confirmed `rememberGraphicsLayer` is **stable** in Compose 1.7, so no opt-in error).

---

## 2. Blockers — it can't be built or shipped as-is

| # | Issue | Evidence |
|---|---|---|
| B1 | **No Gradle wrapper.** No `gradlew`, no `gradle/wrapper/*.jar`. The project can only be built by whatever Gradle an IDE happens to inject. | `gradle/` contains only `libs.versions.toml` |
| B2 | **Debug signing points at a keystore that doesn't exist in the repo.** `storeFile = file("${rootDir}/debug.keystore")`, and `.gitignore` excludes `debug.keystore`. AGP fails at the signing task → `assembleDebug` fails on a fresh clone. | `app/build.gradle.kts:33-38`, `.gitignore` |
| B3 | **Release signing likewise.** Defaults to `${rootDir}/my-upload-key.jks` (absent) with `storePassword`/`keyPassword` from unset env vars → `assembleRelease` fails. README step 5 literally tells you to delete this line. | `app/build.gradle.kts:26-32`, `README.md` |
| B4 | **Google Sign-In will fail on any machine but the original.** `google-services.json` pins `certificate_hash: b931fb9d…`, which belongs to a keystore that is not in the repo (and can't be, per B2). Until each dev/CI adds their own SHA-1 in the Firebase console, Google sign-in returns `DEVELOPER_ERROR`/12500. | `app/google-services.json`, `AuthScreen.kt:49-76` |
| B5 | **Production builds ship the sandbox billing key.** `.env` is gitignored, so the Secrets plugin falls back to `.env.example`, whose `REVENUECAT_API_KEY` is `test_CXTzfigsVcjlbbmWiBaIGeCDeBB` — a **test** key. Unless someone sets `.env` in the release environment, real builds sell fake purchases. | `.env.example:6`, `app/build.gradle.kts:65-68` |
| B6 | **Instrumented test fails by construction.** It asserts `packageName == "com.example"` while `applicationId = "com.perennialapp.android"`. | `ExampleInstrumentedTest.kt:20` vs `app/build.gradle.kts:18` |

> Also worth knowing: `namespace = "com.example"` and every source file lives under `com.example.*` while the app ID is `com.perennialapp.android`. It works, but it's a permanent wart (and the reason B6 exists).

---

## 3. Functional bugs — the core loop is wrong in places

### 3.1 `growthPoints` is never incremented — it is always `0`

`growthPoints` is **read** in four places and **written** in exactly zero. `toggleCheckIn` updates `currentStreak` and `longestStreak` but never touches `growthPoints`.

Consequences:
- **Milestones screen**: progress bars read `habit.growthPoints / nextTier` → permanently `0/10` (`MilestonesScreen.kt:50-55, 302-318`). "In progress" milestones never advance.
- **Garden "sort by growth"**: a no-op sort (`GardenScreen.kt:110`).
- **Share card**: `streak = habit.growthPoints` → every share image says **0** (`AppNavigation.kt:374`).

### 3.2 Streaks never decay

`currentStreak` is only recomputed inside `toggleCheckIn`. There is no daily reconciliation, no "last check-in" comparison at read time. Miss five days and the UI still reports the old streak (`HabitDetailScreen.kt:129`, `GardenScreen.kt:210-213`). For a streak-based habit app, this is the headline defect.

### 3.3 A growth stage silently renders as the wrong stage

Stored statuses are `"seed" | "perennial" | "bud" | "bloom"`, but the enum is `SEED, SPROUT, BUD, BLOOM, …`. `HabitDetailScreen.kt:106-112` does `GrowthStatus.valueOf(checkIn.status.uppercase())`, so `"perennial"` → `"PERENNIAL"` → **throws** → caught → falls back to `SEED`. Days 2–3 of a streak display as a seed instead of a sprout. (`Models.kt:77-85` names the constant `SPROUT` with value `"perennial"` — the mapping is lossy by design and breaks the round-trip.)

### 3.4 Editing a habit erases its target text

`AddEditHabitScreen` builds a fresh `Habit(...)` on save and **never passes `targetText`**, and the form has no field for it. Editing "Drink Water — 6 glasses" wipes "6 glasses" (`AddEditHabitScreen.kt:81-93`). `targetText` is only ever set by the seeded defaults.

### 3.5 The time picker misreads 24-hour times and silently moves reminders

`TimeSelectionDialog` splits `"07:30"` on `" "`/`":"` → `parts = ["07","30"]` → `amPm` defaults to **"PM"** (`NotificationTimesScreen.kt:96-99`). A default habit stored as `07:30` opens the picker showing **7:30 PM**; confirming saves `07:30 PM`. A 7:30 AM reminder becomes 7:30 PM. (Compounding it: `Habit` defaults to 24-hour `"07:30"` while the Add/Edit form writes 12-hour `"07:30 PM"` — two formats in one column.)

### 3.6 The three seeded habits never get a reminder

Defaults are seeded by inserting straight into the DAO (`HabitRepositoryImpl.kt:457-459`) — bypassing `saveHabit`, which is the only place that calls `ReminderScheduler.scheduleReminder`. `rescheduleAllReminders` is invoked **only** from `BootReceiver`. Net effect: a brand-new user with 3 habits and reminders enabled receives **nothing**, until they open and re-save each habit.

### 3.7 Habit frequency is decorative

- `TodayScreen` lists every non-archived habit every day, ignoring `frequency`/`customDays` (`TodayScreen.kt:48-50, 135-145`).
- `toggleCheckIn` doesn't check `isScheduled`, so a Weekdays/Custom habit can be checked in on a rest day.
- `ReminderScheduler` ignores frequency entirely → a Mon/Wed/Fri habit reminds you 7 days a week.
- Only the streak calculation honors frequency.

### 3.8 Deleting a habit orphans its check-ins

`deleteHabitPermanently` deletes the habit + milestones locally and cleans Firestore, but there is **no DAO method to delete local check-ins** — the code comment admits it (`HabitRepositoryImpl.kt:562-566`; `PerennialDatabase.kt:30-40`). Those rows survive, and `WeeklySummaryCard` counts distinct dates across *all* check-ins, so "You showed up X of 7 days" inflates after deletions (`GardenScreen.kt:311-313`).

### 3.9 An offline sign-in can wipe a paying user's data

`initializeDefaultHabits` decides "first time user" from whether the Firestore read succeeded. If the read fails (offline), it treats the user as brand-new and calls `userDocRef.set(initialUserData)` **without** `SetOptions.merge()` — replacing the whole document: `subscriptionTier` → `"free"`, `display_name` → `"Friend"`, `theme` reset (`HabitRepositoryImpl.kt:386-425`). Same non-merge `.set()` appears in `updateSubscriptionFromCustomerInfo`'s failure fallback (`:196-203`), whereas `PerennialApplication.kt:38` correctly uses merge.

### 3.10 Milestone timeline sorts alphabetically

`dateReached` is stored as a formatted string (`"MMM d, yyyy"`) and the DAO does `ORDER BY dateReached DESC` (`PerennialDatabase.kt:42`). Lexicographic order ≠ chronological. Also the Milestones screen labels `milestone.tier` (a streak of 7/30/100) as "growth points", and its progress bars are keyed off the same broken `growthPoints`.

### 3.11 Smaller data-model issues

- `User` is annotated `@Entity(tableName = "users")` but is **not registered** in `@Database(entities = […])` — dead model, no `users` table, no DAO (`Models.kt:10-19`, `PerennialDatabase.kt:52`).
- Duplicated snake_case *and* camelCase copies of every field (`subscription_tier` **and** `subscriptionTier`, `display_name` **and** `displayName`, …) scattered across four writers. It works only because readers try both.
- DB `version = 1`, `exportSchema = false`, no migration and no destructive fallback → the first schema change crashes existing installs.
- `Converters.fromString` is declared non-null but `Gson().fromJson` can return `null` for a malformed/null blob (`Models.kt:63-75`).
- New habits all get `sortOrder = 0`, so manual ordering starts as a pile of ties.

---

## 4. Features that are stubs or placeholders

| Feature | Reality | Evidence |
|---|---|---|
| **Data export** | Builds a `JSONArray` containing literally `{"habit":"Drink Water","growth_points":12}` and shares it as `sprout_export.json`. No real data is exported. | `SettingsScreen.kt:354-368` |
| **Notifications inbox** | Two hardcoded rows ("It's time to water your habit: Drink Water", "You reached a 7 day streak on Meditate!") with fake timestamps. No store, no read/sent notifications. | `NotificationsScreen.kt:31-37` |
| **Habit detail overflow menu** | `IconButton(onClick = { /* TODO */ })` — renders "⋮" and does nothing. | `HabitDetailScreen.kt:51` |
| **Apple Sign-In** | Rendered button → `Toast("Apple Sign-In is coming soon!")`. | `AuthScreen.kt:158-160` |
| **Paywall when offerings fail** | If `getOfferings()` returns `null` (not configured, network, test key), the CTA is permanently `enabled = false` with a hardcoded `$79.99/yr` fallback price and **no error message**. The user is stuck on a dead screen with no explanation. | `PaywallScreen.kt:57-63, 180-212` |
| **Purchase failures** | `result.isSuccess` false is silently ignored — no toast, no state, no retry. | `PaywallScreen.kt:182-191` |
| **Contextual paywall reason** | `paywallMessage` is set at 3 call sites and **never rendered** anywhere, so users always see generic copy. | `AppNavigation.kt:35, 255, 329` |
| **Sign-out navigation** | `popUpTo(0) { inclusive = true }` targets destination ID `0`, which isn't in the graph. Modern Navigation ignores unknown destinations, so the previous screens (Today/Settings) can stay on the back stack behind the Auth screen after sign-out. *(verify on-device)* | `AppNavigation.kt:56-61` |
| **Settings "simulate upgrade"** | The dev toggle was removed from the UI, but `onToggleSubscription` is still threaded through `AppNavigation.kt:312` → `SettingsScreen.kt:44` and never used. Dead path that still contains a `"plus"`/`"free"` writer. | `SettingsScreen.kt:44` |
| **Gemini capability** | `metadata.json` declares `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` and the README instructs you to set `GEMINI_API_KEY`, but there is **zero** Gemini code and `firebase-ai` is commented out. Dead config + dead README step. | `metadata.json`, `README.md:14`, `app/build.gradle.kts:110` |

---

## 5. Security, privacy & billing trust

| # | Issue | Severity | Detail |
|---|---|---|---|
| S1 | **Subscription tier is client-authoritative** | High | `canAddHabit()` gates Pro features on `subscriptionTier`, which the client itself writes: `updateSubscriptionTier()` calls Firestore `.update("subscriptionTier", "plus")` (`HabitRepositoryImpl.kt:119-126`), and `updateSubscriptionFromCustomerInfo` writes it too. There is no server validation. With permissive default rules (`allow write: if request.auth.uid == userId` — the usual copy-paste), **any user can grant themselves Pro** with a single API call. Pro status must come from a RevenueCat webhook / server, and the field must be `allow write: if false` from clients. |
| S2 | **No Firestore security rules or `firebase.json` in the repo** | High | Habit data (`habits`, `check_ins`, `milestones`, `users`) is written directly from the client across 4 collections. Without committed rules there is no reviewable access control, and the codebase's own dual `snake_case`/`camelCase` writes imply rules have never been hardened. Cannot verify current state — it lives only in the console. |
| S3 | **App Check is commented out** | Medium | `firebase-appcheck-recaptcha` is disabled, and `google-services.json` ships a live API key. Nothing prevents scripted abuse of Auth/Firestore against this project. |
| S4 | **Test-mode RevenueCat key in a public repo** | Medium | Anyone can initialize the SDK against that test project. Coupled with S1, sandbox purchases are trivially spoofable. |
| S5 | **`allowBackup="true"` with full backup rules** | Medium | Habit/check-in data (behavioural, health-adjacent) is backed up to Google Drive with no encryption guard and no opt-out for the user. |
| S6 | **`google-services.json` committed** | Low | Standard practice for Android, but the API key must be restricted to `com.perennialapp.android` + the release SHA-1, otherwise it's a free quota target. |
| S7 | **Legacy storage permissions declared, never used** | Low | `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` are declared with no `maxSdkVersion` and no runtime request anywhere; `ShareScreen.saveBitmapToGallery` writes via MediaStore on API 29+ and will **fail silently** on API 24–28 (`ShareScreen.kt:291-320`). |
| S8 | **All local data survives sign-out** | Low | `onUserChanged(null)` resets in-memory state but Room is never wiped; the previous account's rows stay on disk, and filtering is by `userId` only. On a shared device that's a privacy gap. |
| S9 | **Release build is unminified** | Low | `isMinifyEnabled = false` with no ProGuard rules — bigger APK, trivially decompilable, and the whole client-side tier logic is readable. |

---

## 6. Code quality, tests & build hygiene

**Tests — the safety net doesn't exist**

| File | State |
|---|---|
| `ExampleUnitTest.kt` | `assertEquals(4, 2+2)`. Template. |
| `ExampleRobolectricTest.kt` | Tests color-hex mapping (passes) and **re-implements** `isScheduled` locally instead of calling production code — so the scheduling "test" would pass even if `HabitRepositoryImpl.isScheduled` were deleted. |
| `GreetingScreenshotTest.kt` | Renders `Text("Perennial")`. Screenshot smoke test only. |
| `ExampleInstrumentedTest.kt` | **Fails** (B6). |

Zero coverage for the parts that are actually broken: `toggleCheckIn`, streak decay, `growthPoints`, tier resolution, time parsing, Firestore↔Room reconciliation, `deleteHabitPermanently`. `@Config(sdk = [36])` against `compileSdk = 35` also makes the Robolectric setup fragile (needs a downloadable SDK 36 runtime jar).

**Dependencies**
- 5 unused/unwired libraries ship in the APK: `retrofit`, `converter-moshi`, `okhttp`, `logging-interceptor`, `moshi-kotlin` — there is not a single Retrofit/OkHttp call in the codebase. `coil-compose`, `accompanist-permissions`, `camera-*`, `play-services-location`, `firebase-ai`, `datastore`, `appcheck` are commented out but still declared in `libs.versions.toml`.
- `android.enableJetifier=true` — unnecessary for a modern AndroidX-only project; slows every build.
- The `reorderable` API used (`rememberReorderableLazyColumnState`) **does** exist in 2.4.3 — verified against upstream, so this is not a compile issue.

**Repo hygiene**
- No `gradlew`/`gradle-wrapper.properties` (B1); no `.github/workflows` CI; no `lint.xml`/detekt/ktlint.
- A 1200×475 marketing JPG mockup (`add_habit_mockup_1784421858749.jpg`) ships inside `res/drawable` — dead weight in the APK.
- Two shell scripts + a Python script (`generate_icons.sh`, `generate_filled_icons.sh`, `update_today_screen.py`) sit at repo root; accidental/one-off tooling.
- `README.md` is the untouched AI Studio template: it tells you to set `GEMINI_API_KEY` (unused) and to delete a line from `build.gradle.kts` (a workaround for B3) instead of configuring signing properly.
- Single squashed commit (`chore: configure firebase and notification setup`) — no history to learn from.

**Compose/architecture**
- No ViewModels below the Activity: `HabitRepositoryImpl` is constructed inside a composable via `remember` (`AppNavigation.kt:27-28`), so transient state (paywall message, milestone modal, dialogs) lives in composition and is lost on config change; every screen re-collects flows independently.
- `HabitDetailScreen` and `ShareScreen` render **nothing** if the habit isn't found — blank screens instead of a fallback (`AppNavigation.kt:222-247, 368-378`).
- Unused parameters threaded through nearly every screen (`onNavigateToTab` in Milestones/Garden, unused `BottomNavBar` imports) — the global Scaffold already owns the bottom bar.
- **All UI copy is hardcoded** in Kotlin; `strings.xml` contains only `app_name`, despite `supportsRtl="true"`. Zero localization readiness.
- Accessibility: icon-only buttons with `contentDescription = null`, and "←"/"⋮" text glyphs used as controls. Version string says `v1.2.0` while `versionName = "1.0"`; Settings' "Notification times" subtitle is hardcoded `"7:30 AM, 7:30 PM"` regardless of the actual times.
- `TodayScreen` — the primary screen — shows **no streak, no progress, no stage**. The app's core metric is invisible where users spend their time.

---

## 7. Suggested fix order

**P0 — get it building and installable (hours)**
1. Add the Gradle wrapper (`gradle wrapper --gradle-version 8.9`) and commit it.
2. Replace the two hardcoded `signingConfigs` with `~/.android/debug.keystore`/env-var-driven config, or drop `signingConfig` from `debug` entirely; document keystore setup.
3. Commit `firestore.rules` + `firebase.json`; disable client writes to `subscriptionTier` (S1/S2).
4. Register the debug + release SHA-1 in Firebase for Google Sign-In (B4).
5. Move the RevenueCat key out of `.env.example` into a real secret path per build type (B5).
6. Fix `ExampleInstrumentedTest`'s expected package (B6).

**P1 — make the product honest (days)**
7. Increment `growthPoints` on check-in (+ undo), or delete the field and re-key Milestones/Share/Garden sort off `currentStreak`.
8. Recompute/expire streaks at read time (store `lastCheckInDate`, derive the streak instead of caching it).
9. Fix `"perennial"` → `SPROUT` mapping (or make `checkIn.status` the enum name).
10. Preserve `targetText` in Add/Edit (add a field).
11. Normalize `reminderTime` to one format end-to-end and fix the AM/PM default.
12. Schedule reminders for seeded habits (`saveHabit` or an explicit `rescheduleAllReminders()` after seeding/login).
13. Enforce `frequency` in Today, in `toggleCheckIn`, and in `ReminderScheduler`.
14. Delete local check-ins in `deleteHabitPermanently` (add the DAO method).
15. Make user-doc writes merge-safe (`SetOptions.merge()` everywhere).
16. Real data export; real notifications store (or hide both from Settings until they work).

**P2 — ship quality (1–2 weeks)**
17. Paywall: show an error state + retry when offerings fail, surface purchase errors, render `paywallMessage`, replace `context as Activity` with a safe cast.
18. Verify/replace `popUpTo(0)` on sign-out; clear local data on sign-out.
19. Move to ViewModels + a small DI container; add unit tests for check-in/streak/tier/time-parsing and a Room migration test.
20. Extract strings, add accessibility labels, add streak/progress to Today.
21. Remove dead deps, dead entity, dead parameters, mockup JPG, root scripts; disable Jetifier; add CI (`assembleDebug` + `testDebugUnitTest` + lint).

---

## 8. What I could not verify

Stated plainly, so you know the edges of this report:

- **Compilation.** No JDK/Android SDK here, and Maven/Google Maven are outside this sandbox's network allowlist. I checked the riskiest API guesses against upstream (reorderable 2.4.3's `rememberReorderableLazyColumnState` exists; Compose 1.7's `rememberGraphicsLayer`/`toImageBitmap` are stable, not opt-in) — but a real `./gradlew assembleDebug` is still the only proof.
- **Runtime behavior.** No emulator, no device.
- **Firestore security rules, Firebase Auth provider config, RevenueCat product/offering/entitlement setup, Play Console state.** None of this is in the repo; the S1/S2 risks are inferences from client code, not from observed rules.
- **Whether `popUpTo(0)` clears the back stack** in the exact Navigation 2.8.5 version — flagged as *(verify on-device)* rather than asserted.
- **Play policy** (targetSdk 35 is fine for new submissions today; the legacy storage permissions and `allowBackup` will likely get comments).

---

### Bottom line

The **UI and auth are genuinely good** — this looks like a real product, not a demo. But the thing that makes it a habit tracker (streaks and growth) is **half-wired**: `growthPoints` never moves, streaks never expire, one stage renders as the wrong plant, frequency is ignored, and the three habits every new user starts with never notify them. Two advertised features (export, notification inbox) are visibly fake. And it doesn't build from a clean clone. The good news: almost every item above is a small, localized fix in `HabitRepositoryImpl.kt` and a handful of screens — the hard architectural work is already done.
