package com.tinnitustracker.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinnitustracker.R
import com.tinnitustracker.data.repository.AudioRepository
import com.tinnitustracker.data.repository.ListeningSessionRepository
import com.tinnitustracker.data.repository.SoundPresetRepository
import com.tinnitustracker.data.repository.UserSettingsRepository
import com.tinnitustracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onStartTherapy: () -> Unit,
    onStartTfi: () -> Unit,
    showTfiPrompt: Boolean,
    onOpenQuickLog: () -> Unit,
    onOpenPresetPicker: () -> Unit
) {
    val state by vm.state.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    var showSleepTimer by remember { mutableStateOf(false) }
    val sleepTimer by vm.sleepTimer.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(40.dp))

        // --- Header ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("안녕하세요", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(4.dp))
            Text("치료 ${state.treatmentWeek}주차", fontSize = 13.sp, color = Ink2)
        }

        Spacer(Modifier.height(24.dp))

        // --- Hero Player ---
        Surface(
            color = TealSoft,
            shape = RoundedCornerShape(Radius.card),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(Elevation.card, RoundedCornerShape(Radius.card))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp)
            ) {
                // Play Ring
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = if (state.dailyGoalMin > 0) {
                        (state.todayListenMs / 60000f) / state.dailyGoalMin
                    } else 0f
                    
                    ProgressRing(progress = progress.coerceIn(0f, 1f))
                    
                    Surface(
                        color = if (isPlaying) Teal2 else Teal,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(Elevation.raised, CircleShape)
                            .pressableClickable { vm.togglePlay() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "정지" else "재생",
                            tint = Color.White,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text("현재 프리셋", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Teal, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .pressableClickable(onClick = onOpenPresetPicker)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        state.activePreset?.name ?: "저녁 휴식",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        letterSpacing = (-0.5).sp
                    )
                    Text(" ▾", fontSize = 18.sp, color = Ink2)
                }
                Spacer(Modifier.height(4.dp))
                val presetInfo = buildList {
                    when (state.activePreset?.processingMode) {
                        "notch" -> add("노치")
                        "amplify" -> add("증폭")
                        "off" -> add("주파수 끔")
                    }
                    state.activePreset?.colorNoise?.let { if (it != "off") add(it.replaceFirstChar { c -> c.uppercase() }) }
                    if (state.activePreset?.ambientMix?.isNotEmpty() == true) add("자연음")
                }.joinToString(" · ")
                Text(if (presetInfo.isBlank()) "기본 모드" else presetInfo, fontSize = 13.sp, color = Ink2)
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Adherence Card ---
        Surface(
            color = Surface,
            shape = RoundedCornerShape(Radius.card),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Line, RoundedCornerShape(Radius.card))
                .shadow(Elevation.card, RoundedCornerShape(Radius.card))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("오늘의 청취 달성률", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Muted)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${state.todayListenMs / 60000}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(" / ${state.dailyGoalMin} 분", fontSize = 13.sp, color = Muted, modifier = Modifier.padding(bottom = 2.dp))
                    }
                }

                // Mini horizontal bar
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(6.dp)
                        .background(Bg, RoundedCornerShape(3.dp))
                ) {
                    val progress = if (state.dailyGoalMin > 0) {
                        (state.todayListenMs / 60000f) / state.dailyGoalMin
                    } else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Teal, RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Action Grid ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Sleep Timer
            Surface(
                color = Surface,
                shape = RoundedCornerShape(Radius.card),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Line, RoundedCornerShape(Radius.card))
                    .shadow(Elevation.card, RoundedCornerShape(Radius.card))
                    .pressableClickable { showSleepTimer = true }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Bg, RoundedCornerShape(Radius.chip))
                            .border(1.dp, Line, RoundedCornerShape(Radius.chip)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Bedtime, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("수면 타이머", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    val timer = sleepTimer
                    Text(
                        if (timer != null) {
                            val remainingMin = (timer.remainingSec + 59) / 60
                            "${timer.setMinutes}분 설정 · ${remainingMin}분 남음"
                        } else "꺼짐",
                        fontSize = 12.sp,
                        color = if (timer != null) Teal else Muted,
                        fontWeight = if (timer != null) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // Quick Log
            Surface(
                color = CoralSoft,
                shape = RoundedCornerShape(Radius.card),
                modifier = Modifier
                    .weight(1f)
                    .pressableClickable(onClick = onOpenQuickLog)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Surface, RoundedCornerShape(Radius.chip))
                            .shadow(2.dp, RoundedCornerShape(Radius.chip), spotColor = Coral.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Coral, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("상태 기록", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    Text("지금 이명은?", fontSize = 12.sp, color = Coral)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Optional TFI Prompt
        if (showTfiPrompt) {
            Surface(
                color = Surface,
                shape = RoundedCornerShape(Radius.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Line, RoundedCornerShape(Radius.card))
                    .shadow(Elevation.card, RoundedCornerShape(Radius.card))
                    .pressableClickable(onClick = onStartTfi)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TealSoft, RoundedCornerShape(Radius.chip)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("설", color = Teal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("정기 TFI 설문", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("이명 상태 변화를 측정할 시간입니다.", fontSize = 12.sp, color = Muted)
                    }
                    Text("›", fontSize = 18.sp, color = Muted)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(96.dp))
    }

    if (showSleepTimer) {
        ModalBottomSheet(
            onDismissRequest = { showSleepTimer = false },
            containerColor = Surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("수면 타이머 설정", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(15, 30, 60, 90).forEach { minutes ->
                        Button(
                            onClick = { 
                                vm.setSleepTimer(minutes)
                                showSleepTimer = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Bg, contentColor = Ink),
                            shape = RoundedCornerShape(Radius.button)
                        ) {
                            Text("${minutes}분")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { 
                        vm.cancelSleepTimer()
                        showSleepTimer = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape = RoundedCornerShape(Radius.button)
                ) {
                    Text("타이머 끄기", color = Color.White)
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProgressRing(progress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 6.dp.toPx()
        // Background ring
        drawCircle(
            color = Surface,
            radius = (size.minDimension - strokeWidth) / 2,
            style = Stroke(width = strokeWidth)
        )
        // Foreground ring
        drawArc(
            color = Teal,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(size.width - strokeWidth, size.height - strokeWidth)
        )
    }
}
