package com.tinnitustracker.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.Elevation
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.PlayButtonVariant
import com.tinnitustracker.ui.theme.PolishedPlayButton
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.Teal2
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.components.AppCard
import com.tinnitustracker.ui.theme.components.AppCardVariant
import com.tinnitustracker.ui.theme.components.ChevronRow
import com.tinnitustracker.ui.theme.pressableClickable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 홈 — daily landing screen. Direction A (player-first hero) + Direction D's
 * horizontal week strip, per `docs/design/app-wireframes.html`. Mock content
 * is hardcoded for visual fidelity; functional wiring is intentionally minimal
 * (play button hands off to the parent, which switches to the 소리 tab).
 */
@Composable
fun HomeScreen(
    onStartTherapy: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartTfi: () -> Unit,
    showTfiPrompt: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(Spacing.lg))
        TopBar(onOpenSettings = onOpenSettings)

        Spacer(Modifier.height(Spacing.lg))
        HeroPlayerCard(onPlay = onStartTherapy)

        Spacer(Modifier.height(Spacing.xl))
        WeekStrip()

        Spacer(Modifier.height(Spacing.lg))
        SleepTimerRow()

        if (showTfiPrompt) {
            Spacer(Modifier.height(Spacing.md))
            TfiPromptCard(onStart = onStartTfi)
        }

        Spacer(Modifier.height(96.dp))
    }
}

// ── Top bar ────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onOpenSettings: () -> Unit) {
    val todayKo = remember {
        // M월 d일 (E) — e.g. "5월 11일 (월)". java.time on minSdk 28 needs no desugar.
        LocalDate.now().format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("안녕하세요", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            // TODO: derive "치료 N주차" from a real treatment_start_date once that DataStore key exists.
            Text("치료 3주차 · $todayKo", color = Muted, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .pressableClickable(onClick = onOpenSettings)
                .size(36.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Line, CircleShape)
                .semantics { contentDescription = "설정 열기" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = null,
                tint = Ink2,
                modifier = Modifier.size(Spacing.xl)
            )
        }
    }
}

// ── Hero player card ───────────────────────────────────────────────────────

@Composable
private fun HeroPlayerCard(onPlay: () -> Unit) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        variant = AppCardVariant.DarkHero,
        radius = Radius.card,
        padding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle coral radial wash in the top-right corner — mirrors the
            // wireframes' `radial-gradient(ellipse at top right, rgba(214,124,92,0.20)…)`.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Coral.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(Float.POSITIVE_INFINITY, 0f),
                            radius = 600f
                        )
                    )
            )

            // Atmospheric sine wave painted across the lower-middle.
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = Spacing.xl)
                    .align(Alignment.Center)
            ) {
                val path = Path()
                val w = size.width
                val h = size.height
                val midY = h / 2f
                val amplitude = h * 0.35f
                val steps = 6
                path.moveTo(0f, midY)
                for (i in 1..steps) {
                    val x = w * i / steps.toFloat()
                    val y = if (i % 2 == 0) midY + amplitude else midY - amplitude
                    val cx = w * (i - 0.5f) / steps.toFloat()
                    path.quadraticBezierTo(cx, y, x, midY)
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.28f),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Top-left text stack.
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Text(
                    "BROADBAND · NOTCHED 4.2 kHz",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(Spacing.md))
                Text(
                    "저녁\n휴식",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.4).sp
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    "노치 · 핑크 노이즈 · 빗소리",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.5.sp
                )
            }

            // Bottom-left adherence overlay.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = Spacing.xl, bottom = Spacing.xxl)
            ) {
                Text(
                    "오늘 청취",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "84",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        " / 120 min",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // Bottom-right play button — polished hero CTA (gradient face,
            // top gloss, inner rim, coral spot shadow, press-scale + tucked shadow).
            PolishedPlayButton(
                onClick = onPlay,
                contentDescription = "재생",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Spacing.xl, bottom = Spacing.xl),
                size = 76.dp,
                iconSize = 34.dp
            )
        }
    }
}

// ── Week strip ─────────────────────────────────────────────────────────────

@Composable
private fun WeekStrip() {
    // Direction D, hardcoded mock state (week ending on today=Fri).
    // (day-label, kind)
    val days = listOf(
        "월" to DayCell.Hit,
        "화" to DayCell.Hit,
        "수" to DayCell.Partial(62),
        "목" to DayCell.Hit,
        "금" to DayCell.Today(minutes = 84, goal = 120),
        "토" to DayCell.Future,
        "일" to DayCell.Future,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        days.forEach { (label, cell) ->
            val weight = if (cell is DayCell.Today) 1.6f else 1f
            Column(
                modifier = Modifier.weight(weight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DayCellBox(cell)
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    label,
                    color = if (cell is DayCell.Today) Coral else Muted,
                    fontSize = 10.sp,
                    fontWeight = if (cell is DayCell.Today) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }

    Spacer(Modifier.height(Spacing.md))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text("일평균 98분 · 지난 주 대비 ", color = Muted, fontSize = 10.5.sp)
            Text("+12분", color = Teal, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        }
        Text("🔥 7일 연속", color = Muted, fontSize = 10.5.sp)
    }
}

private sealed interface DayCell {
    object Hit : DayCell
    data class Partial(val minutes: Int) : DayCell
    data class Today(val minutes: Int, val goal: Int) : DayCell
    object Future : DayCell
}

@Composable
private fun DayCellBox(cell: DayCell) {
    when (cell) {
        is DayCell.Hit -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(Radius.chip))
                .background(Teal),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                "✓",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
        is DayCell.Partial -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(Radius.chip))
                .background(TealSoft),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                cell.minutes.toString(),
                color = Muted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
        is DayCell.Today -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(Radius.chip))
                .background(
                    brush = Brush.linearGradient(listOf(Teal2, Teal))
                )
                .shadow(elevation = Elevation.raised, shape = RoundedCornerShape(Radius.chip), clip = false)
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm)
        ) {
            Column {
                Text(
                    "TODAY",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 8.5.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        cell.minutes.toString(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        "/${cell.goal}",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "▮▮▮▮▯▯",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            }
        }
        DayCell.Future -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(Radius.chip))
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, Line, RoundedCornerShape(Radius.chip))
        )
    }
}

// ── Sleep timer chip ───────────────────────────────────────────────────────

@Composable
private fun SleepTimerRow() {
    ChevronRow(
        title = "수면 타이머",
        leading = { Text("⏱", fontSize = 14.sp) },
        trailing = { Text("30분", color = Muted, fontSize = 11.sp) },
        modifier = Modifier.semantics { contentDescription = "수면 타이머" },
        onClick = {
            // TODO: open sleep-timer sheet (15 / 30 / 60 / 90 / 끄지않음)
        }
    )
}

// ── TFI prompt card ────────────────────────────────────────────────────────

@Composable
private fun TfiPromptCard(onStart: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AppCardVariant.CoralSoft,
        radius = Radius.card,
        padding = PaddingValues(start = Spacing.md, end = Spacing.md, top = Spacing.md, bottom = Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "이번 주 설문조사",
                    color = Coral,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text("약 5분 소요 · TFI", color = Ink, fontSize = 12.5.sp)
            }
            PolishedPlayButton(
                onClick = onStart,
                contentDescription = "설문조사 시작",
                variant = PlayButtonVariant.Teal,
                size = 44.dp,
                iconSize = Spacing.xl
            )
        }
    }
}

