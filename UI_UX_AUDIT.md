# Perennial — UI / UX Audit: Where Imagery and Motion Belong

Companion to [`AUDIT.md`](AUDIT.md) and [`FIX_PLAN.md`](FIX_PLAN.md).
**Scope:** visual identity, illustration, and motion only. Behaviour bugs stay in the other two docs.

---

## Verdict in one line

Perennial doesn't look generic because it's *undecorated* — it looks generic because **every visual in the app is a 24dp monochrome glyph on a flat surface, and almost nothing ever moves.** The app is built on a gardening metaphor, but the largest picture it ever draws of "your plant" is a 120dp tinted icon inside a milestone dialog.

---

## Part 1 — What the visual layer is today (evidence)

| Dimension | Reality |
|---|---|
| **Artwork** | 38 drawables. **All** are 24dp viewport, 1.5dp stroke, single-colour vector glyphs. All 49 `painterResource` call sites apply `ColorFilter.tint(...)` → **every image in the app is monochrome.** |
| **Illustration** | **Zero.** No scene art, no characters, no plant portraits, no backgrounds. |
| **Photography / texture** | **Zero.** No paper, no soil, no gradients, no grain, no shadows beyond Material elevation. |
| **Hero moments** | Only 3 exist: onboarding page 2 (120dp icon), milestone modal (100dp icon in a 120dp box), habit detail (80dp icon). |
| **Motion** | 3 implementations total, of which **one is a no-op** (below). No `AnimatedVisibility`, no `AnimatedContent`, no `Crossfade`, no `animateItem()` on any list, no shared-element transitions, no haptics, no skeletons, no splash screen. |
| **Empty states** | Today has **none**. Milestones / Archived / Notification times are one grey sentence each. |
| **Typography** | Genuinely good — Fraunces (display serif) + Public Sans + IBM Plex Mono. But loaded via **downloadable Google Fonts at runtime** (`Typography.kt:12-28`), so first launch, offline, and non-GMS devices render the system font and then **reflow**. Your one premium signal can silently disappear. |

### The dead animation

`ui/components/GrowthIcon.kt:23`

```kotlin
val scale by animateFloatAsState(
    targetValue = 1f,          // ← constant. never changes.
    animationSpec = tween(durationMillis = 300),
    label = "growthScale"
)
```

`targetValue` is a literal `1f`, so this animates once from its initial value to 1f and then has **no reason to ever run again**. It looks like motion in code review and produces zero visual feedback on device. The growth icon — the centrepiece of the whole metaphor — is static.

### Your own mockup proves the gap

`app/src/main/res/drawable/add_habit_mockup_1784421858749.jpg` is a **379 KB design mockup shipped inside the APK, referenced by nothing**. It shows the intended language:

- warm **cream paper** background (~`#F2EFE6`), not the printed `#EDF1EA`
- **filled cream icon tiles** (56dp), not white boxes with hairline borders
- colour chips with **hex captions** — a designer's, not a developer's, detail
- pill-shaped segmented controls, generous negative space

The Compose implementation kept the *structure* of that mockup and threw away the *warmth*: `Color.Transparent` / `Color.White` surfaces, hairline outlines, and single-tint glyphs (`AddEditHabitScreen.kt:146-171`). The design intent already exists in your repo — it just never reached the screens.

### Why it reads "generic" — 5 causes

1. **No scale contrast.** Everything is 16–44dp glyphs. Without an 80–240dp object on screen, the eye has no focal point, so no screen has a "hero".
2. **No colour contrast from art.** Every image is one tint. The only colour in the app is swatches and buttons, so composition never gets depth.
3. **No texture.** Flat fills on flat fills. Paper, soil, and shadow are what make a paper-garden aesthetic feel tactile.
4. **No feedback.** Tapping "check in" changes a button state instantly. Nothing grows, nothing celebrates, nothing is acknowledged. The core loop — the entire point — has **zero** reward signal.
5. **No stateful art.** The metaphor says *the habit is a plant*. The app never shows you a plant; it shows a glyph that stands for a plant. There is no "your garden right now" object to feel attached to.

---

## Part 2 — The four registers of imagery (and what's missing)

Right now all imagery is collapsed into register 1. A garden app needs all four.

