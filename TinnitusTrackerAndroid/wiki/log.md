# Development Log

## [2026-05-12] feature | 소리 (Sound) screen redesign — Direction A card stack

**Scope:** Complete redesign of stub Sound settings screen per wireframes Direction A (Calm settings list).

**Changes:**
- Replaced single "frequency matching" card + 3 placeholders with full 5-card layout:
  1. **Preset switcher card** (TealSoft bg) — "저녁 휴식 ▾" + ghost "+ 저장" button
  2. **Frequency card** — "4,250 Hz · 고음" + TealSoft "다시 측정 ›" pill (routes to matcher)
  3. **Processing mode card** — segmented control (노치 active | 증폭) + description
  4. **Color noise card** — pill selector (핑크 active | 화이트 | 브라운 | 끄기)
  5. **Ambient mix card** — 빗소리 60% + 파도소리 0% with progress bars
- Header row: "소리 설정" (18sp) + "미리듣기 ▶" preview button
- All values hardcoded with `// TODO:` comments for live data wiring
- Matcher subscreen and MainActivity untouched; "다시 측정" pill preserves existing routing

**Verification:**
- ✓ Build green (`./gradlew :app:assembleDebug`)
- ✓ All 5 cards render with correct layout and colors per wireframes
- ✓ "다시 측정 ›" pill still opens matcher (onOpenMatcher callback preserved)

## [2026-05-12] feature | Onboarding re-skin — Direction A polish

**Scope:** Polished Direction A onboarding (already structured correctly) with three targeted improvements per wireframes spec.

**Changes:**
- Hero height: 340dp → 320dp (match wireframes mockup)
- Fixed hardcoded color: `Color(0xFFE8F0EE)` → `TealSoft` token
- **Section-specific hero art** — addresses "no imagery" & "no visual rhythm" critique:
  - Tinnitus section: waveform patterns (pages 1–3)
  - Therapy section: brain node network, 6 outer nodes + center coral node (pages 4–6)
  - AppGuide section: checkmark arc with progress dots (page 7)
- Hero composable now routes to correct Canvas art based on `OnboardingSection` param

**Verification:**
- ✓ Build green (`./gradlew :app:assembleDebug`)
- ✓ Three distinct hero art assets now render per section
- ✓ Canvas drawing patterns reuse existing WaveformArt style

## [2026-05-12] feature | Visual system migration + 홈 redesign

**Scope:** Migrated app chrome from dark/orange (inherited matcher styling) to light/teal/coral per wireframes design system. Completely redesigned 홈 (Home) screen with editorial direction (Direction A hero card + Direction D week strip).

**Files modified:**
- `ui/theme/Theme.kt`: Status bar appearance flipped to light (Bg background, isAppearanceLightStatusBars = true)
- `MainActivity.kt`: NavigationBar restyled — white surface container, Teal active, Muted inactive, TealSoft indicator
- `ui/home/HomeScreen.kt`: Full redesign per wireframes
  - Top bar: greeting + treatment week + settings gear icon
  - Hero player card: gradient background, "저녁 휴식" preset, play button, 84/120 min overlay
  - Week strip: 7-column progress visualization with past-days checkmarks, today's gradient bar, future-days empty
  - Sleep timer chip and TFI prompt card (mock content, no-op for now)
- Other screens (`onboarding`, `sounds`, `records`, `settings`): Updated to inherit MaterialTheme correctly; no visual changes

**Verification completed:**
- ✓ Build succeeds (`./gradlew :app:assembleDebug`)
- ✓ Status bar icons now dark on cream background
- ✓ Home screen renders with new design, all wireframe elements in place
- ✓ Navigation bar is light/teal/cream, active tab highlighted
- ✓ Week strip shows correct progression (4 checkmarks + 1 partial + today's 84/120)
- ✓ Play button routes to Sound tab (existing wiring preserved)
- ✓ Settings gear routes to Settings tab
- ✓ Matcher still renders dark with light tab bar at bottom (expected visual seam)
- ✓ Zero AndroidRuntime FATAL crashes

**Next tasks in queue:**
1. Onboarding re-skin (wireframes A/B/C directions) — calls out redesign needed per wireframes critique
2. 소리 (Sound) page redesign
3. TFI questionnaire implementation
4. 기록 (Records) page redesign
