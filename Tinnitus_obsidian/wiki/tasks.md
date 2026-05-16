---
title: "Wiki Maintenance Tasks"
type: synthesis
aliases: ["Wiki Tasks", "Birds Eye View"]
tags: [wiki/maintenance]
evidence: n/a
status: stable
sources: []
last_reviewed: 2026-04-28
---

# Wiki Maintenance Tasks

This document provides a bird's eye view of the tasks needed to maintain and expand the Tinnitus / TRT Knowledge Wiki.

## Backlog

- [x] Ingest `raw/genius_ui_ux_design_strategy_detailed.md` + `raw/ui_ux_beginner_mistakes_detailed.md` → [[wiki/sources/kole-jain-genius-ux]], [[wiki/sources/kole-jain-beginner-mistakes]], [[wiki/app/ux-principles]]
- [x] Ingest `raw/Top_Tier_Web_Design_Guide.md` (Sajid) → [[wiki/sources/sajid-top-tier-web-design]]; updated [[wiki/app/ux-principles]] (4 dp grid, de-emphasis, zoom-out test)
- [x] Ingest `raw/TFI.pdf` → [[wiki/sources/ohsu-tfi-2012]], [[wiki/clinical/tfi]]
- [x] Ingest `raw/NR21-001-12.pdf` (NECA TRT HTA) → [[wiki/sources/neca-2022-trt]]; updated TRT/sound-therapy/CBT/habituation/tinnitus pages
- [x] Ingest `raw/a-korean-adaptation-of-the-tinnitus-magnitude-index-validity-12jyfduy7w.pdf` → [[wiki/sources/hwang-bahng-2018-ktmi]], [[wiki/clinical/tmi]]
- [x] Created [[wiki/clinical/thi]] assessment page (referenced widely across the wiki; previously a broken wikilink)
- [x] Ingested `raw/gemini_chat/` (LLM design proposal) → [[wiki/sources/gemini-records-architecture-2026-05]]; divergences surfaced + triaged (2026-05-14): min-session raised 5 s → 3 min ([[wiki/app/plan]] Next #1); per-sound segments instead of dominant-label ([[wiki/app/plan]] Next #2); 60-s pause debounce deferred (no pause UI exists yet)
- [ ] Triage `LiveSessionPill` floating in-app tracking indicator — is this a Next item on [[wiki/app/plan]]?
- [ ] Triage Quick-Log Bottom Sheet for `DiaryEntry` capture — pair with calendar day-tap drilldown ([[wiki/app/plan]] Next #3)?
- [ ] Find & ingest a validated Korean TFI (K-TFI) validation paper before shipping TFI in-app
- [ ] Decide on OHSU TFI permission/licensing path before app ship — required for verbatim Korean item wording
- [ ] Decide whether to surface THI in the app at all (current default: TFI primary + TMI companion, no THI)
- [ ] Apply state-variation coverage from [[wiki/app/ux-principles]] §6 to 소리 and 기록 screen designs in [[wiki/app/navigation]]
- [ ] Ingest any remaining documents in the `raw/` directory (e.g. guidelines, papers, transcripts) as they are added.
- [ ] Review all pages in `wiki/conditions/` and `wiki/therapies/` for missing `sources` references.
- [ ] Create missing concept pages for items referenced multiple times (e.g., hyperacusis, misophonia).
- [ ] Lint the wiki for broken links, orphan pages, or contradictions between newly ingested sources and old claims.
- [ ] Refine the structure of `wiki/app/` to ensure all product surfaces and features are documented and backed by clinical references.
- [ ] Review and expand `wiki/app/safety/red-flags` to ensure it is comprehensive and explicitly referenced by all related condition pages.
