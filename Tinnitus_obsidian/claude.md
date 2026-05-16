# CLAUDE.md — Tinnitus / TRT Knowledge Wiki

Schema file for an LLM-maintained Obsidian wiki backing a digital companion app for people pursuing Tinnitus Retraining Therapy and adjacent interventions. Inspired by Karpathy's LLM Wiki pattern.

## 1. Purpose

This vault is the **canonical knowledge base** for the app. It is the single source of truth for:

- Clinical explainers shown to users (tinnitus mechanisms, TRT phases, sound therapy rationale)
    
- In-app FAQ answers and microcopy
    
- Exercise / module rationale documentation
    
- Decision logic for personalisation (e.g. when to suggest hearing-aid evaluation vs. sound generator)
    
- Team reference for product, design, and clinical review
    

**Audience hierarchy (most → least important):**

1. The LLM agent maintaining this wiki
    
2. Internal clinicians reviewing content
    
3. Product / content / design team
    

_(Patient-facing copy is **generated from** this wiki — never authored directly here.)_

**Hard constraint** — this is medical content. Accuracy, evidence level, and the line between _established practice_ and _emerging research_ are non-negotiable. When uncertain, **flag rather than synthesize**. Never invent a citation. Never soften a strength-of-evidence rating to make a page read more confidently.

---

## 2. Directory layout

Plaintext

```
/
├── CLAUDE.md              ← this file
│
├── raw/                   ← IMMUTABLE source documents (LLM reads, never edits)
│   ├── assets/            ← audiograms, diagrams, screenshots, images
│   └── (No other empty folders. Only create folders like papers/ or guidelines/ when a file actually exists)
│
└── wiki/                  ← markdown pages maintained by Claude
    ├── index.md           ← table of contents for the entire wiki
    ├── log.md             ← append-only record of all operations
    ├── tasks.md           ← birds eye view of wiki tasks (ALWAYS update as project progresses)
    ├── clinical/          ← all clinical pages (conditions, therapies, concepts) — disambiguated by `type` frontmatter
    ├── sources/           ← one summary page per ingested source
    └── app/               ← product / Android app docs (flat — no subfolders); downstream of clinical
```

_Folders are flat by design. Don't add subfolders inside `clinical/`, `sources/`, or `app/` — `type` and `tags` in frontmatter are the disambiguators, surfaced via `index.md` and Dataview queries._

---

## 3. Page conventions

### 3.1 Every page has YAML frontmatter

YAML

```
---
title: "Tinnitus Retraining Therapy"
type: therapy            # condition | therapy | concept | assessment | medication | person | organization | source | synthesis | app
aliases: ["TRT", "Jastreboff protocol"]
tags: [therapy/sound, therapy/counseling, evidence/moderate]
evidence: moderate       # high | moderate | low | mixed | anecdotal | n/a
status: stable           # draft | stable | needs-review | contested
sources: ["[[wiki/sources/jastreboff-2004]]", "[[wiki/sources/cochrane-trt-2010]]"]
last_reviewed: 2026-04-28
---
```

`tags` and `evidence` are queryable via Dataview. Use `evidence/` and `therapy/` style nested tags.

### 3.2 Wikilinks, not markdown links, for internal references

Use `[[wiki/clinical/trt]]` not `[wiki/clinical/trt](wiki/clinical/trt.md)`. Obsidian's graph view depends on this.

### 3.3 Page templates

**Condition page (`wiki/clinical/*`, `type: condition`)**

Markdown

