# Perennial — Phase-by-Phase Fix Plan

Companion to [`AUDIT.md`](AUDIT.md). Every item here maps back to a finding in that report.

**Status:** proposed — not started. Nothing in this plan is implemented yet.

---

## How to use this document

- Work **top to bottom**. Each phase is a shippable unit with its own acceptance criteria; later phases assume earlier ones landed.
- One PR per phase (Phase 1 splits into two — see §Phase 1). Every PR should be independently revertable.
- Each item has an ID (`1.3`), the ×finding from the audit, the files it touches, and a concrete acceptance test. "AC" is the thing that must be demonstrable, not just "code written".
- **Decisions I need from you are in §Decisions (D1–D7).** Phases 1, 2, 3 and 4 are each blocked on one decision. Read those first if you want to redirect me early.

### Verification reality

I cannot compile or run this app in my sandbox (no JDK/Android SDK, no Maven access). That is exactly why **Phase 0 exists and comes first**: it adds a Gradle wrapper plus GitHub Actions CI so every later PR is proven by a real build on a real runner instead of by my reading. Until Phase 0 lands, every fix I write is unverified code.

---

## Overview

| Phase | Theme | Effort | Risk | Depends on | Audit refs |
|---|---|---|---|---|---|
| **0** | Buildable & verifiable baseline | S — 0.5–1 d | Low | — | B1, B2, B3, B6 |
| **1** | Data integrity / core loop | M — 2–3 d | Medium | 0, D1 | §3.1–3.5, §3.8–3.10 |
| **2** | Reminders that actually work | M — 1–2 d | Medium | 0, 1, D2, D3 | §3.5–3.7, §6 |
| **3** | Security & billing trust | M–L — 2–4 d | High | 0, D4 | S1–S6 |
| **4** | Make features honest | M — 1–2 d | Low | 0, D6 | §4 |
| **5** | Monetization UX | S — 0.5–1 d | Low | 3 (soft) | §4 paywall rows |
| **6** | Architecture & quality | L — 4–6 d | Medium | 1, 3 | §6 |
| **7** | Release readiness | S–M — 1–2 d | Low | all | B4, B5, S7–S9 |

**Recommended order:** `0 → 1 → 2 → 5 → 3 → 4 → 6 → 7`
I moved Phase 5 ahead of 3 because the paywall is a one-day fix that stops user-visible breakage, while Phase 3 needs a backend decision and more time. If you're launching soon, pull **3.1 (Firestore rules)** forward into Phase 0 — it's ~2 hours and it's the single most dangerous open door.

---

## Phase 0 — Buildable & verifiable baseline

**Objective:** a clean clone builds, tests run, and CI guards every subsequent PR.

| ID | Task | Files | Notes |
|---|---|---|---|
| 0.1 | **Add Gradle wrapper.** `gradle-wrapper.jar` can be pulled from `github.com/gradle/gradle` tag `v8.9.0` (verified reachable), plus hand-written `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties` pointing at `gradle-8.9-bin.zip`. | `gradle/wrapper/*`, `gradlew*` | AGP 8.7.3 requires Gradle ≥ 8.9. Wrapper jar is version-agnostic. |
| 0.2 | **Fix signing.** Delete the custom `debugConfig` so AGP uses the standard `~/.android/debug.keystore` it creates itself. For release: apply `signingConfigs.release` **only when** `KEYSTORE_PATH`/`STORE_PASSWORD`/`KEY_PASSWORD` are all present, otherwise produce an unsigned release AAB. | `app/build.gradle.kts:23-40` | Removes B2 + B3; no keystore in repo, ever. |
| 0.3 | **Fix instrumented test.** Expected package → `com.perennialapp.android` (or read it from `BuildConfig.APPLICATION_ID`). | `ExampleInstrumentedTest.kt:20` | B6 |
| 0.4 | **GitHub Actions CI:** JDK 17, Gradle cache, `assembleDebug`, `testDebugUnitTest`, `lint`. Fail on any of them. | `.github/workflows/android.yml` | The safety net everything else relies on. |
| 0.5 | **Room `exportSchema = true`** + commit `app/schemas/`. | `PerennialDatabase.kt:52` | Must land *before* Phase 1/6 touch entities, so migrations are reviewable. |
| 0.6 | Update `README.md`: real build instructions, remove the "delete this line" workaround and the unused `GEMINI_API_KEY` step. | `README.md` | §3.3 of audit |

