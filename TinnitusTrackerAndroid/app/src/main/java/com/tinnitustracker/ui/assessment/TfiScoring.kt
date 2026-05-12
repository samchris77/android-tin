package com.tinnitustracker.ui.assessment

import kotlin.math.roundToInt

/**
 * Pure-Kotlin TFI (Tinnitus Functional Index) scoring.
 *
 * The questionnaire collects 25 items, each answered on a 0..10 scale. Total
 * score = mean of valid items × 10 → 0..100. Subscale scores follow the same
 * formula per subscale group. Per the TFI spec, the total is "valid" only when
 * at least 19 items are answered — the questionnaire UI enforces this by
 * disabling "다음" until every question on the page has a value, but the helper
 * carries the flag defensively.
 *
 * The TFI source instrument uses a 0..100 percentage scale for items #1 and
 * #3 (rescaled by ÷10 before averaging). Our UI shows 0..10 for every item
 * uniformly, with anchor labels conveying the percentage meaning — so no
 * per-item rescaling happens here.
 */
object TfiScoring {

    /** Subscale code → item numbers (1-indexed, matching the TFI spec). */
    val Subscales: Map<String, List<Int>> = linkedMapOf(
        "I"  to listOf(1, 2, 3),        // Intrusive
        "SC" to listOf(4, 5, 6),        // Sense of Control
        "C"  to listOf(7, 8, 9),        // Cognitive
        "SL" to listOf(10, 11, 12),     // Sleep
        "A"  to listOf(13, 14, 15),     // Auditory
        "R"  to listOf(16, 17, 18),     // Relaxation
        "Q"  to listOf(19, 20, 21, 22), // Quality of Life (4 items)
        "E"  to listOf(23, 24, 25)      // Emotional
    )

    /** Korean display labels for the 8 subscales. */
    val SubscaleKoreanLabels: Map<String, String> = mapOf(
        "I"  to "침투성",
        "SC" to "통제감",
        "C"  to "인지",
        "SL" to "수면",
        "A"  to "청각",
        "R"  to "휴식",
        "Q"  to "삶의 질",
        "E"  to "정서"
    )

    data class Scored(
        val total: Int,                    // 0..100
        val subscales: Map<String, Int>,   // 0..100 each
        val isValidTotal: Boolean          // false if < 19 of 25 items answered
    )

    fun score(items: Map<Int, Int>): Scored {
        if (items.isEmpty()) {
            return Scored(
                total = 0,
                subscales = Subscales.mapValues { 0 },
                isValidTotal = false
            )
        }

        val total = (items.values.average() * 10).roundToInt().coerceIn(0, 100)

        val subscales = Subscales.mapValues { (_, itemNums) ->
            val vals = itemNums.mapNotNull { items[it] }
            if (vals.isEmpty()) 0
            else (vals.average() * 10).roundToInt().coerceIn(0, 100)
        }

        return Scored(
            total = total,
            subscales = subscales,
            isValidTotal = items.size >= 19
        )
    }

    /**
     * TFI severity bands (Meikle et al. 2012) translated to the Korean register
     * established by `wiki/app/architecture.md` and the clinical wiki pages.
     *
     * **Pending clinician review** — bands themselves are well-established
     * (0–17, 18–31, 32–53, 54–72, 73–100); the Korean phrasing is our draft.
     */
    fun severityKorean(total: Int): String = when {
        total <= 17 -> "경미"
        total <= 31 -> "경도"
        total <= 53 -> "중등도"
        total <= 72 -> "심함"
        else        -> "매우 심함"
    }
}