```
# {Name}

## TL;DR
One-paragraph plain-English summary suitable for adapting into patient copy.

## Definition & diagnostic criteria
Formal clinical definition. Cite source.

## Mechanisms (current understanding)
What's known, what's hypothesized — keep these separated.

## Subtypes / variants
Wikilinks to related condition pages.

## Comorbidities
[[wiki/clinical/hyperacusis]], [[wiki/clinical/anxiety]], etc.

## Assessment
Wikilinks to relevant [[wiki/assessments/*]] pages.

## Standard interventions
Wikilinks to [[wiki/clinical/*]] with one-line rationale each.

## Open questions / contested claims
Where the literature disagrees. THIS SECTION IS REQUIRED — write "None known" if empty.

## Sources
Wikilink list to `wiki/sources/*` pages.
```

**Therapy page (`wiki/clinical/*`, `type: therapy`)**

Markdown

```
# {Name}

## TL;DR

## What it is
Procedure, not theory.

## Theoretical basis
Wikilink to relevant [[wiki/clinical/*]] (e.g. [[wiki/clinical/habituation]]).

## Evidence
- Strength of evidence: {high/moderate/low/mixed}
- Key trials: wikilinks to source pages
- Effect sizes when reported, with CIs
- Limitations of the evidence base

## Indications / contraindications

## How it's delivered
Sessions, duration, who delivers it, typical cost.

## Common confusions
**Critical for this domain.** E.g. "TRT is often confused with simple sound masking — distinguish them here."

## App relevance
How (if at all) this maps to features in [[wiki/app/*]].

## Sources
```

**Concept page (`wiki/clinical/*`, `type: concept`)** — short, focused, one mechanism per page.

**Source summary (`wiki/sources/*`)** — one per ingested source:

Markdown

```
# {Citation}

- **Type:** RCT | review | guideline | book | article | podcast | case study
- **Year:** - **Population / N:** - **Intervention:** - **Comparator:** - **Primary outcome:** - **Result:** - **Risk of bias / quality:** - **Updates this caused in the wiki:** wikilink list

## Key findings
3–7 bullets, plain English.

## Quotes worth preserving
Short verbatim quotes (cite page numbers).

## My / our take
Editorial — what this means for the app.
```

---

## 4. Evidence discipline (domain-specific)

Tinnitus has a long tail of low-quality studies, marketing-as-research, and pseudoscientific products. Be ruthless.

**Evidence levels** — apply per-claim, not just per-page:

- `high` — multiple well-powered RCTs or Cochrane review with consistent findings
    
- `moderate` — single RCT, or several smaller controlled studies, or strong mechanistic + clinical convergence
    
- `low` — observational, small, or methodologically weak
    
- `mixed` — meaningful disagreement in the literature
    
- `anecdotal` — case reports, expert opinion, patient testimony
    

**Always distinguish:**

- TRT (Jastreboff protocol: directive counseling + sound therapy over ~12–24 months) vs. generic sound masking
    
- Tinnitus vs. hyperacusis vs. misophonia — overlap exists, but they are different
    
- Habituation of _reaction_ vs. habituation of _perception_ (TRT primarily targets the former)
    
- "Cure" claims vs. management / habituation outcomes
    
- Subjective vs. objective tinnitus (only the latter has a sound source)
    
- Acute (<6 months, may resolve) vs. chronic tinnitus
    

**Watchlist — flag and treat skeptically:**

- Supplements marketed for tinnitus (lipoflavonoids, ginkgo, NAD+, etc.)
    
- Devices with unblinded trials only
    
- "Neuromodulation" claims without sham-controlled data
    
- Stem cell / regenerative claims
    
- App-based interventions citing only their own internal data
    

_When a source falls in the watchlist, the source page must include a **Quality concerns** section._

---

## 5. Workflows

### 5.1 Ingest

When the user drops a new file into `raw/` and asks to ingest:

1. Read the source end-to-end. For PDFs, extract figures/tables explicitly.
    
2. Briefly discuss key takeaways with the user before writing.
    
3. Create `wiki/sources/{slug}.md` using the source template.
    
4. Identify which existing pages this source touches. Update them:
    
    - Add the source to their `sources` frontmatter.
        
    - Update claims, evidence levels, or open questions as warranted.
        
    - If the source contradicts an existing claim, do not silently overwrite. Add to the page's "Open questions / contested claims" and surface to the user.
        
5. Create new pages for any entity / concept introduced that warrants its own page (rule of thumb: mentioned in ≥2 sources OR central to one source).
    
6. Update `wiki/index.md` and `wiki/tasks.md` (to reflect any new tasks or completed ones).
    
7. Append a `wiki/log.md` entry: `## [YYYY-MM-DD] ingest | {source title}` followed by a bulleted list of pages touched.
    

_A typical ingest touches 5–15 pages. If you only touched 1–2, you probably under-integrated — re-check._

### 5.2 Query

When the user asks a question:

1. Read `wiki/index.md` first to locate relevant pages.
    
2. Read those pages (don't go straight to `raw/` — the wiki is the synthesis).
    
3. Answer with wikilink citations to wiki pages and source citations to `wiki/sources/*`.
    
4. If the answer is **novel synthesis** (a comparison, a connection, a decision tree) — offer to file it as `wiki/synthesis/{slug}.md`. Don't let valuable analysis evaporate into chat history.
    
5. Append to `wiki/log.md`: `## [YYYY-MM-DD] query | {short description}`.
    
6. If the wiki can't support a confident answer, say so explicitly and suggest sources to ingest.
    

### 5.3 Lint (run on request, ~weekly)

Health-check the wiki and produce a report. Look for:

- **Contradictions** between pages
    
- **Stale claims** — pages whose `last_reviewed` predates a newer source on the same topic
    
- **Orphans** — pages with zero inbound wikilinks
    
- **Stubs** — pages under ~150 words that should be expanded or merged
    
- **Missing pages** — concepts referenced ≥3 times across the wiki without their own page
    
- **Evidence drift** — claims labeled `high` evidence that on re-read are actually `moderate`
    
- **Watchlist creep** — content from low-quality sources that has migrated into authoritative-sounding wiki prose without proper hedging
    
- **Citation gaps** — claims with no `sources` link
    
- **App alignment** — `wiki/app/*` pages whose clinical claims aren't backed by a `wiki/clinical/` page (`type: therapy` or `type: condition`)
    

Output as a checklist. Do not fix without the user's go-ahead.

---

## 6. `wiki/index.md` and `wiki/log.md`

`wiki/index.md` is content-oriented. Sections:

Markdown

```
## Conditions
- [[wiki/clinical/subjective-tinnitus]] — most common form; phantom auditory perception without external source
- [[wiki/clinical/pulsatile-tinnitus]] — rhythmic, often vascular origin; requires medical workup
...

## Therapies
...

## Sources (chronological, newest first)
- [[wiki/sources/{slug}]] — {one-line takeaway} ({year})
```

Update on every ingest. Keep one-liners genuinely informative — they are how the LLM finds pages on future queries.

`wiki/log.md` is chronological, append-only, parseable:

Markdown

```
## [2026-04-28] ingest | Jastreboff & Hazell, Tinnitus Retraining Therapy (2004)
- Created [[wiki/sources/jastreboff-hazell-2004]]
- Updated [[wiki/clinical/trt]], [[wiki/clinical/habituation]], [[wiki/clinical/neurophysiological-model]]
- Created [[wiki/people/pawel-jastreboff]]

## [2026-04-28] query | difference between TRT and CBT for tinnitus
- Filed answer as [[wiki/synthesis/trt-vs-cbt]]

## [2026-04-29] lint | weekly
- Found 2 orphan pages, 1 contradiction, 4 stubs — see report
```

Consistent `## [date] {action} | {title}` prefix — keeps `grep "^## \[" wiki/log.md | tail -20` useful.

---

## 7. App-specific notes (`wiki/app/`)

`wiki/app/` is **flat** — no subfolders. Every product doc lives directly in `wiki/app/`. Disambiguate by filename and `tags` frontmatter (e.g. `app/strategy`, `app/features`, `app/safety`, `app/planning`).

Canonical pages:

```
wiki/app/
├── prd.md              ← what the app is and isn't
├── architecture.md     ← Android codebase + data-model architecture
├── plan.md             ← implementation plan (one active task at a time)
├── android-snapshot.md ← current-state code snapshot
├── red-flags.md        ← medical red flags (sacred — see below)
├── sound-library.md    ← feature page
└── cbt-program.md      ← module page
```

`wiki/app/red-flags.md` is sacred. Pulsatile tinnitus, sudden unilateral hearing loss, vertigo, neurological symptoms — these need ENT/medical referral, not app content. This page must always exist, must always be linked from any relevant condition page, and must never be softened.

App pages should always wikilink to the clinical pages backing their claims. If `wiki/app/sound-therapy-intro.md` makes a claim, that claim should be supported by `wiki/clinical/sound-therapy.md` or a specific source. **No clinical claim originates in `wiki/app/`** — `wiki/app/` is downstream of the clinical layer.

---

## 8. Style for LLM-authored prose

- **Plain English first.** A motivated patient should understand the TL;DR. Jargon comes after, with definitions.
    
- **Hedge appropriately, but stop there.** "Some studies suggest" is fine when accurate. Don't pile on "may", "could", "might" until the sentence says nothing.
    
- **Active voice.** "TRT combines counseling with sound therapy" not "TRT is comprised of...".
    
- **No marketing tone.** This is reference material, not a landing page.
    
- **Numbers matter.** When effect sizes, sample sizes, follow-up durations, or dropout rates are reported in a source, preserve them. Don't summarize away the quantitative content.
    
- **Distinguish facts from framing.** "The Jastreboff model proposes…" not "tinnitus is caused by…".
    

---

## 9. What the human does vs. what the LLM does

**Human (you):**

- Curates `raw/` — chooses what gets ingested
    
- Asks questions, directs analysis
    
- Reviews wiki updates and pushes back
    
- Owns final clinical judgment — especially anything that flows into patient-facing copy
    
- Co-evolves this `CLAUDE.md` as patterns emerge
    

**LLM (me):**

- Reads sources, writes summaries
    
- Creates and maintains all `wiki/` pages
    
- Maintains `wiki/index.md`, `wiki/log.md`, `wiki/tasks.md`, cross-references
    
- Flags contradictions, stale claims, evidence drift
    
- Suggests new sources to seek, new pages to create
    
- Never edits `raw/`. Never invents citations. Never publishes patient-facing copy without explicit human review.
    

---

## 10. Open conventions to settle

These are deliberately unresolved — fill in as the wiki matures:

- Frontmatter for `wiki/app/*` pages — what fields do product/design need?
    
- How to handle non-English sources (translate? note in frontmatter?)
    
- Whether to track THI/TFI cutoffs as structured data (Dataview-queryable) for in-app use
    
- Versioning strategy for clinical claims that get superseded — keep history inline or move to an archive folder?
    
- Image handling — do we want LLM-generated diagrams in `wiki/synthesis/`, or only human-curated visuals?
    

_Discuss with the user before unilaterally resolving any of these._

**This file is a living document. When workflow patterns recur or break, update `CLAUDE.md` before updating individual pages — the schema should always lead.**