package com.tinnitustracker.ui.assessment

/**
 * Static content for the TFI questionnaire — the 25 items, their Korean
 * prompts, and the Korean anchor labels for the 0 and 10 ends of the scale.
 *
 * Source: `raw/TFI.pdf` (Meikle et al. 2012). Translated to Korean for this
 * app. All prompts share an implicit "지난 한 주 동안…" stem rendered once at
 * the top of each page; per-item text omits it for readability.
 *
 * Items #1 and #3 are 0..100 percentage in the source instrument; we present
 * them with the same 0..10 button row as the rest of the items, with anchor
 * labels conveying the percentage meaning. See [TfiScoring] for the scoring
 * implication (no per-item rescaling needed).
 *
 * **Translations are a v1 draft** — flag for clinician / Korean-speaker
 * review before any patient-facing build.
 */
object TfiContent {

    data class Item(
        val number: Int,            // 1..25
        val subscale: String,       // matches keys in TfiScoring.Subscales
        val prompt: String,         // Korean question text
        val anchorLow: String,      // label under "0"
        val anchorHigh: String      // label under "10"
    )

    data class Page(
        val subscale: String,       // subscale code (I, SC, …)
        val subscaleLabel: String,  // Korean header
        val items: List<Item>
    )

    val Items: List<Item> = listOf(
        Item(1, "I",
            prompt = "이명을 의식하고 있던 시간의 비율은 어느 정도였나요?",
            anchorLow = "전혀 의식하지 않음",
            anchorHigh = "항상 의식함"
        ),
        Item(2, "I",
            prompt = "이명이 얼마나 강하거나 크게 들렸나요?",
            anchorLow = "전혀 강하지 않음",
            anchorHigh = "매우 강함"
        ),
        Item(3, "I",
            prompt = "이명 때문에 짜증이 났던 시간의 비율은 어느 정도였나요?",
            anchorLow = "전혀 없음",
            anchorHigh = "항상"
        ),
        Item(4, "SC",
            prompt = "이명에 대해 통제감을 느꼈나요?",
            anchorLow = "충분히 통제됨",
            anchorHigh = "전혀 통제되지 않음"
        ),
        Item(5, "SC",
            prompt = "이명에 대처하기가 얼마나 쉬웠나요?",
            anchorLow = "매우 쉬움",
            anchorHigh = "불가능"
        ),
        Item(6, "SC",
            prompt = "이명을 무시하기가 얼마나 쉬웠나요?",
            anchorLow = "매우 쉬움",
            anchorHigh = "불가능"
        ),
        Item(7, "C",
            prompt = "이명이 집중하는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(8, "C",
            prompt = "이명이 또렷하게 생각하는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(9, "C",
            prompt = "이명 외의 일에 주의를 기울이는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(10, "SL",
            prompt = "이명 때문에 잠들거나 잠을 유지하기 어려웠던 적이 얼마나 자주 있었나요?",
            anchorLow = "한 번도 없음",
            anchorHigh = "항상"
        ),
        Item(11, "SL",
            prompt = "이명 때문에 필요한 만큼 잠을 자기 어려웠던 적이 얼마나 자주 있었나요?",
            anchorLow = "한 번도 없음",
            anchorHigh = "항상"
        ),
        Item(12, "SL",
            prompt = "이명 때문에 깊고 평온하게 잠을 자지 못한 시간은 어느 정도였나요?",
            anchorLow = "전혀 없음",
            anchorHigh = "항상"
        ),
        Item(13, "A",
            prompt = "이명이 또렷하게 듣는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(14, "A",
            prompt = "이명이 상대방의 말을 이해하는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(15, "A",
            prompt = "이명이 모임이나 회의에서 대화를 따라가는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(16, "R",
            prompt = "이명이 조용한 휴식 활동에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(17, "R",
            prompt = "이명이 편안히 쉬는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(18, "R",
            prompt = "이명이 '평온함과 고요함'을 즐기는 능력에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(19, "Q",
            prompt = "이명이 사회 활동의 즐거움에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(20, "Q",
            prompt = "이명이 삶의 즐거움에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(21, "Q",
            prompt = "이명이 가족, 친구, 다른 사람과의 관계에 얼마나 방해가 되었나요?",
            anchorLow = "전혀 방해되지 않음",
            anchorHigh = "완전히 방해됨"
        ),
        Item(22, "Q",
            prompt = "이명 때문에 일이나 일상 업무(가사, 학업, 자녀 돌봄 등)를 수행하기 어려웠던 적이 얼마나 자주 있었나요?",
            anchorLow = "한 번도 없음",
            anchorHigh = "항상"
        ),
        Item(23, "E",
            prompt = "이명 때문에 얼마나 불안하거나 걱정스러웠나요?",
            anchorLow = "전혀 없음",
            anchorHigh = "극도로"
        ),
        Item(24, "E",
            prompt = "이명 때문에 얼마나 괴롭거나 속상했나요?",
            anchorLow = "전혀 없음",
            anchorHigh = "극도로"
        ),
        Item(25, "E",
            prompt = "이명 때문에 얼마나 우울했나요?",
            anchorLow = "전혀 없음",
            anchorHigh = "극도로"
        )
    )

    /** 8 pages, one per subscale, items in subscale order. */
    val Pages: List<Page> = TfiScoring.Subscales.entries.map { (code, _) ->
        val label = TfiScoring.SubscaleKoreanLabels.getValue(code)
        val items = Items.filter { it.subscale == code }
        Page(subscale = code, subscaleLabel = label, items = items)
    }

    /** Total number of pages (8 subscales). */
    val PageCount: Int get() = Pages.size
}
