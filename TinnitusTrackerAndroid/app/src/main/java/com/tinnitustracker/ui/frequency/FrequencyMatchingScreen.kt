package com.tinnitustracker.ui.frequency

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FrequencyMatchingScreen(viewModel: FrequencyViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Frequency Matcher",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Frequency", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${viewModel.frequency.toInt()} Hz",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Volume", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${(viewModel.volume * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Interactive Pad
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.updateFromUserDrag(
                                offset.x / size.width.toFloat(),
                                offset.y / size.height.toFloat()
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            viewModel.updateFromUserDrag(
                                change.position.x / size.width.toFloat(),
                                change.position.y / size.height.toFloat()
                            )
                        }
                    )
                }
        ) {
            // Grid lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // Vertical lines (Frequency)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(width * 0.25f, 0f),
                    end = Offset(width * 0.25f, height)
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(width * 0.5f, 0f),
                    end = Offset(width * 0.5f, height)
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(width * 0.75f, 0f),
                    end = Offset(width * 0.75f, height)
                )
                
                // Horizontal lines (Volume)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, height * 0.25f),
                    end = Offset(width, height * 0.25f)
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, height * 0.5f),
                    end = Offset(width, height * 0.5f)
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, height * 0.75f),
                    end = Offset(width, height * 0.75f)
                )

                // Control Point
                val pointX = viewModel.normalizedX * width
                val pointY = viewModel.normalizedY * height
                
                drawCircle(
                    color = Color.White,
                    radius = 30f,
                    center = Offset(pointX, pointY)
                )
                drawCircle(
                    color = Color(0xFFEF6C00), // Orange accent
                    radius = 24f,
                    center = Offset(pointX, pointY)
                )
            }
            
            Text(
                "Drag to adjust",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Play Button
        Button(
            onClick = { viewModel.togglePlayPause() },
            modifier = Modifier
                .size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (viewModel.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (viewModel.isPlaying) "Stop" else "Play",
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (viewModel.isPlaying) "Playing White Noise" else "Tap to Play",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
