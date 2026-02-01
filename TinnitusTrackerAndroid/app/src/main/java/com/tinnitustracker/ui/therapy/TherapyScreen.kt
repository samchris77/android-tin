package com.tinnitustracker.ui.therapy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinnitustracker.R
import com.tinnitustracker.audio.AudioEngine

import androidx.compose.material3.ExperimentalMaterial3Api

data class SoundEffect(
    val name: String,
    val resourceId: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapyScreen(
    audioEngine: AudioEngine,
    viewModel: TherapyViewModel = viewModel(factory = TherapyViewModelFactory(audioEngine))
) {
    val sounds = listOf(
        SoundEffect("Rain", R.raw.rain, Color(0xFF4FC3F7)),
        SoundEffect("Fire", R.raw.fireplace, Color(0xFFFF7043)),
        SoundEffect("Beach", R.raw.beach, Color(0xFFFFF176)),
        SoundEffect("Brook", R.raw.brook, Color(0xFF66BB6A))
    )
    
    // UI just observes ViewModel state
    val selectedResourceId = viewModel.selectedResourceId
    var isNotchEnabled by remember { mutableStateOf(false) } // AudioEngine tracks this, but UI needs state
    var showTimerDialog by remember { mutableStateOf(false) }
    
    // Sync notch state
    fun toggleNotch(enabled: Boolean) {
        isNotchEnabled = enabled
        audioEngine.setNotchEnabled(enabled)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showTimerDialog = true },
                icon = { Icon(Icons.Default.AccessTime, "Timer") },
                text = { 
                    if (viewModel.isTimerRunning) {
                        Text(viewModel.getFormattedTime())
                    } else {
                        Text("Sleep Timer")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Sound Therapy",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notch Toggle
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isNotchEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { toggleNotch(!isNotchEnabled) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isNotchEnabled, onCheckedChange = { toggleNotch(it) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Notch Filtering", style = MaterialTheme.typography.titleMedium)
                        Text("Masks your matched frequency", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sound Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sounds) { sound ->
                    val isSelected = selectedResourceId == sound.resourceId
                    SoundCard(
                        sound = sound,
                        isSelected = isSelected,
                        onClick = {
                            viewModel.toggleSound(sound.resourceId)
                        }
                    )
                }
            }
        }
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Set Sleep Timer") },
            text = {
                Column {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        TextButton(
                            onClick = { 
                                viewModel.startTimer(mins)
                                showTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$mins minutes")
                        }
                    }
                    if (viewModel.isTimerRunning) {
                         TextButton(
                            onClick = { 
                                viewModel.stopTimer()
                                showTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel Timer")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SoundCard(
    sound: SoundEffect,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) sound.color.copy(alpha=0.8f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Placeholder Icon
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = "Playing")
                }
            }
        }
    }
}
