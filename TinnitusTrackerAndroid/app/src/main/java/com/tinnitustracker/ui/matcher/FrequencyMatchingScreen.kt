package com.tinnitustracker.ui.matcher

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinnitustracker.audio.engine.AudioEngine
import com.tinnitustracker.ui.theme.CreamPanel
import com.tinnitustracker.ui.theme.CreamPanel2
import com.tinnitustracker.ui.theme.DarkBg
import com.tinnitustracker.ui.theme.DarkCard
import com.tinnitustracker.ui.theme.InkCream
import com.tinnitustracker.ui.theme.OrangeAccent
import com.tinnitustracker.ui.theme.OrangeAccent2
import com.tinnitustracker.ui.theme.TextPrimary
import com.tinnitustracker.ui.theme.TextSecondary
import com.tinnitustracker.ui.theme.TextTertiary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

// ─── Hz ↔ Angle (log scale, 100 Hz = 0° / 12 o'clock, 16 kHz = 360°) ────

private const val DIAL_MIN_HZ = 100f
private const val DIAL_MAX_HZ = 16000f
private val DIAL_LOG_RANGE = log10(DIAL_MAX_HZ / DIAL_MIN_HZ)

private fun hzToAngle(hz: Float): Float =
    log10(hz.coerceIn(DIAL_MIN_HZ, DIAL_MAX_HZ) / DIAL_MIN_HZ) / DIAL_LOG_RANGE * 360f

private fun angleToHz(deg: Float): Float =
    (DIAL_MIN_HZ * 10f.pow(deg.coerceIn(0f, 360f) / 360f * DIAL_LOG_RANGE))
        .coerceIn(DIAL_MIN_HZ, DIAL_MAX_HZ)

private fun Float.normDeg(): Float = ((this % 360f) + 360f) % 360f

// ─── Top-level screen ──────────────────────────────────────────────────────

@Composable
fun FrequencyMatchingScreen(viewModel: FrequencyMatchingViewModel) {
    val hz        by viewModel.frequency.collectAsStateWithLifecycle()
    val volume    by viewModel.volume.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val showOctaveCheck by viewModel.showOctaveCheck.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    LaunchedEffect(showOctaveCheck) {
        if (showOctaveCheck) scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        WaveRingDecoration(modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader()
            InstructionBanner()
            Spacer(Modifier.height(8.dp))
            CreamKnobPanel(
                hz = hz,
                onHzChange = {
                    viewModel.setSliderFrequency(viewModel.frequencyToSlider(it))
                    viewModel.onDialInteraction()
                },
                onStep = {
                    viewModel.stepFrequency(it)
                    viewModel.onDialInteraction()
                }
            )
            Spacer(Modifier.height(12.dp))
            VolumeSafetyCard(
                volume = volume,
                isPlaying = isPlaying,
                onVolumeChange = viewModel::setVolumeSafe,
                onPlayToggle = viewModel::togglePlay
            )
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(
                visible = showOctaveCheck,
                enter = slideInVertically { it } + fadeIn(),
                exit  = slideOutVertically { it } + fadeOut()
            ) {
                OctaveConfusionCard(viewModel)
            }
            SkipAffordance(viewModel::skipTonalPitch)
        }
    }
}

// ─── Wave ring decoration ──────────────────────────────────────────────────

@Composable
private fun WaveRingDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(240.dp)) {
        val cx = size.width / 2f
        for (i in 1..6) {
            val rx = (80 + i * 55).dp.toPx()
            val ry = (32 + i * 24).dp.toPx()
            val alpha = (0.07f - i * 0.008f).coerceAtLeast(0.008f)
            drawOval(
                color = Color.White.copy(alpha = alpha),
                topLeft = Offset(cx - rx, -ry * 0.3f),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

// ─── App header ───────────────────────────────────────────────────────────

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandWavesIcon()
        Spacer(Modifier.width(10.dp))
        Text("주파수 매칭", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.07f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.07f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun BrandWavesIcon() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        val paint = android.graphics.Paint().apply {
            color = OrangeAccent.toArgb()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.2.dp.toPx()
            strokeCap = android.graphics.Paint.Cap.ROUND
            isAntiAlias = true
        }
        for (idx in 0..2) {
            val yBase = h * (0.33f + idx * 0.22f)
            val path = android.graphics.Path()
            path.moveTo(0f, yBase)
            path.cubicTo(w * 0.25f, yBase - h * 0.14f, w * 0.5f, yBase + h * 0.14f, w * 0.75f, yBase - h * 0.08f)
            path.cubicTo(w * 0.88f, yBase - h * 0.14f, w * 0.94f, yBase - h * 0.06f, w, yBase)
            drawIntoCanvas { it.nativeCanvas.drawPath(path, paint) }
        }
    }
}

// ─── Instruction banner ───────────────────────────────────────────────────

@Composable
private fun InstructionBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Filled.VolumeDown,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(15.dp).padding(top = 1.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "낮은 볼륨에서 시작하고, 이명 소리와 일치할 때까지 다이얼을 돌리세요.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

// ─── Cream knob panel ─────────────────────────────────────────────────────

@Composable
private fun CreamKnobPanel(
    hz: Float,
    onHzChange: (Float) -> Unit,
    onStep: (Int) -> Unit
) {
    var pitchEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFFF1EBDC), Color(0xFFDFD6C5))),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(top = 18.dp, bottom = 20.dp, start = 18.dp, end = 18.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DialEyebrow()
            Spacer(Modifier.height(10.dp))
            FrequencyKnob(hz = hz, onHzChange = onHzChange)
            Spacer(Modifier.height(20.dp))
            StepperButtonRow(onStep)
            Spacer(Modifier.height(14.dp))
            PitchAdjustRow(enabled = pitchEnabled, onToggle = { pitchEnabled = it })
        }
    }
}

