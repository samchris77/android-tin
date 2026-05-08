package com.tinnitustracker.ui.matcher

import android.graphics.Paint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// ─── Hz ↔ Angle (log scale, 0° = 100 Hz at 12-o'clock, 360° = 16 kHz) ────
// 100 Hz is the origin so the dial starts at the top with the minimum usable frequency.

private const val DIAL_MIN_HZ = 100f
private const val DIAL_MAX_HZ = 16000f
private val DIAL_LOG_RANGE = log10(DIAL_MAX_HZ / DIAL_MIN_HZ)   // ≈ 2.204

private fun hzToAngle(hz: Float): Float =
    log10(hz.coerceIn(DIAL_MIN_HZ, DIAL_MAX_HZ) / DIAL_MIN_HZ) / DIAL_LOG_RANGE * 360f

private fun angleToHz(deg: Float): Float =
    (DIAL_MIN_HZ * 10f.pow(deg.coerceIn(0f, 360f) / 360f * DIAL_LOG_RANGE))
        .coerceIn(DIAL_MIN_HZ, DIAL_MAX_HZ)

// ─── Top-level screen ──────────────────────────────────────────────────────

@Composable
fun FrequencyMatchingScreen(viewModel: FrequencyMatchingViewModel) {
    val hz      by viewModel.frequency.collectAsStateWithLifecycle()
    val volume  by viewModel.volume.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        WaveRingDecoration(modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader()
            HeroSection()
            Spacer(Modifier.height(12.dp))
            CreamKnobPanel(
                hz = hz,
                onHzChange = { viewModel.setSliderFrequency(viewModel.frequencyToSlider(it)) },
                onStep = viewModel::stepFrequency
            )
            Spacer(Modifier.height(12.dp))
            CoreNoiseCard()
            Spacer(Modifier.height(12.dp))
            SleepFavoritesCard(
                volume = volume,
                isPlaying = isPlaying,
                onVolumeChange = viewModel::setVolume,
                onPlayToggle = viewModel::togglePlay
            )
            Spacer(Modifier.height(8.dp))
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
        Text("이명", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
        // 3 wave lines approximated as arcs drawn on native canvas
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

// ─── Hero section ─────────────────────────────────────────────────────────

@Composable
private fun HeroSection() {
    var planFree by remember { mutableStateOf(true) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text("사운드 찾기", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(6.dp))
        Text("일치할 때까지 주파수와 볼륨을 조정하세요",
            color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        FreePremiumPill(isFree = planFree, onToggle = { planFree = it })
    }
}

@Composable
private fun FreePremiumPill(isFree: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0xFF141416), RoundedCornerShape(999.dp))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(999.dp))
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PillOption(label = "기본", sub = "Free", selected = isFree,
            onClick = { onToggle(true) })
        Spacer(Modifier.width(2.dp))
        PillOption(label = "귀별", sub = "Premium", selected = !isFree,
            accent = true, onClick = { onToggle(false) })
    }
}

@Composable
private fun PillOption(
    label: String, sub: String, selected: Boolean,
    accent: Boolean = false, onClick: () -> Unit
) {
    val bg = if (selected) Color.White.copy(alpha = 0.07f) else Color.Transparent
    val labelColor = if (selected) TextPrimary else TextSecondary
    val subColor = if (accent) OrangeAccent else TextTertiary
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = labelColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("($sub)", color = subColor, fontSize = 12.sp)
        }
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
        text = "주파수 매칭",
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

