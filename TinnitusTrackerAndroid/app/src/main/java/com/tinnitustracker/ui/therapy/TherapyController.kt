package com.tinnitustracker.ui.therapy

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow

@Composable
fun TherapyController(
    viewModel: TherapyViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spectrum Bar
            SpectrumBar(
                volume = viewModel.volume,
                notchFrequency = viewModel.matchedFrequency,
                isNotchEnabled = viewModel.isNotchEnabled
            )

            // Playback Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notch Toggle (Mini)
                IconButton(onClick = { viewModel.toggleNotch() }) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Toggle Notch",
                        tint = if (viewModel.isNotchEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Play/Pause (Central, High Emphasis)
                FilledIconButton(
                    onClick = { viewModel.togglePlayback() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (viewModel.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Placeholder for symmetry or other action (maybe Timer)
                 Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun SpectrumBar(
    volume: Float,
    notchFrequency: Float,
    isNotchEnabled: Boolean
) {
    val barCount = 20
    val minFreq = 250f
    val maxFreq = 8000f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            // Calculate frequency for this bar (Log scale)
            // freq = min * (max/min)^(i / (N-1))
            val fraction = i.toFloat() / (barCount - 1)
            val barFreq = minFreq * (maxFreq / minFreq).pow(fraction)

            // Determine if inside notch (0.5 octave bandwidth)
            // 0.5 octave around center means range [center / 2^0.25, center * 2^0.25]
            val octaveRange = 0.25f
            val lowerBound = notchFrequency / 2.0f.pow(octaveRange)
            val upperBound = notchFrequency * 2.0f.pow(octaveRange)

            val isInNotch = isNotchEnabled && (barFreq in lowerBound..upperBound)

            // Dynamic Height
            // Base height is volume %, but if in notch, it drops significantly (approx 10%)
            val targetHeight = if (isInNotch) {
                0.1f // Notched state
            } else {
                volume.coerceAtLeast(0.1f) // Ensure at least small visibility
            }

            val animatedHeight by animateDpAsState(
                targetValue = 80.dp * targetHeight,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f), label = "barHeight"
            )
            
            val barColor = if (isInNotch) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(animatedHeight)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}