@Composable
private fun DialEyebrow() {
    Text(
        text = "주파수 선택",
        color = InkCream.copy(alpha = 0.55f),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp
    )
}

private fun pitchBandKr(hz: Float): String = when {
    hz < 250f   -> "저음"
    hz < 1000f  -> "중저음"
    hz < 2500f  -> "중음"
    hz < 6000f  -> "고음"
    else        -> "초고음"
}

// ─── Frequency knob ───────────────────────────────────────────────────────

@Composable
private fun FrequencyKnob(hz: Float, onHzChange: (Float) -> Unit) {
    val lastAngle  = remember { mutableFloatStateOf(hzToAngle(hz)) }
    val hzState    = remember { mutableFloatStateOf(hz) }
    hzState.floatValue = hz

    val haptic      = LocalHapticFeedback.current
    val lastTickIdx = remember { androidx.compose.runtime.mutableIntStateOf(-1) }

    Box(
        modifier = Modifier
            .size(280.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        lastAngle.floatValue = Math.toDegrees(
                            atan2((offset.y - cy).toDouble(), (offset.x - cx).toDouble())
                        ).toFloat().normDeg()
                    },
                    onDrag = { change, _ ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val newA = Math.toDegrees(
                            atan2(
                                (change.position.y - cy).toDouble(),
                                (change.position.x - cx).toDouble()
                            )
                        ).toFloat().normDeg()
                        var delta = newA - lastAngle.floatValue
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        val clampedAngle = (hzToAngle(hzState.floatValue) + delta).coerceIn(0f, 360f)
                        val newHz = angleToHz(clampedAngle)
                        onHzChange(newHz)

                        val tickIdx = (clampedAngle / 6f).toInt()
                        if (tickIdx != lastTickIdx.intValue) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastTickIdx.intValue = tickIdx
                        }
                        lastAngle.floatValue = newA
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDial(hz)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(hz.roundToInt()),
                    color = Color(0xFF2A2620),
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp,
                    lineHeight = 60.sp
                )
                Text(
                    text = " Hz",
                    color = InkCream.copy(alpha = 0.55f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = pitchBandKr(hz),
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
        }
    }
}

private fun DrawScope.drawDial(hz: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerR = size.width / 2f
    val angle  = hzToAngle(hz)

    val tickRingR = outerR * 0.97f
    val labelR    = outerR * 0.84f
    val arcRadius = outerR * 0.72f
    val arcStroke = 9.dp.toPx()
    val faceR     = outerR * 0.62f

    // Inner paper face
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to Color(0xFFF7F1E2), 1f to Color(0xFFE8E1D0)),
            center = Offset(cx, cy - faceR * 0.2f),
            radius = faceR * 1.4f
        ),
        radius = faceR,
        center = Offset(cx, cy)
    )

    // 60 outer tick marks
    for (i in 0 until 60) {
        val isMajor = i % 5 == 0
        val rad = Math.toRadians((i * 6.0 - 90.0))
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        val tickLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
        drawLine(
            color = Color(0xFF6E6657).copy(alpha = if (isMajor) 0.55f else 0.18f),
            start = Offset(cx + cosA * (tickRingR - tickLen), cy + sinA * (tickRingR - tickLen)),
            end   = Offset(cx + cosA * tickRingR, cy + sinA * tickRingR),
            strokeWidth = if (isMajor) 1.4.dp.toPx() else 1.dp.toPx()
        )
    }

    // Background arc track
    drawArc(
        color = Color(0xFFCFC6B3),
        startAngle = -90f, sweepAngle = 360f, useCenter = false,
        topLeft = Offset(cx - arcRadius, cy - arcRadius),
        size = Size(arcRadius * 2f, arcRadius * 2f),
        style = Stroke(width = arcStroke, cap = StrokeCap.Round)
    )

    // Progress arc (orange)
    if (angle > 0.5f) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(OrangeAccent, OrangeAccent2, OrangeAccent),
                center = Offset(cx, cy)
            ),
            startAngle = -90f, sweepAngle = angle, useCenter = false,
            topLeft = Offset(cx - arcRadius, cy - arcRadius),
            size = Size(arcRadius * 2f, arcRadius * 2f),
            style = Stroke(width = arcStroke, cap = StrokeCap.Round)
        )
    }

    // Log-scale labels: 100Hz=0°, 500Hz, 2kHz, 8kHz
    val labels = listOf(100f to "100", 500f to "500", 2000f to "2k", 8000f to "8k")
    val textPaint = Paint().apply {
        color = android.graphics.Color.argb(160, 80, 70, 55)
        textSize = 10.sp.toPx()
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        letterSpacing = 0.05f
    }
    labels.forEach { (freq, lbl) ->
        val rad = Math.toRadians((hzToAngle(freq) - 90.0))
        val lx = cx + cos(rad).toFloat() * labelR
        val ly = cy + sin(rad).toFloat() * labelR + textPaint.textSize / 3f
        drawIntoCanvas { it.nativeCanvas.drawText(lbl, lx, ly, textPaint) }
    }

    // Indicator pill at arc tip
    val indRad = Math.toRadians(angle.toDouble() - 90.0)
    val indX = cx + cos(indRad).toFloat() * arcRadius
    val indY = cy + sin(indRad).toFloat() * arcRadius
    drawCircle(OrangeAccent.copy(alpha = 0.35f), 14.dp.toPx(), Offset(indX, indY))
    drawCircle(Color(0xFFF5EFE2), 9.dp.toPx(), Offset(indX, indY))
    drawCircle(OrangeAccent, 6.dp.toPx(), Offset(indX, indY))
    drawCircle(Color.White.copy(alpha = 0.7f), 1.8.dp.toPx(),
        Offset(indX - 1.6.dp.toPx(), indY - 1.6.dp.toPx()))
}

