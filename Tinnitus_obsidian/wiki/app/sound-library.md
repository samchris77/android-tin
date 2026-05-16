---
title: "Sound Library & Therapy Features"
type: app
aliases: ["Sound Library", "사운드 라이브러리", "Notched Sound Therapy"]
tags: [app/features]
evidence: moderate
status: stable
sources: ["[[wiki/sources/app-strategy-summary]]", "[[wiki/clinical/sound-therapy]]"]
last_reviewed: 2026-04-28
---

# Sound Library & Therapy Features

## Overview
The Sound Therapy module provides the patients with an auditory background engineered to reduce the salience of their tinnitus, ultimately fostering habituation. The library follows the principles of TRT: sounds must never be annoying, must be kept neutral, and crucially, MUST NOT completely mask the tinnitus.

## Core Feature: The Mixing Point Mixer
The app provides various background sounds (White noise, Pink noise, Brown noise, Rain, Wind, Sea waves). 
The user is instructed through the UX to set the volume to the **"Mixing Point"**. This is the point where the background sound and the tinnitus blend together, but the tinnitus is still audible. 
*Clinical Rationale*: Complete masking provides temporary relief but prevents the brain from habituating to the sound, as the brain cannot habituate to a signal it cannot perceive.

## Core Feature: Multilayering
Users can blend up to 5 sound layers. For instance, layering Pink Noise with Rain and a slight Fireplace crackle. The goal is to provide enough acoustic density to raise the background noise floor without triggering annoyance.

## Differentiation Feature: Notched Sound Therapy (Tinnitus Tuner)
If the user chooses to use Notched Sound Therapy, they must first use the **Tinnitus Tuner**.
### 1. Tinnitus Tuner
An interactive tool that plays sweeps of frequencies to help the user identify their exact tinnitus pitch. It should include careful checks for octave confusion (since patients often pick a pitch one octave above or below their true tinnitus).

### 2. The Notch Filter
Once the frequency ($f$) is found, the app applies a 1-octave bandstop filter (a notch) centered on that frequency to any uploaded music or the built-in white/pink noises. 
*Clinical Rationale (Okamoto et al. PNAS 2010)*: Removing the tinnitus frequency from the acoustic environment drives lateral inhibition in the auditory cortex, physically suppressing the hyperactive neurons representing the tinnitus frequency.

## Sleep Support
Since tinnitus perception surges in quiet environments, a Sleep Mode is provided with an 8+ hour timer and a gentle fade-out, utilizing calming soundscapes tailored for rest.  

## Implementation Notes
- All audio output logic must include volume limiters, keeping it strictly below hazardous limits to prevent worsening hearing loss or triggering hyperacusis.
- The UI must regularly remind patients to check if they are masking their tinnitus and advise them to lower the volume if so.
