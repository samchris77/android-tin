---
title: "UX 진단 — 홈/소리/기록/설정 (에뮬레이터 실측)"
date: 2026-10-01
type: chat-summary
tags: [app/ux, app/design, chat]
sources: ["[[wiki/app/navigation]]", "[[wiki/app/ux-principles]]", "[[wiki/app/architecture]]"]
---

# 2026-10-01 UX 진단

에뮬레이터(`Medium_Phone_API_36.1`)에서 debug APK 실행 후 홈 / 소리 / 기록 / 설정 4탭 스크린샷 확보. `navigation.md` + `ux-principles.md` 기준으로 괴리 평가.

## 사용자 동선 원칙

- 북극성: **1-tap 재생 + "오늘 목표?"** 즉시 확인
- Secondary: 프리셋 편집, 기록 조회, 설정
- 매일 동선 = 앱 열기 → 홈 ring 으로 진척 확인 → play

## 화면별 진단

### 홈 — 괴리 큼

| 문제 | 영향 |
|---|---|
| Spec 핵심 ring (84/120) 사라지고 선형 bar 로 강등 | "did I do today?" gestalt 약화 |
| Play hero + "new ▾" preset chip 같은 card → CTA 2개 충돌 | tap target 모호 |
| 수면 타이머 / 상태 기록 side-by-side 중 상태 기록만 coral | 1-accent 규칙 위반 (ux-principles §7) |
| preset dropdown affordance (`▾`) 작음 | 사용자 PresetPickerBottomSheet 존재 모름 |

**수정**
- `HomeScreen.kt` hero: ring centerpiece + play 를 ring 내부 중앙에 배치
- Preset → `ChevronRow` ("현재 프리셋 › new / 증폭 · Brown · 자연음")
- 수면 타이머 / 상태 기록 색 통일 (둘 다 뉴트럴 또는 상태 기록만 유지)

### 소리 — 깔끔하나 결정 부담

| 문제 | 수정 |
|---|---|
| 처리 방식 ON/OFF toggle + 노치/증폭 segmented (2단) | `SegmentedControl(노치, 증폭, 끄기)` 3-way 로 통합 |
| 컬러 노이즈 `끄기` 옵션 부재 | `Pill` 세트에 `끄기` 추가 |
| 자연음 각 row 토글 없음 → 끄려면 slider 0% drag | 좌측 `Switch` + 우측 slider; Off 시 dim |

### 기록 — 근접

| 문제 | 수정 |
|---|---|
| Spec 3-segment (`달력 / 청취 / TFI`) vs 실제 2-segment (`개요 및 기록 / TFI 점수`) | 3-segment 복원 또는 day-tap drilldown 안에 세션 상세 명시 |
| 첫달 heatmap 전부 동색 → legend `적음↔많음` 변별 안됨 | zero-state 시 legend 숨기고 `EmptyStateCard` |

### 설정 — 정렬 역순 + 누락 많음

**문제**
1. 학습 자료 (튜토리얼/메커니즘) 최상단 — spec 은 하단 meta
2. `"준비 중"` 라벨 2개 (이명 메커니즘, 주파수 다시 측정) 노출 → dead-end
3. 누락: 테마(다크 모드), 알림 권한, 앱 버전, **임상 면책 조항** (PRD 필수), OSS 라이선스
4. TFI 주기 `1주마다` 표시 — default `2주` 와 불일치 (state 확인 필요)

**spec 순서로 재배열**
```
1. 청취 목표      일일 청취 목표 (2/4/6시간)
2. 설문조사       TFI 주기 / TFI 다시 작성 / 마지막 작성일
3. 치료           치료 시작일
4. 주파수         주파수 다시 측정 (현재값)
5. 학습 자료      튜토리얼 다시 보기 / 메커니즘 다시 보기
6. 알림           권한 상태
7. 정보           앱 버전 / 임상 면책 / OSS 라이선스
```

## MiniPlayer

- 보임 로직 `visible = current != Tab.Home` 정상
- Default preset 이름 `new` → `"저녁 휴식"` 등 polish (SoundPresetDao seed 재검토)

## 수정 우선순위

| # | 수정 | 파일 | 효과 |
|---|------|------|------|
| 1 | 홈 ring 복원 (centerpiece) | `ui/home/HomeScreen.kt` | daily intent 명확 |
| 2 | 설정 섹션 재정렬 + 누락 필드 | `ui/settings/SettingsScreen.kt` | 법적/임상 필수 |
| 3 | 소리 처리 방식 → 3-way segmented | `ui/sounds/SoundSettingsScreen.kt` | 결정 1단 축소 |
| 4 | 자연음 토글 추가 | `ui/sounds/SoundSettingsScreen.kt` | tap 수 감소 |
| 5 | "준비 중" 숨김 또는 disable | `ui/settings/SettingsScreen.kt` | dead-end 제거 |
| 6 | 기록 heatmap zero-state | `ui/records/RecordsScreen.kt` | 신규 사용자 혼란 제거 |
| 7 | Default preset 이름 변경 | DB seed / migration | 첫인상 polish |

Impact 최대: **1 + 2**. 1 = 매일 UX, 2 = 법적 필수.

## 실측 환경

- Device: Pixel-size AVD 1080×2400
- Build: `./gradlew :app:assembleDebug` → `adb install -r app-debug.apk`
- 설치 성공, 앱 실행 OK, `rememberSaveable` 로 이전 세션의 기록 탭 복원됨 (default `Tab.Home` 이지만 saved state 우선)

## See also

- [[wiki/app/navigation]] — screen-by-screen layout
- [[wiki/app/ux-principles]] — design rules
- [[wiki/app/architecture]] — package 구조
- [[wiki/app/plan]] — 다음 작업 큐
