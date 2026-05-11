package com.tinnitustracker.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.Teal2
import com.tinnitustracker.ui.theme.TealSoft
import kotlinx.coroutines.launch

/**
 * 7-page onboarding tutorial (Direction A — gradient hero + bottom card).
 * Dark teal gradient on the top half carries atmospheric waveform art and a
 * coral progress bar; a cream card slides up over it with the title + body
 * for the current page. Copy is synthesized from `wiki/clinical/<file>.md` —
 * each page cites its source for traceability.
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
    val currentPage = pages[pagerState.currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeroBaseDark)
    ) {
        Hero(
            currentIndex = pagerState.currentPage,
            totalPages = pages.size,
            sectionLabel = currentPage.section.label,
            section = currentPage.section,
            onSkip = finish
        )

        // Cream card overlapping the hero by -24dp.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = HERO_HEIGHT_DP.dp - 24.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Bg)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) { index ->
                OnboardingCardContent(pages[index])
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
}

private const val HERO_HEIGHT_DP = 320
private val HeroBaseDark = Color(0xFF0F1F1E)
private val HeroBaseDarkEnd = Color(0xFF1A2E2C)

// ── Hero ───────────────────────────────────────────────────────────────────

@Composable
private fun Hero(
    currentIndex: Int,
    totalPages: Int,
    sectionLabel: String,
    section: OnboardingSection,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT_DP.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(HeroBaseDark, HeroBaseDarkEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Soft coral wash bottom-left, soft teal-tone wash top-right.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Coral.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(0f, Float.POSITIVE_INFINITY),
                        radius = 700f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(TealSoft.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(Float.POSITIVE_INFINITY, 0f),
                        radius = 600f
                    )
                )
        )

        when (section) {
            OnboardingSection.Tinnitus -> WaveformArt(modifier = Modifier.fillMaxSize())
            OnboardingSection.Therapy -> BrainNetworkArt(modifier = Modifier.fillMaxSize())
            OnboardingSection.AppGuide -> CheckmarkArt(modifier = Modifier.fillMaxSize())
        }

        // Status-bar-respecting top row (Skip).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                "건너뛰기",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSkip)
                    .semantics { contentDescription = "건너뛰기" }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        // Bottom-anchored progress bar + page/section labels.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 42.dp)
        ) {
            ProgressBar(
                progress = (currentIndex + 1).toFloat() / totalPages.toFloat()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${currentIndex + 1} / $totalPages",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    sectionLabel,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WaveformArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Three wave paths layered across the vertical middle, low contrast.
        fun wave(baseline: Float, amplitude: Float): Path = Path().apply {
            moveTo(0f, baseline)
            val pts = 5
            for (i in 1..pts) {
                val x = w * i / pts.toFloat()
                val y = if (i % 2 == 0) baseline + amplitude else baseline - amplitude
                val cx = w * (i - 0.5f) / pts.toFloat()
                quadraticBezierTo(cx, y, x, baseline)
            }
        }
        val midY = h * 0.62f
        drawPath(
            wave(midY - 14.dp.toPx(), 18.dp.toPx()),
            color = Color.White.copy(alpha = 0.18f),
            style = Stroke(width = 1.2.dp.toPx())
        )
        drawPath(
            wave(midY, 22.dp.toPx()),
            color = Coral.copy(alpha = 0.55f),
            style = Stroke(width = 1.4.dp.toPx())
        )
        drawPath(
            wave(midY + 16.dp.toPx(), 14.dp.toPx()),
            color = Color.White.copy(alpha = 0.10f),
            style = Stroke(width = 1.0.dp.toPx())
        )
    }
}

@Composable
private fun BrainNetworkArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w * 0.5f
        val centerY = h * 0.55f
        val nodeRadius = 10.dp.toPx()
        val centerNodeRadius = 14.dp.toPx()
        val orbitRadius = 60.dp.toPx()

        val colors = listOf(
            Teal.copy(alpha = 0.55f),
            Teal2.copy(alpha = 0.55f),
            Teal.copy(alpha = 0.55f),
            Teal2.copy(alpha = 0.55f),
            Teal.copy(alpha = 0.55f),
            Teal2.copy(alpha = 0.55f)
        )

        // Draw connecting lines from center to each node
        for (i in 0 until 6) {
            val angle = (i * 60f - 90f) * (Math.PI / 180.0)
            val nodeX = centerX + (orbitRadius * Math.cos(angle)).toFloat()
            val nodeY = centerY + (orbitRadius * Math.sin(angle)).toFloat()
            drawLine(
                color = Teal.copy(alpha = 0.22f),
                start = Offset(centerX, centerY),
                end = Offset(nodeX, nodeY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw outer nodes
        for (i in 0 until 6) {
            val angle = (i * 60f - 90f) * (Math.PI / 180.0)
            val nodeX = centerX + (orbitRadius * Math.cos(angle)).toFloat()
            val nodeY = centerY + (orbitRadius * Math.sin(angle)).toFloat()
            drawCircle(
                color = colors[i],
                radius = nodeRadius,
                center = Offset(nodeX, nodeY)
            )
        }

        // Draw center node (coral)
        drawCircle(
            color = Coral.copy(alpha = 0.55f),
            radius = centerNodeRadius,
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
private fun CheckmarkArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w * 0.5f
        val centerY = h * 0.5f
        val radius = 50.dp.toPx()

        // Draw incomplete circle arc (270°)
        drawArc(
            color = Teal.copy(alpha = 0.35f),
            startAngle = -90f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw checkmark path
        val checkmarkPath = Path().apply {
            moveTo(centerX - 14.dp.toPx(), centerY + 2.dp.toPx())
            lineTo(centerX - 4.dp.toPx(), centerY + 12.dp.toPx())
            lineTo(centerX + 14.dp.toPx(), centerY - 8.dp.toPx())
        }
        drawPath(
            path = checkmarkPath,
            color = Coral.copy(alpha = 0.60f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw two small dots
        drawCircle(
            color = TealSoft.copy(alpha = 0.8f),
            radius = 4.dp.toPx(),
            center = Offset(w * 0.3f, h * 0.4f)
        )
        drawCircle(
            color = TealSoft.copy(alpha = 0.8f),
            radius = 4.dp.toPx(),
            center = Offset(w * 0.7f, h * 0.4f)
        )
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Coral)
        )
    }
}

// ── Card body ──────────────────────────────────────────────────────────────

@Composable
private fun OnboardingCardContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            page.title,
            color = Ink,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp
        )
        Spacer(Modifier.height(18.dp))
        page.paragraphs.forEachIndexed { i, paragraph ->
            if (i > 0) Spacer(Modifier.height(12.dp))
            Text(
                paragraph,
                color = Ink2,
                fontSize = 14.sp,
                lineHeight = 23.sp
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

// ── Bottom bar ─────────────────────────────────────────────────────────────

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "이전",
            color = if (isFirst) Muted.copy(alpha = 0.4f) else Muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .let { if (isFirst) it else it.clickable(onClick = onPrev) }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
        Spacer(Modifier.weight(1f))
        CoralPill(
            label = if (isLast) "시작하기" else "다음",
            onClick = if (isLast) onFinish else onNext
        )
    }
}

@Composable
private fun CoralPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Coral)
            .clickable(onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 12.dp)
            .semantics { contentDescription = label }
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Content (synthesized from wiki/clinical sources) ──────────────────────

private enum class OnboardingSection(val label: String) {
    Tinnitus("이명에 대해"),
    Therapy ("치료 접근"),
    AppGuide("앱 사용법")
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
    // Source: implemented Android surfaces (MainActivity + 4-tab shell)
    OnboardingPage(
        section = OnboardingSection.AppGuide,
        title = "이 앱 사용법",
        paragraphs = listOf(
            "이 앱에는 네 개의 탭이 있습니다. 홈은 일일 청취와 빠른 재생, 소리는 주파수 매칭과 사운드 설정, 기록은 청취 기록과 설문 추이, 설정은 앱과 학습 자료 조정입니다.",
            "주파수 매칭은 소리 탭의 \"주파수 매칭\" 카드를 눌러 시작할 수 있습니다. 일치하는 주파수를 찾으면 노치 또는 증폭 모드로 바로 사운드 치료를 재생합니다.",
            "권장 순서: 먼저 주파수 매칭을 마친 다음, 혼합점 아래의 편안한 볼륨에서 길게(하루 8시간 이상) 재생하세요."
        )
    )
)
