package com.tinnitustracker.ui.assessment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.pressableClickable
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch

/**
 * 8-page subscale-grouped TFI questionnaire. Each page holds 3–4 items
 * sharing one subscale; the user picks 0–10 per item. "다음" stays disabled
 * until the current page is complete; final page becomes "제출하기".
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TfiQuestionnaireScreen(
    vm: TfiQuestionnaireViewModel,
    onClose: () -> Unit,
    onSubmitted: () -> Unit
) {
    val pages = TfiContent.Pages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    BackHandler { onClose() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        TopBar(
            current = pagerState.currentPage + 1,
            total = pages.size,
            onClose = onClose
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(0.dp)
        ) { index ->
            PageContent(pages[index], vm)
        }

        BottomBar(
            currentPage = pagerState.currentPage,
            totalPages = pages.size,
            isPageComplete = vm.isPageComplete(pagerState.currentPage),
            isAllComplete = vm.isAllComplete(),
            onNext = {
                scope.launch {
                    if (pagerState.currentPage < pages.size - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            onSubmit = { vm.submit(onDone = onSubmitted) }
        )
    }
}

@Composable
private fun TopBar(current: Int, total: Int, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "← 닫기",
                color = Ink2,
                fontSize = 13.sp,
                modifier = Modifier
                    .pressableClickable(onClick = onClose)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .semantics { contentDescription = "닫기" }
            )
            Text(
                "$current / $total",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(Spacing.md))
        // Thin progress bar.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Line)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(current.toFloat() / total)
                    .height(3.dp)
                    .background(Teal, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun PageContent(page: TfiContent.Page, vm: TfiQuestionnaireViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Instruction stem (shared across all 25 items).
        Text(
            "지난 한 주 동안…",
            color = Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        // Subscale eyebrow.
        Text(
            page.subscaleLabel,
            color = Coral,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(16.dp))

        page.items.forEach { item ->
            QuestionCard(
                item = item,
                selected = vm.responses[item.number],
                onSelect = { vm.setResponse(item.number, it) }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun QuestionCard(item: TfiContent.Item, selected: Int?, onSelect: (Int) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Line, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                "${item.number}. ${item.prompt}",
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(Spacing.lg))
            ScaleRow(selected = selected, onSelect = onSelect)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.anchorLow, color = Muted, fontSize = 10.5.sp)
                Text(item.anchorHigh, color = Muted, fontSize = 10.5.sp)
            }
        }
    }
}

@Composable
private fun ScaleRow(selected: Int?, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (0..10).forEach { value ->
            ScaleButton(
                value = value,
                isSelected = selected == value,
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScaleButton(
    value: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Box(
            modifier = modifier
                .pressableClickable(onClick = onClick)
                .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Teal.copy(alpha = 0.45f))
                .background(Teal, RoundedCornerShape(8.dp))
                .height(36.dp)
                .semantics { contentDescription = "$value 선택됨" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$value",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Box(
            modifier = modifier
                .pressableClickable(onClick = onClick)
                .background(Color.Transparent, RoundedCornerShape(8.dp))
                .height(36.dp)
                .semantics { contentDescription = "$value" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$value",
                color = Ink2,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomBar(
    currentPage: Int,
    totalPages: Int,
    isPageComplete: Boolean,
    isAllComplete: Boolean,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    val isLastPage = currentPage == totalPages - 1
    val ctaEnabled = if (isLastPage) isAllComplete else isPageComplete
    val ctaLabel = if (isLastPage) "제출하기" else "다음 →"
    val ctaAction = if (isLastPage) onSubmit else onNext

    Surface(
        color = Bg,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            CtaButton(label = ctaLabel, enabled = ctaEnabled, onClick = ctaAction)
        }
    }
}

@Composable
private fun CtaButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val bg = if (enabled) Teal else TealSoft
    val fg = if (enabled) Color.White else Muted
    Box(
        modifier = Modifier
            .let { if (enabled) it.pressableClickable(onClick = onClick) else it }
            .let {
                if (enabled) it.shadow(3.dp, RoundedCornerShape(12.dp), spotColor = Teal.copy(alpha = 0.45f))
                else it
            }
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .semantics { contentDescription = label }
    ) {
        Text(
            label,
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