private fun Float.normDeg(): Float = ((this % 360f) + 360f) % 360f

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

                        // Clamp to [0°, 360°] — prevents wrap-around past min/max
                        val clampedAngle = (hzToAngle(hzState.floatValue) + delta)
                            .coerceIn(0f, 360f)
                        val newHz = angleToHz(clampedAngle)
                        onHzChange(newHz)

                        // Haptic tick every 6° (one per minor tick mark)
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
    val angle = hzToAngle(hz)

    val tickRingR  = outerR * 0.97f
    val labelR     = outerR * 0.84f
    val arcRadius  = outerR * 0.72f
    val arcStroke  = 9.dp.toPx()
    val faceR      = outerR * 0.62f

    // Layer 1: subtle inner face — paper-like, very gentle warmth
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color(0xFFF7F1E2),
                1f to Color(0xFFE8E1D0)
            ),
            center = Offset(cx, cy - faceR * 0.2f),
            radius = faceR * 1.4f
        ),
        radius = faceR,
        center = Offset(cx, cy)
    )
    // soft inner shadow at the bottom of the face for subtle depth
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.85f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.06f)
            ),
            center = Offset(cx, cy + faceR * 0.4f),
            radius = faceR
        ),
        radius = faceR,
        center = Offset(cx, cy)
    )

    // Layer 2: outer fine tick ring (60 ticks, 12 major)
    val numTicks = 60
    for (i in 0 until numTicks) {
        val isMajor = i % 5 == 0
        val angleDeg = i.toFloat() / numTicks * 360f
        val rad = Math.toRadians((angleDeg - 90.0))
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        val tickLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
        val outerEdge = tickRingR
        val innerEdge = tickRingR - tickLen
        drawLine(
            color = Color(0xFF6E6657).copy(alpha = if (isMajor) 0.55f else 0.18f),
            start = Offset(cx + cosA * innerEdge, cy + sinA * innerEdge),
            end   = Offset(cx + cosA * outerEdge, cy + sinA * outerEdge),
            strokeWidth = if (isMajor) 1.4.dp.toPx() else 1.dp.toPx()
        )
    }

    // Layer 3: background arc track (full circle, soft cream)
    drawArc(
        color = Color(0xFFCFC6B3),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(cx - arcRadius, cy - arcRadius),
        size = Size(arcRadius * 2f, arcRadius * 2f),
        style = Stroke(width = arcStroke, cap = StrokeCap.Round)
    )

    // Layer 4: progress arc (orange, sweeping from top clockwise)
    if (angle > 0.5f) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(OrangeAccent, OrangeAccent2, OrangeAccent),
                center = Offset(cx, cy)
            ),
            startAngle = -90f,
            sweepAngle = angle,
            useCenter = false,
            topLeft = Offset(cx - arcRadius, cy - arcRadius),
            size = Size(arcRadius * 2f, arcRadius * 2f),
            style = Stroke(width = arcStroke, cap = StrokeCap.Round)
        )
    }

    // Layer 5: log-scale labels at decade positions (20, 100, 1k, 10k)
    // Labels at log-scale positions; 100Hz is at 0° (top), 16kHz at 360°
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
        val a = hzToAngle(freq)
        val rad = Math.toRadians((a - 90.0))
        val lx = cx + cos(rad).toFloat() * labelR
        val ly = cy + sin(rad).toFloat() * labelR + textPaint.textSize / 3f
        drawIntoCanvas { it.nativeCanvas.drawText(lbl, lx, ly, textPaint) }
    }

    // Layer 6: indicator at arc tip
    val indRad = Math.toRadians(angle.toDouble() - 90.0)
    val indX = cx + cos(indRad).toFloat() * arcRadius
    val indY = cy + sin(indRad).toFloat() * arcRadius

    // glow halo
    drawCircle(OrangeAccent.copy(alpha = 0.35f), 14.dp.toPx(), Offset(indX, indY))
    // cream surround
    drawCircle(Color(0xFFF5EFE2), 9.dp.toPx(), Offset(indX, indY))
    // orange core
    drawCircle(OrangeAccent, 6.dp.toPx(), Offset(indX, indY))
    // tiny highlight
    drawCircle(
        Color.White.copy(alpha = 0.7f),
        1.8.dp.toPx(),
        Offset(indX - 1.6.dp.toPx(), indY - 1.6.dp.toPx())
    )
}