**Acceptance criteria**
- `git clone … && ./gradlew assembleDebug` succeeds with no local keystore setup.
- `./gradlew testDebugUnitTest` passes.
- CI is green on the PR and runs on every later PR.
- `git grep -i "my-upload-key\|debug.keystore"` returns only docs.

**Verification:** CI run URL on the PR.

**Risk:** low. Worst case the wrapper version needs bumping for AGP compatibility — a one-line change.

---

## Phase 1 — Data integrity / core loop

**Objective:** streaks and growth become true. This is the phase that decides whether the app is a habit tracker or a mockup.

**Split into two PRs** so the risky schema/derivation work is separable from the mechanical fixes:
- **1a (PR)** = derivation + points + status (1.1–1.3)
- **1b (PR)** = parsing, preservation, deletion, writes, frequency (1.4–1.8)

| ID | Task | Files | Detail |
|---|---|---|---|
| 1.1 | **Derive streaks instead of trusting a cached column.** Extract the existing private logic into a pure `StreakCalculator` (`isScheduled`, `previousScheduledDate`, `streakEndingOn`). `getHabits()` combines `habits` + `checkIns` flows and computes `currentStreak`/`longestStreak` at read time; the DB column becomes a cache refreshed on write only. | `HabitRepositoryImpl.kt:214-290`, new `domain/StreakCalculator.kt` | Fixes **§3.2** (no decay) by construction — a streak simply doesn't exist if the last expected day wasn't checked in. |
| 1.2 | **Give `growthPoints` real semantics** (per **D1**). Recommended: lifetime check-in count — `+1` on check-in, `−1` on undo — plus a one-time backfill from `check_ins` for existing rows. | `HabitRepositoryImpl.toggleCheckIn` | Fixes **§3.1**: Milestones `x/10`, Garden sort-by-growth, and the share card all start moving. |
| 1.3 | **One canonical growth status.** Add `GrowthStatus.fromValue(String)` that tolerantly accepts `"perennial"` and `"sprout"` (legacy rows), write `"sprout"` for new rows, and use `fromValue` in the UI instead of `valueOf`. | `Models.kt:77-85`, `HabitDetailScreen.kt:103-114` | Fixes **§3.3** — no migration needed, historic rows keep working. |
| 1.4 | **`ReminderTime` value object:** `parse()` tolerant of `"07:30"`, `"7:30 PM"`, `"7:30PM"`; `format12h()`/`format24h()`; canonical storage `HH:mm`. Update Add/Edit picker + NotificationTimes picker to use it. | new `domain/ReminderTime.kt`, `NotificationTimesScreen.kt:95-99`, `AddEditHabitScreen.kt:50`, `ReminderScheduler.kt:61-81` | Fixes **§3.5** — confirming 7:30 AM no longer becomes 7:30 PM. |
| 1.5 | **Never rebuild an entity field-by-field in a form.** Add a `targetText` field to Add/Edit and `save` via `initialHabit.copy(...)` so every untouched field survives. | `AddEditHabitScreen.kt:81-93` | Fixes **§3.4**; also permanently removes this class of bug. |
| 1.6 | **Delete child rows with the parent.** Add `deleteCheckInsByHabitId` (+ Firestore batch) to `deleteHabitPermanently`. | `PerennialDatabase.kt:27-40`, `HabitRepositoryImpl.kt:562-584` | Fixes **§3.8** — weekly summary stops inflating. |
| 1.7 | **Merge-safe writes + offline-safe bootstrap.** `SetOptions.merge()` in both repository writers; replace the "user doc missing ⇒ brand new user ⇒ `set()`" logic with a local `has_seeded` flag. | `HabitRepositoryImpl.kt:196-203, 386-425` | Fixes **§3.9** — an offline sign-in can no longer reset a payer to free or erase their name. |
| 1.8 | **Enforce `frequency`.** Today shows only today's scheduled habits (with a "rest day" state instead of hiding silently); `toggleCheckIn` rejects unscheduled days (per **D7**); reminders follow in Phase 2. | `TodayScreen.kt:48-50`, `HabitRepositoryImpl.kt:290-380` | Fixes **§3.7** (UI half). |