| # | Register | Role | Today | Needed |
|---|---|---|---|---|
| 1 | **Iconography** (16–32dp) | Navigation, affordances, list bullets | ✅ Solid — 38 good line glyphs | Keep. Add stage glyphs already exist |
| 2 | **Illustration** (80–320dp) | Emotion, onboarding, empty states, paywall | ❌ None | ~10 scenes |
| 3 | **Texture / surface** | Paper, soil, gradients, grain, soft shadow | ❌ None | 3–4 tiles + 1 gradient set |
| 4 | **Stateful art** (the plant) | *This is your product.* The thing that grows | ❌ None | **5 stages × 6 colour tags** |

**Register 4 is the highest-value gap in the entire app.** It's the only imagery that is *different for every user on every day* — it's the reason someone opens the app in the morning.

---

## Part 3 — Where images go, screen by screen

Priority: **P1** = do first (identity-defining) · **P2** = next · **P3** = polish.

| Screen | Slot | What to show | Spec | Pri |
|---|---|---|---|---|
| **App icon / splash** (API 31+) | Adaptive icon + `SplashScreen` animated | Sprout unfurling into the app | vector, `windowSplashScreenAnimatedIcon` | **P1** |
| **Onboarding p1** | Full-bleed hero, ~55% of viewport | A warm morning garden: 3–4 plants in a soil bed, soft light, paper grain | raster 1080×1600, WebP q80 | **P1** |
| **Onboarding p2** | Diagram | The 5 stages rising left→right (seed → sprout → bud → bloom → resting) with labels | vector or raster 1080×900 | **P1** |
| **Onboarding p3** | Small card art | Three reminder styles — voice (sound waves over leaf), text (note card), none (dormant pot) | 3 × 160dp raster | P2 |
| **Auth** | Top third + corner motif | Soft botanical fronds bleeding off both corners; keeps the form on cream paper | raster 1080×600 + corner PNG | P2 |
| **Today — header** | Replaces the 28dp sprout glyph | **"Your garden right now"**: the user's actual habits as small plants in a soil strip. Tappable → Garden | composable (register 4) | **P1** |
| **Today — habit card** | Replaces the 44dp tinted glyph | The habit's **plant at its current stage in its colour tag**, in a small planter vignette, in full colour | 5 stages × 6 tags, 96dp @3x | **P1** |
| **Today — empty state** | Centre, when 0 habits | Single seed in soil + "Plant your first habit" | raster 480dp + CTA | **P1** |
| **Habit detail — hero** | Top 40% | **The portrait**: 200–240dp plant, layered (soil → stem → leaves → bloom), the largest object in the app | composable, layers as vectors | **P1** |
| **Habit detail — growth bed** | 7-day strip | Plants planted *in* a soil row at their stage for that day; dormant/unscheduled shows a seed outline in soil | composable | P2 |
| **Garden — bed** | Behind the list | Illustrated soil bed + paper texture + a soft sky gradient; plants sit in the bed | raster bed 1080×400 + tile | **P2** |
| **Milestones — celebration** | Full-screen overlay | Bloom burst: petals, seeds, light motes; the plant assembling itself | composable particles | **P1** |
| **Milestones — timeline nodes** | 40dp per node | Stage art per tier (7 / 30 / 100), with the earned node in colour and future nodes as line art | vector ×3 | P2 |
| **Milestones — empty** | Centre | "Your first bloom is 7 days away" + a single budding stem | raster 480dp | P2 |
| **Archived** | Centre | A dormant pot with a resting leaf — "These plants are resting" | raster 400dp | P3 |
| **Paywall** | Top 45% | Split composition: one pot (free) vs. a lush bed (Pro). This is your conversion asset | raster 1080×900 | **P1** |
| **Share card** | Whole card | Illustrated share card: plant portrait + paper + wordmark + streak — currently text and a 🌿 emoji | composable → graphics layer (already implemented) | **P2** |
| **Notification** | `setLargeIcon` | The habit's plant art — makes the notification recognisable at a glance | 64dp bitmap | P3 |
| **First-sync loading** | Card placeholders | Shimmer skeletons shaped like habit cards (no spinner) | composable | P3 |

### What NOT to add images to
Settings, Account, Add/Edit forms, Notification times, the habit-detail stat block. These are functional surfaces; adding art there makes the app feel *busier*, not richer. Restraint is what separates "designed" from "decorated".

---

## Part 4 — Where motion goes (ranked by impact)

