# BUILD_PLAN.md — the tackle plan

**Owner:** build agent · **Branch:** `arena/460765ea-spout-app` · **Supersedes:** nothing (FIX_PLAN.md stays as the technical backlog; this is the execution order)
**Companion docs:** `AUDIT.md` (what's broken), `FIX_PLAN.md` (D1–D7 technical debt, deferred), `UI_UX_AUDIT.md` (why it looks generic, U1–U4)

---

## 1. The north star

> Perennial should feel like a calm object you tend, not a form you fill in.

Today the app is functionally complete and visually generic: 38 single-colour drawables, 49 tinted
`painterResource` sites, one of three animations dead (`targetValue = 1f`), no splash, no
`animateItem`, no haptics, no empty state. The data model is fine. **The gap is entirely
presentation and motion.**

So the work is not "add features". It is: give the app a visual language, then move it.

**The one-sentence test for every screen:** *can you see something growing?* If a screen is only
text, rows, and tinted glyphs, it isn't done.

---

## 2. Decisions I'm taking

You deferred these. Here they are, with reasoning, and all are cheap to reverse.

### D1 — Five stages, and the seed gets a tighter crop
`seed → sprout → bud → bloom → resting`. Four stages would collapse "seed" into "sprout" and lose
the beginning of the story, which is the part users see most (every new habit starts there).

The concern that the seed reads too small at card size is real. Fix is in the pipeline, not the
concept: every stage ships **two crops** — `full` (1024², for the hero and the growth bed) and
`card` (bottom-centre crop at 1.35×, soil baseline preserved, for 60 dp tiles). Same art, two
framings, ~4 KB each.

### D2 — Moss only for now; all six tags in Phase 5
Tinting full-colour art with `BlendMode.Multiply` looks muddy at 60 dp — it tints the soil and the
paper too. Proper per-tag art is a generation pass, not a runtime trick. So: **build and judge the
whole experience in Moss first.** If the look holds, generating the other five tags (Clay, Butter,
Soil, InkTeal, Charcoal) is ~30 images and ~150 KB — a half-day, and it's Phase 5, not Phase 1.
Nothing in the code hard-codes "moss"; the tag is a parameter from day one.

### D3 — Dark mode: a real night set, not a duotone
A duotone filter over warm-paper art produces grey mud, because the paper *is* the light source in
these illustrations. Since a full 5-stage set is only ~32 KB as WebP, a hand-generated "night"
variant (deep ink-teal paper, moonlit leaves) is affordable and dramatically better. **One
generation pass, 5 images, Phase 6.**

### D4 — Verify in CI with Roborazzi, not with a tunnel
The preview tunnel is unreliable (502s from the gateway, not the app) and there is no JDK, Gradle,
or Android SDK in this sandbox — I cannot compile or screenshot Compose from here. Roborazzi is
already a dependency in the repo. So: **GitHub Actions builds the app and captures
`captureRoboImage()` screenshots as run artifacts.** That gives you pixel-accurate Compose output
with no proxy, and gives us visual regression on every future PR. This is Phase 0 because it
unblocks *seeing* everything else.

### D5 — The harness stays out of git
`design-preview/` and `design-source/` are git-excluded (`.git/info/exclude`, not `.gitignore`, so
your tree stays clean). They live inside the repo only so they survive sandbox rotation. **Only
`res/` assets and Kotlin code get committed.** The harness is a sketch pad, per your call.

### D6 — No new dependencies
Everything below is Compose foundation/material3, which you already have. The only "new" thing is
Roborazzi in CI, and the plugin is already present.

---

## 3. Phases

Effort assumes focused sessions. Each phase ends with something you can look at.

### Phase 0 — Make the build observable · 1–1.5 d · ✅ done (PR #3)
The whole plan is blind without this: I can't run Gradle here.

| # | Task | Done when |
|---|---|---|
| 0.1 | Repair Gradle wrapper (jar fetchable from `gradle/gradle@v8.9.0`; `distributionUrl` → `gradle-8.9-bin.zip`) | `./gradlew --version` succeeds in CI |
| 0.2 | Confirm the project actually compiles (AGP 8.7.3 / Kotlin version mismatch is a real risk) | `assembleDebug` green in CI |
| 0.3 | `.github/workflows/screenshots.yml`: JDK 17, SDK, `:app:recordRoborazziDebug` | artifact contains PNGs |
| 0.4 | First screenshot of Today, as-is | you can open it |

**Risk:** 0.2 may surface latent compile errors that were never caught. That's a feature — you need
them fixed before any of this ships. I'll fix them under this phase and report before proceeding.

### Phase 1 — Foundations · 1.5 d · ✅ done (PR #3)
| # | Task | File |
|---|---|---|
| 1.1 | Semantic colour tokens: `PaperCream`, `CreamDeep`, `LeafDeep`, `BloomPink`, `InkSoft`, `InkMuted`, `SoilDeep` | `theme/Color.kt` |
| 1.2 | Shape scale: 12 / 18 / 26 / pill | `theme/Shape.kt` *(new)* |
| 1.3 | Wire tokens into scheme: `surface` → `PaperCream`, `outlineVariant`, `surfaceContainer*` | `theme/Theme.kt` |
| 1.4 | `PlantStage` enum with day thresholds + `RESTING` | `ui/components/PlantPortrait.kt` *(new)* |
| 1.5 | `PlantPortrait(stage, tag, size, animated)` — crossfade on stage change, a11y label, `Fit` scaling | same |
| 1.6 | 5 × (`full` + `card`) WebP → `res/drawable-nodpi` | `res/` |
| ~~1.7~~ | ~~Bundle fonts~~ **done** (`a49cee6`) — Fraunces/Public Sans/Plex Mono, 328 KB | — |

### Phase 2 — Today · 1.5–2 d · ✅ done (this PR)
The screen users see 30× a day. This is where the money is.

| # | Task |
|---|---|
| 2.1 | Garden strip header — 3 largest plants, phase-offset sway |
| 2.2 | Habit card rebuilt: plant tile (60 dp) + serif title + mono meta + check ring; cream surface |
| 2.3 | Check-in: spring scale 0.94→1.06→1, ring expand, colour warm, `HapticFeedbackType.Confirm` |
| 2.4 | `Modifier.animateItem()` on the list |
| 2.5 | Empty state: a seed in soil, "Plant your first habit", real CTA |
| 2.6 | Streak visible on Today (currently detail-only) |

**Acceptance:** tap-checking a habit moves something, makes a sound in the hand, and the list
reorders smoothly. Offline. Animator-scale 0 → everything still lands correctly.

### Phase 3 — Habit detail · 1 d
Hero portrait 250 dp with `SharedTransitionLayout` from the card; growth bed (7 cells, real stage
per day, unscheduled days distinct); streak roll-up counter; sticky action bar.

### Phase 4 — Milestone celebration · 1 d
Full-bleed butter surface, breathing plant, `Canvas` petals seeded from soil, count-up number,
`Share` primary. Skippable by tap, capped ~1.8 s, honours reduced-motion.

### Phase 5 — Supporting screens & the six tags · 1.5 d
Add/Edit (the worst offender per the audit: 143–215), Garden grid, Milestones list, Settings rows;
generate the five remaining colour tags × 5 stages.

### Phase 6 — Polish · 1–1.5 d
Splash (seed → wordmark), night art set + dark scheme, a11y pass (contrast, touch targets,
content descriptions), motion audit against the 12 ranked moments, dead-code cleanup
(`GrowthIcon.kt:23-27` dead `animateFloatAsState`, unreferenced 379 KB `add_habit_mockup_…jpg`).

**Total ≈ 8–9 focused days.**

---

### Progress log

| Date | What | Evidence |
|---|---|---|
| 2026-11-14 | Fonts bundled, tokens, PlantPortrait, wrapper + CI | PR #3, `713dcf9` |
| 2026-11-14 | Today rebuilt (strip, cards, empty state, animateItem, haptic) | `today_mixed.png`, `today_fresh.png`, `today_empty.png` |

**Note on art sources:** the untracked `design-source/` PNGs were lost to a sandbox
rotation. The committed 1024px `plant_*_full.webp` files are now the source of truth —
card crops are derived from them, so an asset change never needs the original PNGs.

---

## 4. How you see the work

| Loop | Latency | Cost | Use for |
|---|---|---|---|
| **CI screenshots (Roborazzi)** | ~5 min | free | Every phase gate — the default |
| `design-preview/standalone.html` | instant | free | Look/timing decisions before code exists |
| Live preview tunnel | instant | flaky (502) | When it works; never depended on |
| Device | slow | — | Haptics, fps, real touch targets |

---

## 5. What could go wrong

| Risk | Likelihood | Mitigation |
|---|---|---|
| Project doesn't compile (never verified) | **medium-high** | Phase 0.2 first; I fix and report before Phase 1 |
| AGP 8.7.3 vs Gradle/Kotlin versions | medium | Pin Gradle 8.9; adjust AGP if CI rejects |
| Sandbox rotation wipes non-repo files | certain, periodically | Harness lives in repo, git-excluded |
| Art re-roll differs from what you approved | **already happened** | Same direction/palette; your download restores exact art |
| Roborazzi needs `roborazzi` task wiring | low | Plugin already in repo; fall back to `screenshotTest` |
| Scope creep into "add features" | medium | This plan is UI/motion only; FIX_PLAN stays deferred |

---

## 6. Definition of done

1. Every screen shows at least one plant, at its real stage, in colour.
2. Every state change (check-in, stage advance, completion, milestone) has motion **and** haptic.
3. No screen depends on network or Play Services to look right.
4. Reduced-motion and zero animator scale produce correct, calm static screens.
5. Visual regressions are caught in CI, not by you.
6. The harness is gone from the repo; art lives in `res/`.

---

## 7. Immediate order of work

1. **Phase 0.1–0.2** — wrapper + prove it compiles *(the blocker)*
2. **Phase 1** — tokens, shapes, `PlantPortrait` *(started this turn)*
3. **Phase 2** — Today
4. **Phase 0.3–0.4** — CI screenshots *(slots in as soon as 0.2 is green)*
5. Phases 3 → 6

I'll report at every phase gate with either a screenshot artifact or, before CI is green, a static
diff of exactly what changed and why.
