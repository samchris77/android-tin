# CLAUDE.md — TinnitusTracker Android

## IMPORTANT: Read these three files before doing anything

Before starting any task in this project, read these files in full:

1. `/Users/midnight/Documents/Projects/2.Android_tin/Tinnitus_obsidian/wiki/app/architecture.md` — Android package layout, data model, module dependency rules, tech stack
2. `/Users/midnight/Documents/Projects/2.Android_tin/Tinnitus_obsidian/wiki/app/plan.md` — Current active task, next queue, and backlog (one active task at a time)
3. `/Users/midnight/Documents/Projects/2.Android_tin/Tinnitus_obsidian/wiki/app/prd.md` — Product scope, what the app is and isn't, target user, clinical framing

These are the canonical source of truth for product direction, architecture decisions, and implementation sequence. Do not start coding until you have read all three.

## Project

- **Language / UI:** Kotlin + Jetpack Compose
- **Pattern:** MVVM + Repository
- **Package:** `com.tinnitustracker`
- **Min SDK:** 28
- **Target SDK:** 34

## Key files

- `app/src/main/java/com/tinnitustracker/MainActivity.kt` — root nav scaffold
- `app/src/main/java/com/tinnitustracker/audio/engine/` — AudioEngine, BiquadFilter
- `app/src/main/java/com/tinnitustracker/ui/matcher/` — FrequencyMatchingScreen + ViewModel
- `app/src/main/java/com/tinnitustracker/ui/theme/` — Color, Theme, Type
- `docs/design/` — HTML design prototypes from Claude Design handoffs