The rule: **motion should explain a state change or reward an action.** Anything else is noise.

| # | Moment | Trigger | What it does | Compose API | Timing |
|---|---|---|---|---|---|
| **1** | **Check-in** | Tap check-in | Plant "absorbs" the tap: quick scale-down → overshoot up, leaf unfurls, one soft ring expands from the soil, colour deepens, **haptic tick** | `animateFloatAsState` + `spring(dampingRatio = 0.45f)` on `graphicsLayer`; `LocalHapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)` | 450 ms |
| **2** | **Stage advance** | streak crosses 2 / 4 / 7 | Old stage art cross-fades + grows into new stage; label counts up | `AnimatedContent(transitionSpec = { fadeIn + scaleIn })` | 600 ms |
| **3** | **Streak count** | Value changes | Digits roll up on Today + detail (also fills the product gap: streak is invisible on Today today) | `animateIntAsState` + `AnimatedContent` | 500 ms |
| **4** | **Milestone celebration** | 7 / 30 / 100 reached | Full-screen: petals + seeds drift up, plant assembles, text staggers in, ambient light pulse. Skippable by tap | `Canvas` + `rememberInfiniteTransition`, staggered `AnimatedVisibility` | 1.2–1.8 s |
| **5** | **List → detail hero** | Open a habit | The plant card grows into the detail hero (shared bounds) | `SharedTransitionLayout` — **stable in Compose 1.7.6**, which this project already uses | 300 ms |
| **6** | **List add / remove / reorder** | Any list change | Items slide + fade into place instead of snapping (Today, Garden, Archived, Milestones) | `Modifier.animateItem()` — stable in 1.7 | 250 ms spring |
| **7** | **Tab / screen change** | Bottom nav, push | Fade + 8dp slide; selected nav icon springs to 1.1× | `NavHost(enterTransition = …)`, `animateFloatAsState` | 200 ms |
| **8** | **Garden drag** | Long-press drag | Already has elevation (`GardenScreen.kt:232`) — add a 2° tilt while dragging and let neighbours slide (via #6) | `graphicsLayer { rotationZ }` | live |
| **9** | **Weekly summary** | Screen appears | Ring/segments sweep from 0 to value, once | `animateFloatAsState`, `LaunchedEffect` | 700 ms |
| **10** | **Ambient sway** | Idle on Today / detail | Plants sway ±1.5° over 4–6 s, phase-offset per plant. **This is what makes a static screen feel alive** | `rememberInfiniteTransition` | ∞ |
| **11** | **Paywall entrance** | Screen opens | Plan cards stagger in; hero plant breathes slowly | `AnimatedVisibility` stagger + infinite | 400 ms |
| **12** | **Onboarding pager** | Swipe | Illustration parallax (moves at 0.6× page offset) + gentle breath loop | `pagerState.currentPageOffsetFraction` + `graphicsLayer` | live |

### Also
- **Splash**: `installSplashScreen()` + `setKeepOnScreenCondition` while the first Firestore snapshot loads — removes the current white/blank flash on cold start, which is the very first impression.
- **Pull-to-refresh** (if added later): a sprout that grows as you pull, instead of the default spinner.
- **Shimmer** on first sync instead of empty lists flashing to populated.

### Where motion does NOT belong
Settings rows, form fields, price text, and any text-heavy screen. No parallax on content people are reading; no looping animation near inputs (it steals focus, and it's hostile for ADHD users — your own Settings screen calls them out as the audience). No animation longer than ~600 ms on a repeating action like check-in.

---

## Part 5 — Technical constraints (so this doesn't backfire)

| Concern | Guidance |
|---|---|
| **Font flash** | Bundle Fraunces / Public Sans / IBM Plex Mono in `res/font` (~300–500 KB) and drop the runtime Google Fonts provider, or preload via `FontFamily.Resolver.preload()`. Right now your typography identity is a network dependency. |
| **Asset formats** | Painterly art → **WebP** (q80–85), one `drawable-nodpi` + explicit decode, or density buckets. Glyphs/stages → **vector XML** (crisp, tiny, tintable). Never ship PNGs for flat art. |
| **APK size** | Budget: ≤ 4 MB of new art. `add_habit_mockup_*.jpg` (379 KB, unused) gets deleted as part of this. Enable R8 resource shrinking (`isShrinkResources`). |
| **Dark mode** | The palette flips to Ink Teal; cream paper art would glow. Produce **two variants** (light paper / ink) or use a duotone overlay + `BlendMode`. Decide per asset; the plant portraits should be the *same* art with an adjusted backdrop. |
| **60 fps** | Animate `alpha` / `translation` / `scale` / `rotation` only (all `graphicsLayer`, no re-layout). Never animate `padding`/`size` on scroll. Use `drawWithCache` for particles. |
| **Accessibility** | Honor the system animator scale — Compose animations already do; **disable ambient sway** when duration scale is 0. Give every illustration a `contentDescription` or `null` if purely decorative, and keep text out of raster art (it can't be localised or scaled). |
| **Layout shift** | Reserve `aspectRatio` boxes for every image so nothing jumps when it decodes. |
| **Sharing** | The share card already captures via `GraphicsLayer` — illustrated art composes into it for free. |

---

## Part 6 — Proposed UI workstream (slots into `FIX_PLAN.md`)

This runs **in parallel** with the functional phases, because it touches different files.

| Stage | Work | Effort | Depends on | Ships |
|---|---|---|---|---|
| **U1 — Foundations** | Bundle fonts; splash screen; cream/paper surface tokens; nav + list transitions (`animateItem`, fade-slide); delete the no-op `GrowthIcon` animation and add the real one; haptic on check-in | 1–2 d | Phase 0 (build) | App stops feeling cheap on contact |
| **U2 — The plant system** ⭐ | 5 stages × 6 colour tags plant art; `PlantPortrait` composable with layers; Today card art; detail hero; check-in growth animation; shared-element transition | 3–4 d | Phase 1.1 (streak), 1.3 (status) | **The identity shift** |
| **U3 — Hero moments** | Milestone celebration (particles); onboarding p1/p2 illustrations; all empty states; paywall hero; share-card redesign | 2–3 d | 1.2 (growthPoints) | Marketing screenshots |
| **U4 — Ambient & polish** | Idle sway; Garden bed + soil; weekly ring animation; skeletons; notification large icon; dark-mode art variants | 2 d | U2 | "Alive" feel |

**Total: ~8–11 days**, of which U2 alone is the change people will notice.

---

## Part 7 — If you only do five things

1. **The plant system (U2).** Replace every habit card's tinted glyph with the actual plant, in colour, at its stage. This single change does more than the other four combined.
2. **Check-in animation + haptic (#1).** The most-repeated action in the app currently gives zero feedback.
3. **Bundle the fonts + add the splash screen.** Kills the font reflow and the cold-start flash — the two "cheap app" tells on first launch.
4. **List + nav transitions (`animateItem()`, fade-slide).** ~30 lines of code; removes the "web view" snapping feel everywhere at once.
5. **Empty states + onboarding heroes.** The first 30 seconds and the sad paths are where the app currently looks most unfinished.

---

## Part 8 — Decisions I need

| ID | Question | Options | My recommendation |
|---|---|---|---|
| **V1** | Art direction for the illustrations | (a) **Painterly/gouache botanical** — soft, warm, textured, matches cream paper; (b) flat vector — crisp, cheap, but risks the "generic" feel you're trying to escape; (c) line-art only + colour accents — cheapest, most consistent with the existing glyphs | **(a)** for hero/onboarding/empty states, **vector** for the 30 stage glyphs. Mixed is correct here |
| **V2** | Who produces the art? | (a) I generate it (I can produce the full set in-brand and iterate); (b) you commission; (c) open-source illustration set, restyled | **(a)** to unblock U2/U3 now — swap for commissioned art later without touching code |
| **V3** | Ambient sway by default | (a) on for everyone; (b) on, but off when system animations are disabled; (c) opt-in via Settings | **(b)** |
| **V4** | Dark mode art | (a) two full asset sets; (b) one set + duotone overlay; (c) light-only art, UI chrome flips | **(b)** — much less to maintain, and a duotone keeps the plants consistent |

---

### Bottom line

The bones are good — the type system, the palette, the tokens, and one very telling mockup all point at a warm editorial garden product. What's missing isn't decoration, it's **scale, colour, texture and feedback**: one big object per screen, art that's different every day, and a reaction when the user does the thing the app exists for. Fix the plant, the feedback, and the fonts, and the same code will stop reading as a form-based habit tracker and start reading as *your garden*.
