package com.tinnitustracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Teal

@Composable
fun SettingsScreen(onReplayOnboarding: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Settings",
            color = Ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(24.dp))

        ActionRow(label = "튜토리얼 다시 보기", onClick = onReplayOnboarding)

        Spacer(Modifier.height(16.dp))

        SettingsRow(label = "Version", value = "1.0")
        SettingsRow(label = "Sample rate", value = "44.1 kHz")
        SettingsRow(label = "Output", value = "Mono")
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Ink2, fontSize = 13.sp)
            Text(value, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = label }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Ink2, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("열기", color = Teal, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