**Acceptance criteria (automated — these are the first real tests in the repo)**
- `StreakCalculatorTest`: daily / weekdays / custom; boundary at `createdAt`; missed day resets; checked-in-today vs not-yet-today.
- `GrowthPointsTest`: +1 per check-in, −1 on undo, never negative.
- `GrowthStatusTest`: `"perennial"` → `SPROUT`, `"sprout"` → `SPROUT`, unknown → `SEED` (no throw).
- `ReminderTimeTest`: all parse/format round-trips, AM/PM edges (12 AM → 00:00, 12 PM → 12:00).

**Acceptance criteria (manual, on device)**
- Check in 3 consecutive days → habit shows **SPROUT**, not seed.
- Skip a day → streak reads 0 on next launch without any check-in action.
- Check in once → Milestones shows `1/10`; Garden "sort by growth" reorders.
- Edit "Drink Water — 6 glasses" → save → target still there.
- Delete a habit → Garden's "You showed up X of 7" drops by that habit's days.
- Turn on airplane mode, sign out/in → tier and display name unchanged.

**Risk:** medium. Deriving streaks changes what every screen reads; keep the DB column as a cache so rollback is possible. Backfill is one-shot and idempotent.

---

## Phase 2 — Reminders that actually work

**Objective:** the feature the app is named around actually fires — on the right days, for every habit.

| ID | Task | Files | Detail |
|---|---|---|---|
| 2.1 | **Schedule reminders for seeded habits.** Route seeding through `saveHabit` (or call `scheduleReminder` per seeded habit) and call `rescheduleAllReminders()` once after login/sync. | `HabitRepositoryImpl.kt:429-460`, `AppNavigation.kt:52-83` | Fixes **§3.6** — new users currently get zero notifications. |
| 2.2 | **Frequency-aware scheduling.** `scheduleReminder` computes the *next* `isScheduled` date at `reminderTime`, not "tomorrow". | `ReminderScheduler.kt:17-47` | Fixes **§3.7** (reminder half). |
| 2.3 | **Notification exactness** (per **D2**): either `AlarmManager.setExactAndAllowWhileIdle` (+ `SCHEDULE_EXACT_ALARM` with graceful fallback), or stay on WorkManager and accept drift — but then document it and stop implying precise times in Settings. | `ReminderScheduler.kt`, `AndroidManifest.xml` | Audit §6 — WorkManager one-shots drift badly under Doze. |
| 2.4 | **Watchdog.** Daily `PeriodicWorkRequest` that re-asserts all schedules (cheap, idempotent, survives OEM task killers). Keep `BootReceiver` as a second net. | `ReminderScheduler.kt`, `BootReceiver.kt` | Robustness, not a bug fix. |
| 2.5 | **Deep link to the right habit.** Drop `FLAG_ACTIVITY_CLEAR_TASK` (it discards the habit id and re-launches cold); navigate to `habit_detail/{id}` from the intent. | `HabitReminderWorker.kt:57-61`, `MainActivity.kt:23-49` | Currently the tap loses context; TTS per **D3**. |
| 2.6 | **Permission re-ask.** If POST_NOTIFICATIONS was declined at onboarding, offer it from Settings → Notification times. | `SettingsScreen.kt`, `NotificationTimesScreen.kt` | Audit §6 accessibility/UX. |

**Acceptance criteria**
- Fresh install → sign up → all 3 seeded habits have scheduled work (`adb shell dumpsys jobscheduler` / WorkManager inspector).
- Mon/Wed/Fri habit produces work only for those days; verify next-3-occurrence math in unit tests.
- Reboot → schedules intact.
- Tap notification → lands on that habit's detail.

