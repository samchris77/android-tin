package com.tinnitustracker.ui.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinnitustracker.ui.theme.Heat0
import com.tinnitustracker.ui.theme.Heat1
import com.tinnitustracker.ui.theme.Heat2
import com.tinnitustracker.ui.theme.Heat3
import com.tinnitustracker.ui.theme.Heat4
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Surface as SurfaceColor
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.pressableClickable
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RecordsScreen(vm: RecordsViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            "기록",
            color = Ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(16.dp))
        CalendarCard(
            state = state,
            onPrev = vm::previousMonth,
            onNext = vm::nextMonth
        )
    }
}

@Composable
private fun CalendarCard(
    state: RecordsUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(16.dp)
    ) {
        CalendarHeader(state, onPrev, onNext)
        Spacer(Modifier.height(12.dp))
        WeekdayRow()
        Spacer(Modifier.height(6.dp))
        DayGrid(state.cells, state.maxDayMs)
        Spacer(Modifier.height(12.dp))
        LegendRow(state.monthTotalMs)
    }
}

@Composable
private fun CalendarHeader(
    state: RecordsUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "기록 달력",
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .pressableClickable(onClick = onPrev)
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "이전 달",
                tint = Ink2,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            state.monthYear.format(fmt),
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Box(
            modifier = Modifier
                .pressableClickable(onClick = onNext)
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "다음 달",
                tint = Ink2,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WeekdayRow() {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEach { label ->
            Text(
                label,
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayGrid(cells: List<DayCell>, maxDayMs: Long) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val cell = cells[row * 7 + col]
                    DayCellView(cell, maxDayMs, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, maxDayMs: Long, modifier: Modifier) {
    val bg = when {
        !cell.inCurrentMonth -> Heat0
        else -> heatColor(cell.totalMs, maxDayMs)
    }
    val textColor = if (cell.inCurrentMonth) {
        // High-intensity cells need light text for legibility.
        if (cell.totalMs > 0 && maxDayMs > 0 && cell.totalMs > maxDayMs * 0.5) Color.White
        else Ink
    } else {
        Muted
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(
                if (cell.isToday) Modifier.border(
                    width = 2.dp,
                    color = Teal,
                    shape = RoundedCornerShape(8.dp)
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            cell.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (cell.isToday) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun LegendRow(monthTotalMs: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("적음", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.width(6.dp))
        listOf(Heat1, Heat2, Heat3, Heat4).forEach { c ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(c)
            )
            Spacer(Modifier.width(3.dp))
        }
        Spacer(Modifier.width(3.dp))
        Text("많음", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.weight(1f))
        Text("이번 달: ", color = Muted, fontSize = 12.sp)
        Text(
            formatDuration(monthTotalMs),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun heatColor(ms: Long, maxMs: Long): Color {
    if (ms <= 0L || maxMs <= 0L) return Heat0
    val ratio = ms.toDouble() / maxMs.toDouble()
    return when {
        ratio <= 0.25 -> Heat1
        ratio <= 0.50 -> Heat2
        ratio <= 0.75 -> Heat3
        else          -> Heat4
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0m"
    val totalMin = ms / 60_000L
    val h = totalMin / 60L
    val m = totalMin % 60L
    return when {
        h > 0L -> "${h}h ${m}m"
        else   -> "${m}m"
    }
}