// ─── Stepper buttons ──────────────────────────────────────────────────────

@Composable
private fun StepperButtonRow(onStep: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(-10, -1, 1, 10).forEach { delta ->
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
        Text(
            label,
            color = InkCream,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp
        )
    }
}

// ─── Pitch adjust row ─────────────────────────────────────────────────────

@Composable
private fun PitchAdjustRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.08f),
                shape = RoundedCornerShape(0.dp)
            )
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

// ─── Core noise card ──────────────────────────────────────────────────────

private data class NoiseTile(val labelKr: String, val gradientColors: List<Color>)

private val noiseTiles = listOf(
    NoiseTile("브라운", listOf(Color(0xFF8A4A26), Color(0xFF3D1F14))),
    NoiseTile("그린",   listOf(Color(0xFF2A5E3A), Color(0xFF0A2D18))),
    NoiseTile("핑크",   listOf(Color(0xFF8B4A6B), Color(0xFF3D0820))),
    NoiseTile("화이트", listOf(Color(0xFF787888), Color(0xFF2A2A35))),
)

@Composable
private fun CoreNoiseCard() {
    DarkCard {
        Text("핵심 노이즈", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("전 스펙트럼 마스킹. 여기서 시작하세요",
            color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            noiseTiles.forEach { tile ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.radialGradient(
                                colors = tile.gradientColors + listOf(Color.Transparent),
                                radius = 200f
                            ))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(tile.labelKr, color = TextSecondary, fontSize = 11.sp,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ─── Sleep favorites card ─────────────────────────────────────────────────

@Composable
private fun SleepFavoritesCard(
    volume: Float,
    isPlaying: Boolean,
    onVolumeChange: (Float) -> Unit,
    onPlayToggle: () -> Unit
) {
    DarkCard {
        Text("수면 즐겨찾기", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("더 빨리 잠들고, 더 깊이 잠들기",
            color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(12.dp))
        Text("볼륨", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = OrangeAccent,
                    inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                )
            )
            Spacer(Modifier.width(8.dp))
            Text("${(volume * 100).roundToInt()}%", color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.height(8.dp))
        PlayerRow(isPlaying = isPlaying, onPlayToggle = onPlayToggle)
    }
}

@Composable
private fun PlayerRow(isPlaying: Boolean, onPlayToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedBarsIcon(isPlaying)
            Column {
                Text("현재 사운드", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(if (isPlaying) "재생 중" else "정지됨",
                    color = TextSecondary, fontSize = 11.sp)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SmallIconButton(icon = Icons.Filled.OpenInFull)
            SmallIconButton(icon = Icons.Filled.Timer)
            SmallIconButton(icon = Icons.Filled.FavoriteBorder)
            // Orange play/stop circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(OrangeAccent, CircleShape)
                    .clickable(onClick = onPlayToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(Color.White.copy(alpha = 0.06f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun AnimatedBarsIcon(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bars")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.size(24.dp, 20.dp)) {
        val barW = 3.dp.toPx()
        val gap = 4.dp.toPx()
        val maxH = size.height
        val baseHeights = listOf(0.45f, 1.0f, 0.65f, 0.9f, 0.5f)
        val offsets = listOf(0f, 0.25f, 0.5f, 0.75f, 0.1f)
        baseHeights.forEachIndexed { i, base ->
            val animH = if (isPlaying) {
                val t = ((phase + offsets[i]) % 1f)
                val wave = sin(t * 2 * PI.toFloat())
                (base + wave * 0.25f).coerceIn(0.2f, 1.0f) * maxH
            } else {
                base * 0.5f * maxH
            }
            val x = i * (barW + gap)
            drawRoundRect(
                color = OrangeAccent,
                topLeft = Offset(x, maxH - animH),
                size = Size(barW, animH),
                cornerRadius = CornerRadius(1.5.dp.toPx())
            )
        }
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