**Risk:** medium. AlarmManager exact alarms have Play policy implications (`USE_EXACT_ALARM` needs justification for alarm-clock-style apps; a habit reminder generally qualifies for `SCHEDULE_EXACT_ALARM`). That's why D2 is yours to make.

---

## Phase 3 — Security & billing trust

**Objective:** stop trusting the client for money and data.

| ID | Task | Files | Detail |
|---|---|---|---|
| 3.1 | **Commit `firestore.rules` + `firebase.json`.** Per-user paths only; `users/{uid}` readable by owner, `subscriptionTier`/`subscription_tier` **not client-writable**; habits/check-ins/milestones writable only where `request.auth.uid == resource.data.userId`. Add emulator rule tests. | new `firestore.rules`, `firebase.json`, `firestore-tests/` | Fixes **S1, S2** — currently one Firestore write grants Pro. |
| 3.2 | **Server-authoritative entitlement** (per **D4**): RevenueCat webhook → Cloud Function (or your existing backend) → set `subscriptionTier` with admin credentials. Client drops `updateSubscriptionTier` to a read path. | new `functions/`, `HabitRepositoryImpl.kt:119-126, 191-212` | Closes the bypass permanently; 3.1 alone only makes it harder. |
| 3.3 | **Enable App Check** (Play Integrity in release, debug provider in dev). | `PerennialApplication.kt`, `app/build.gradle.kts:117` | **S3** |
| 3.4 | **Secrets hygiene:** remove the RevenueCat key from `.env.example`, wire per-build-type keys, **rotate the test key**, restrict the `google-services.json` API key to package + SHA-1. | `.env.example`, `app/google-services.json`, `app/build.gradle.kts` | Fixes **B5, S4, S6**. |
| 3.5 | **Backup exposure** (per **D5**): either `allowBackup=false`, or keep backup and document it in the privacy policy. | `AndroidManifest.xml:12`, `res/xml/*` | **S5** |
| 3.6 | **Clear local data on sign-out** so a second user on the same device inherits nothing. | `HabitRepositoryImpl.onUserChanged`, `AppNavigation.kt:307-309` | **S8** |

**Acceptance criteria**
- Emulator test: authenticated write of `{"subscriptionTier":"plus"}` → `PERMISSION_DENIED`.
- Client cannot set tier by any code path (`git grep updateSubscriptionTier` returns only read-side usage).
- Release build with a debug App Check token fails; with Play Integrity it succeeds.
- Sign out → sign in as a different account → today's list is that account's only.

**Risk:** high — rules can lock out your own app if a query shape is wrong. Land 3.1 with the emulator test suite first, ship to an internal testing track, watch for `PERMISSION_DENIED` before promoting.

---

## Phase 4 — Make features honest

**Objective:** nothing in the UI lies.

| ID | Task | Files | Detail |
|---|---|---|---|
| 4.1 | **Real data export.** `repository.exportAll()` → JSON (habits, check-ins, milestones, user) via Gson/kotlinx-serialization, `prettyPrint`, shared through the existing FileProvider. Keep the same UI affordance. | `SettingsScreen.kt:354-368`, `HabitRepository.kt` | Replaces the hardcoded `{"habit":"Drink Water","growth_points":12}`. |
| 4.2 | **Notifications inbox** (per **D6**): either a real `notifications` Room table written by the worker (with read/unread + deep link), or delete the screen, the Today bell, and the route. | `NotificationsScreen.kt`, `HabitReminderWorker.kt`, `Routes.kt`, `AppNavigation.kt` | Currently two fake rows. |
| 4.3 | **Habit detail ⋮ menu:** wire to Edit / Archive / Delete-with-confirm, or remove the button. | `HabitDetailScreen.kt:51` | `/* TODO */` no-op today. |
| 4.4 | **Apple Sign-In:** hide the button behind a build flag until it exists, instead of a "coming soon" toast. | `AuthScreen.kt:158-160` | Dead control. |
| 4.5 | **Remove dead capability config:** drop `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` + `GEMINI_API_KEY` from metadata/README/`.env.example`. | `metadata.json`, `README.md`, `.env.example` | Audit §4. |

