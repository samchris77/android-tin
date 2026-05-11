package com.tinnitustracker.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.DarkBg
import com.tinnitustracker.ui.theme.DarkCard
import com.tinnitustracker.ui.theme.OrangeAccent
import com.tinnitustracker.ui.theme.TextPrimary
import com.tinnitustracker.ui.theme.TextSecondary
import com.tinnitustracker.ui.theme.TextTertiary
import kotlinx.coroutines.launch

/**
 * 7-page onboarding tutorial. Copy is synthesized from the wiki — each page
 * cites its source `wiki/clinical/<file>.md` so the synthesis is traceable.
 * Section 1 (pages 1-3): what tinnitus is + why understanding matters.
 * Section 2 (pages 4-6): therapy approaches available.
 * Section 3 (page 7):    how to navigate this app.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel
) {
    val pages = remember { onboardingPages() }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val finish: () -> Unit = { vm.completeOnboarding(onPersisted = onFinished) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        TopBar(
            currentIndex = pagerState.currentPage,
            totalPages = pages.size,
            onSkip = finish
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) { index ->
            OnboardingPageContent(pages[index])
        }

        BottomBar(
            currentIndex = pagerState.currentPage,
            totalPages = pages.size,
            onPrev = {
                scope.launch {
                    if (pagerState.currentPage > 0) {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            onNext = {
                scope.launch {
                    val next = pagerState.currentPage + 1
                    if (next < pages.size) pagerState.animateScrollToPage(next)
                }
            },
            onFinish = finish
        )
    }
}

// ── Layout ────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(currentIndex: Int, totalPages: Int, onSkip: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${currentIndex + 1} / $totalPages",
            color = TextTertiary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.weight(1f))
        Text(
            "건너뛰기",
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onSkip)
                .semantics { contentDescription = "건너뛰기" }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    // Each clinical/therapy page is a synthesis from the cited wiki source.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))
        SectionBadge(page.section)
        Spacer(Modifier.height(20.dp))
        Text(
            page.title,
            color = TextPrimary,
            fontSize = 26.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.3).sp
        )
        Spacer(Modifier.height(24.dp))
        DarkCardBlock {
            page.paragraphs.forEachIndexed { i, paragraph ->
                if (i > 0) Spacer(Modifier.height(14.dp))
                Text(
                    paragraph,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SectionBadge(section: OnboardingSection) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(OrangeAccent.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            section.label,
            color = OrangeAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
private fun DarkCardBlock(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkCard)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) { Column { content() } }
}

@Composable
private fun BottomBar(
    currentIndex: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val isLast = currentIndex == totalPages - 1
    val isFirst = currentIndex == 0

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
        PageDots(currentIndex, totalPages)
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "이전",
                color = if (isFirst) TextTertiary.copy(alpha = 0.4f) else TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .let { if (isFirst) it else it.clickable(onClick = onPrev) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
            Spacer(Modifier.weight(1f))
            PrimaryButton(
                label = if (isLast) "시작하기" else "다음",
                onClick = if (isLast) onFinish else onNext
            )
        }
    }
}

@Composable
private fun PageDots(currentIndex: Int, totalPages: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { i ->
            val active = i == currentIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (active) OrangeAccent else TextTertiary.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(OrangeAccent)
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp)
            .semantics { contentDescription = label }
    ) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Content (synthesized from wiki/clinical sources) ──────────────────────

private enum class OnboardingSection(val label: String) {
    Tinnitus("1. 이명에 대해"),
    Therapy ("2. 치료 접근"),
    AppGuide("3. 앱 사용법")
}

private data class OnboardingPage(
    val section: OnboardingSection,
    val title: String,
    val paragraphs: List<String>
)

private fun onboardingPages(): List<OnboardingPage> = listOf(
    // Source: wiki/clinical/tinnitus.md
    OnboardingPage(
        section = OnboardingSection.Tinnitus,
        title = "이명은 무엇인가요?",
        paragraphs = listOf(
            "이명은 외부에서 소리가 없는데 들리는 소리입니다. \"삐\", \"윙\", \"쉭\" 같은 잡음이 가장 흔합니다.",
            "한국 성인 약 5명 중 1명이 이명을 경험합니다. 하지만 일상생활에 큰 지장을 줄 만큼 심한 경우는 10% 이내입니다.",
            "현재의 의학은 이명을 \"귀의 문제\"가 아니라, 청각 신호를 처리하는 뇌의 활동이 만들어내는 신호로 봅니다."
        )
    ),
    // Source: wiki/clinical/central-gain.md
    OnboardingPage(
        section = OnboardingSection.Tinnitus,
        title = "왜 들리는 걸까요?",
        paragraphs = listOf(
            "청각 손상이나 노화로 귀에서 뇌로 가는 신호가 줄어들면, 뇌는 부족한 입력을 보상하려고 청각 신경의 감도를 자동으로 높입니다. 이를 \"중추 이득(central gain)\"이라고 합니다.",
            "그 결과, 실제로 외부 소리가 없어도 뇌가 신호를 만들어 인식하게 됩니다. 이것이 우리가 듣는 이명입니다.",
            "그래서 청력 검사가 정상으로 나와도 이명이 들릴 수 있고, 귀 손상이 회복되어도 이명이 계속될 수 있습니다."
        )
    ),
    // Source: wiki/clinical/habituation.md, wiki/clinical/triple-network-model.md
    OnboardingPage(
        section = OnboardingSection.Tinnitus,
        title = "치료의 목표는 \"습관화\"",
        paragraphs = listOf(
            "이명 치료의 일차 목표는 소리를 없애는 것이 아니라, 뇌가 그 소리를 \"중요하지 않은 신호\"로 분류하도록 다시 가르치는 것입니다. 이를 습관화(habituation)라고 합니다.",
            "우리 뇌는 자동차 소음, 시계 초침 소리처럼 의미 없는 신호는 자동으로 무시합니다. 이명도 같은 방식으로 배경으로 보낼 수 있습니다.",
            "스트레스, 불안, 그리고 \"이 소리에 집중해서 듣는 것\"은 이명을 더 두드러지게 만듭니다. 치료는 이 악순환을 끊는 데 있습니다."
        )
    ),
    // Source: wiki/clinical/trt.md
    OnboardingPage(
        section = OnboardingSection.Therapy,
        title = "TRT — 12–24개월의 여정",
        paragraphs = listOf(
            "이명 재훈련 치료(TRT, Tinnitus Retraining Therapy)는 만성 이명에 대해 임상 근거가 가장 잘 정리된 접근법입니다. 지시적 상담과 사운드 치료를 함께 사용합니다.",
            "뇌의 신경 가소성이 바뀌는 데는 시간이 걸립니다. 보통 6–18개월의 꾸준한 실천 후에 변화를 체감하기 시작하고, 전체 과정은 12–24개월을 목표로 합니다.",
            "빠른 해결책이 아니라 길고 일관된 여정입니다. 이 사실을 미리 아는 것이 중도 포기를 막는 가장 큰 요인입니다."
        )
    ),
    // Source: wiki/clinical/sound-therapy.md
    OnboardingPage(
        section = OnboardingSection.Therapy,
        title = "사운드 치료: 마스킹이 아닙니다",
        paragraphs = listOf(
            "사운드 치료는 이명을 \"덮어버리는\" 것이 아닙니다. 이명이 안 들릴 만큼 큰 소리를 들으면, 뇌는 이명 신호를 다시 처리할 기회를 잃어 습관화가 일어나지 않습니다.",
            "올바른 방법은 이명이 살짝 들리는 수준 — 이를 혼합점(mixing point) 아래라고 합니다 — 에서 배경음을 길게(하루 8시간 이상) 듣는 것입니다.",
            "가장 흔한 실수가 볼륨을 너무 크게 키우는 것입니다. 편안하고 작게 시작하세요."
        )
    ),
    // Source: wiki/clinical/cbt.md
    OnboardingPage(
        section = OnboardingSection.Therapy,
        title = "인지행동치료(CBT)와 보조 요법",
        paragraphs = listOf(
            "인지행동치료(CBT)는 이명으로 인한 스트레스, 수면 문제, 불안에 대해 현재 가장 강한 임상 근거를 가진 심리 치료입니다.",
            "\"이 소리가 나를 미치게 한다\" 같은 자동적인 부정 사고를 인식하고 다르게 해석하는 훈련이 핵심입니다.",
            "TRT와 CBT는 서로 보완적입니다. 사운드 치료와 함께 진행할 때 효과가 더 큽니다."
        )
    ),
    // Source: implemented Android surfaces (MainActivity, FrequencyMatchingScreen)
    OnboardingPage(
        section = OnboardingSection.AppGuide,
        title = "이 앱 사용법",
        paragraphs = listOf(
            "이 앱에는 두 개의 탭이 있습니다. \"톤 찾기\"에서는 본인의 이명과 비슷한 주파수를 직접 찾고, 그 주파수를 기준으로 노치(notch) 또는 증폭(amplify) 모드로 사운드 치료를 바로 시작할 수 있습니다.",
            "\"설정\"에서는 앱 정보를 확인하고 이 튜토리얼을 다시 볼 수 있습니다.",
            "권장 순서: 먼저 톤 찾기에서 본인의 이명 주파수를 확인하세요. 그다음 혼합점 아래의 편안한 볼륨에서 재생을 시작하세요."
        )
    )
)

