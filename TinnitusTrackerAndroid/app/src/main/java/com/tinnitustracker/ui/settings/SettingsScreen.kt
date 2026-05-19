package com.tinnitustracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.components.ChevronRow
import com.tinnitustracker.ui.theme.components.Pill
import com.tinnitustracker.ui.theme.components.PillSize
import com.tinnitustracker.ui.theme.components.PillVariant
import com.tinnitustracker.ui.theme.components.SegmentedControl
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val GOAL_OPTIONS_MIN = listOf(120, 240, 360)
private val GOAL_LABELS = listOf("2시간", "4시간", "6시간")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onReplayOnboarding: () -> Unit,
    onOpenTfi: () -> Unit,
    tfiCadenceWeeks: Int,
    onToggleTfiCadence: () -> Unit,
    treatmentStartDate: Long,
    onSetTreatmentStartDate: (Long) -> Unit,
    dailyGoalMin: Int,
    onSetDailyGoal: (Int) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("설정", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(Spacing.sm))
        Text("앱과 학습 자료를 조정합니다.", color = Muted, fontSize = 13.sp)

        Spacer(Modifier.height(Spacing.xxl))
        SectionLabel("학습 자료")
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "튜토리얼 다시 보기", onClick = onReplayOnboarding) }
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "이명 메커니즘 다시 읽기", subtitle = "준비 중", onClick = {}, enabled = false) }

        Spacer(Modifier.height(Spacing.xxl))
        // 청취 목표 section header ripped 2026-05-19 — rows below are self-labeling.
        Card {
            DailyGoalRow(currentMin = dailyGoalMin, onSetGoal = onSetDailyGoal)
        }
        Spacer(Modifier.height(Spacing.sm))
        Card {
            ChevronRow(
                title = "치료 시작일",
                subtitle = formatStartDate(treatmentStartDate),
                onClick = { showDatePicker = true }
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Card {
            ChevronRow(
                title = "TFI 주기",
                onClick = onToggleTfiCadence,
                showChevron = false,
                trailing = {
                    Pill(text = "${tfiCadenceWeeks}주마다", variant = PillVariant.TealSoft, size = PillSize.Small)
                }
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "TFI 다시 작성", onClick = onOpenTfi) }

        Spacer(Modifier.height(Spacing.xxl))
        SectionLabel("주파수")
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "주파수 다시 측정", subtitle = "준비 중", onClick = {}, enabled = false) }
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "현재 모드", subtitle = "노치 · 4,250 Hz", onClick = {}, showChevron = false, enabled = false) }

        Spacer(Modifier.height(Spacing.xxl))
        SectionLabel("정보")
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "앱 버전", subtitle = "1.0.0", onClick = {}, showChevron = false, enabled = false) }
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "샘플레이트", subtitle = "44.1 kHz", onClick = {}, showChevron = false, enabled = false) }
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "출력", subtitle = "Mono", onClick = {}, showChevron = false, enabled = false) }
        Spacer(Modifier.height(Spacing.sm))
        Card { ChevronRow(title = "임상 면책 조항", subtitle = "준비 중", onClick = {}, enabled = false) }

        Spacer(Modifier.height(120.dp))
    }

    if (showDatePicker) {
        val initialMs = if (treatmentStartDate > 0L) treatmentStartDate
                        else System.currentTimeMillis()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onSetTreatmentStartDate(it) }
                    showDatePicker = false
                }) { Text("확인", color = Teal) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소", color = Muted) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Radius.card),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.5.dp, RoundedCornerShape(Radius.card))
            .border(1.dp, Line, RoundedCornerShape(Radius.card))
    ) { content() }
}

@Composable
private fun DailyGoalRow(currentMin: Int, onSetGoal: (Int) -> Unit) {
    val selectedIndex = GOAL_OPTIONS_MIN.indexOf(currentMin).let { if (it < 0) 0 else it }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
    ) {
        Text("일일 청취 목표", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(Spacing.sm))
        SegmentedControl(
            options = GOAL_LABELS,
            selectedIndex = selectedIndex,
            onSelect = { idx -> onSetGoal(GOAL_OPTIONS_MIN[idx]) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = Coral,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = Spacing.xs)
    )
}

private fun formatStartDate(epochMs: Long): String {
    if (epochMs <= 0L) return "미설정"
    val date = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN))
}