**Acceptance criteria**
- Export → open the file → real titles, real dates, real check-in counts; round-trip test in JVM.
- No screen in the app displays a hardcoded data row.
- `grep -rn "coming soon\|TODO" app/src/main` returns nothing user-facing.

**Risk:** low.

---

## Phase 5 — Monetization UX

**Objective:** the paywall never dead-ends.

| ID | Task | Files | Detail |
|---|---|---|---|
| 5.1 | **Paywall states:** `Loading | Ready | Error(retryable)`. On offerings failure show "Couldn't load plans — retry" instead of a permanently disabled CTA with a fake price. | `PaywallScreen.kt:44-63, 180-212` | Audit §4 paywall row. |
| 5.2 | **Surface purchase outcomes:** cancelled / failed / pending → distinct messages; on success refresh tier and dismiss. | `PaywallScreen.kt:182-191` | Errors are currently swallowed. |
| 5.3 | **Render `paywallMessage`** ("You've planted 3 habits — upgrade to plant more") inside the paywall instead of generic copy. | `AppNavigation.kt:35, 255, 329` → `PaywallScreen` | Set 3×, displayed 0×. |
| 5.4 | **Safe Activity cast:** unwrap `LocalContext` via `findActivity()` rather than `context as Activity`. | `PaywallScreen.kt:185` | Crash risk in dialogs/previews. |
| 5.5 | **Settings cleanup:** delete the dead `onToggleSubscription` plumbing; compute the "Notification times" subtitle from real habits; version label from `BuildConfig.VERSION_NAME`. | `SettingsScreen.kt:44, 122, 202`, `AppNavigation.kt:312-316` | Audit §4, §6. |

**Acceptance criteria**
- Airplane mode → open paywall → readable error + working Retry.
- Cancel the purchase sheet → friendly "no change" message, CTA usable again.
- Hit the 3-habit limit → paywall explains *why*.
- Settings version matches `versionName`.

**Risk:** low. RevenueCat must be configured (test key is fine) to exercise the happy path.

---

## Phase 6 — Architecture & quality

**Objective:** make the codebase hold up after the fixes, and keep the bugs from coming back.

| ID | Task | Detail |
|---|---|---|
| 6.1 | **ViewModels + UiState** per screen, starting with Today → Garden → Settings; repository injected via a small manual container (or Hilt if you prefer the dependency). Kills the `remember { HabitRepositoryImpl(...) }`-in-composable pattern and state loss on rotation. |
| 6.2 | **Test coverage for the core.** Pure-logic tests from Phase 1 expand to repository-level tests with Robolectric + in-memory Room: toggle check-in, undo, milestone emission at 7/30/100, tier gating at 3 habits, export serializer. Target: every bug in `AUDIT.md` §3 has a regression test. |
| 6.3 | **Room migrations done properly:** `exportSchema` (Phase 0) + explicit `Migration` objects + `MigrationTestHelper`; no destructive fallback in release. |
| 6.4 | **Strings & accessibility:** move all copy to `strings.xml`, plus plurals; `contentDescription` on every icon-only control; drop the "←"/"⋮" glyph buttons; sanity-check RTL since `supportsRtl="true"` but nothing is translated. |
| 6.5 | **Dead weight:** remove `retrofit`, `converter-moshi`, `okhttp`, `logging-interceptor`, `moshi-kotlin` (zero call sites), the unused `User` entity or actually register it, unused `onNavigateToTab`/`BottomNavBar` imports, `add_habit_mockup_*.jpg` from `res/drawable`, root shell/python scripts, `enableJetifier=true`. Consider enabling R8 + `isShrinkResources`. |
| 6.6 | **CI hardening:** lint gate, ktlint/detekt, Roborazzi screenshot job, dependency-update config. |

**Acceptance criteria:** core-logic coverage reported in CI; `lint` clean at `error` severity; APK/AAB size measurably down; rotation no longer loses dialog/modal state.

**Risk:** medium — the ViewModel refactor touches every screen. Do it screen by screen, never as one mega-PR.

---

## Phase 7 — Release readiness