// ─── Stepper buttons ──────────────────────────────────────────────────────

@Composable
private fun StepperButtonRow(onStep: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(-100, -10, 10, 100).forEach { delta ->
            StepperButton(
                label = if (delta > 0) "+$delta" else "$delta",
                onClick = { onStep(delta) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(listOf(Color(0xFFFAF4E5), Color(0xFFE6DDC9))),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.7f), Color(0xFFB6AC97).copy(alpha = 0.45f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = InkCream, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp)
    }
}

// ─── Pitch adjust toggle ──────────────────────────────────────────────────

@Composable
private fun PitchAdjustRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("음조 조정 비활성화", color = InkCream, fontSize = 14.sp)
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrangeAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFA59F93)
            )
        )
    }
}

// ─── Volume safety card ───────────────────────────────────────────────────

@Composable
private fun VolumeSafetyCard(
    volume: Float,
    isPlaying: Boolean,
    onVolumeChange: (Float) -> Unit,
    onPlayToggle: () -> Unit
) {
    DarkCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("볼륨", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                "${(volume * 100).roundToInt()}%  (최대 70%)",
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..0.70f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = OrangeAccent,
                inactiveTrackColor = Color.White.copy(alpha = 0.18f)
            )
        )
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(OrangeAccent, CircleShape)
                    .clickable(onClick = onPlayToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "정지" else "재생",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ─── Octave confusion card ────────────────────────────────────────────────

@Composable
private fun OctaveConfusionCard(viewModel: FrequencyMatchingViewModel) {
    var selectedOctave by remember { mutableStateOf(0) }

    DarkCard {
        Text(
            "이 중 어느 음이 더 가깝나요?",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "이명 소리는 흔히 실제보다 한 옥타브 높거나 낮게 들릴 수 있습니다.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OctaveButton("낮은 음", Modifier.weight(1f), selected = selectedOctave == -1) {
                selectedOctave = -1; viewModel.playOctave(-1)
            }
            OctaveButton("현재 음", Modifier.weight(1f), selected = selectedOctave == 0) {
                selectedOctave = 0; viewModel.playOctave(0)
            }
            OctaveButton("높은 음", Modifier.weight(1f), selected = selectedOctave == 1) {
                selectedOctave = 1; viewModel.playOctave(1)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrangeAccent, RoundedCornerShape(12.dp))
                .clickable { viewModel.confirmPitch() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "이 주파수로 저장하기",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun OctaveButton(
    label: String,
    modifier: Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (selected) OrangeAccent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (selected) OrangeAccent.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) OrangeAccent else TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Skip affordance ──────────────────────────────────────────────────────

@Composable
private fun SkipAffordance(onSkip: () -> Unit) {
    TextButton(
        onClick = onSkip,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            "뚜렷한 음을 찾기 어렵나요?",
            color = TextTertiary,
            fontSize = 12.sp,
            textDecoration = TextDecoration.Underline
        )
    }
}

// ─── Dark card container ──────────────────────────────────────────────────

@Composable
private fun DarkCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .background(DarkCard, RoundedCornerShape(22.dp))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column { content() }
    }
}