| ID | Task |
|---|---|
| 7.1 | Register **release + debug SHA-1** in Firebase; verify Google Sign-In on a signed release build (**B4**). |
| 7.2 | Play Console setup: data-safety form, privacy policy (habit data + backups + RevenueCat), account-deletion path (required for account-creating apps), internal → closed → production tracks. |
| 7.3 | Permission cleanup: drop unused `READ/WRITE_EXTERNAL_STORAGE` (or add `maxSdkVersion`), confirm `targetSdk = 35` submission policy. |
| 7.4 | Version bump + tag; store listing; Crashlytics or equivalent for the first release. |
| 7.5 | Final smoke pass over the manual ACs from Phases 1, 2, 4, 5 on a physical device with a real (non-test) RevenueCat key. |

---

## Decisions I need from you

Each of these changes what I write. My recommendation is included so you can just say "all recommended".

| ID | Question | Options | My recommendation |
|---|---|---|---|
| **D1** | What is `growthPoints` for? | (a) Lifetime check-in count, +1/−1 — keeps Milestones `10/25/50/100` as-is and makes them work; (b) drop the field, re-key Milestones to streak tiers `7/30/100`; (c) keep both systems deliberately (points for milestones, streak for stages) | **(a)** — smallest change, honest label ("growth moments"), and Phase 1's progress bars finally move |
| **D2** | Reminder precision | (a) Exact alarms via `AlarmManager` (precise, needs `SCHEDULE_EXACT_ALARM` + policy); (b) stay on WorkManager, accept minutes-to-hours drift, relabel Settings honestly | **(a)** for a reminder app — users notice a 7:30 nudge arriving at 9:10 |
| **D3** | Voice reminders (TTS) | (a) Speak only on tap (current code path); (b) auto-speak when the notification opens the app; (c) drop TTS | **(a)** — least surprising, no background audio, keeps the differentiator |
| **D4** | How is Pro enforced? | (a) Cloud Functions + RevenueCat webhook (Blaze plan required); (b) webhook into your existing backend; (c) rules-locked client field + accept residual risk | **(a)** if you're on Blaze; otherwise (b). Do **3.1 regardless** |
| **D5** | Backups | (a) `allowBackup=false`; (b) keep, disclose in policy | **(b)** if you want frictionless device migration; **(a)** if you'd rather not explain behaviour data in Drive |
| **D6** | Notifications inbox | (a) build a real store (Room table + read state + deep links); (b) remove screen + bell | **(b)** now, **(a)** when you add push — a dead inbox is worse than none |
| **D7** | Off-schedule check-ins | (a) hard-block (streak integrity); (b) allow, marked as a bonus, streak math unaffected | **(b)** — blocking a motivated user is the classic habit-app mistake |

---

## Suggested starting points

| If you want… | Start with | Time to visible result |
|---|---|---|
| **A green build + CI** | Phase 0 | ~half a day |
| **The app to feel real** (streaks, growth, edits not eating data) | Phase 0 → 1a → 1b | 2–3 days |
| **Fewer support tickets** (reminders) | Phase 0 → 2 | +1–2 days |
| **To not get burned on billing** | Phase 3.1 only (rules) | ~2 hours |
| **Something to show** | Phase 0 → 1 → 5 | ~3 days |

**My default answer if you say "just start":** Phase 0, then 1a. Phase 0 is purely mechanical and unblocks CI; 1a is the highest-value correctness work and it's the foundation for the tests in every later phase.

---

## Definition of done (whole plan)

- `AUDIT.md` §2 (blockers): all closed — CI builds a clean clone, signing works from env vars, Google Sign-In verified on a release build, no test key in the default path.
- `AUDIT.md` §3 (functional bugs): all closed with a regression test each.
- `AUDIT.md` §4 (stubs): zero — every visible feature does what it says.
- `AUDIT.md` §5 (security): rules committed and tested, entitlement server-set, App Check on, secrets rotated.
- `AUDIT.md` §6: core logic covered, strings externalized, dead deps gone, CI gates lint + tests.
- A physical-device pass of every manual AC in Phases 1, 2, 4, 5.
