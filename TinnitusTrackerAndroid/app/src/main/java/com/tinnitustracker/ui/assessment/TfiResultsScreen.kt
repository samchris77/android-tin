package com.tinnitustracker.ui.assessment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinnitustracker.ui.theme.Bg
import com.tinnitustracker.ui.theme.Coral
import com.tinnitustracker.ui.theme.Ink
import com.tinnitustracker.ui.theme.Ink2
import com.tinnitustracker.ui.theme.Line
import com.tinnitustracker.ui.theme.Muted
import com.tinnitustracker.ui.theme.Radius
import com.tinnitustracker.ui.theme.Spacing
import com.tinnitustracker.ui.theme.Teal
import com.tinnitustracker.ui.theme.TealSoft
import com.tinnitustracker.ui.theme.pressableClickable

@Composable
fun TfiResultsScreen(
    vm: TfiQuestionnaireViewModel,
    onDone: () -> Unit
) {
    val saved by vm.lastSubmitted
    val total = saved?.totalScore ?: 0
    val subscaleScores = saved?.subscaleScores ?: emptyMap()
    val severity = TfiScoring.severityKorean(total)

    BackHandler { onDone() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "결과",
            color = Coral,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "TFI 점수",
            color = Ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(20.dp))

        TotalCard(total = total, severity = severity)
        Spacer(Modifier.height(16.dp))

        Text(
            "세부 점수",
            color = Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        SubscaleBars(scores = subscaleScores)

        Spacer(Modifier.height(Spacing.section))
        Text(
            "TFI 점수는 0–100 범위로, 점수가 낮을수록 이명의 영향이 적음을 의미합니다.",
            color = Muted,
            fontSize = 11.5.sp,
            lineHeight = 17.sp
        )

        Spacer(Modifier.height(Spacing.section))
        DoneButton(onClick = onDone)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun TotalCard(total: Int, severity: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Line, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "$total",
                    color = Ink,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                )
                Text(
                    " / 100",
                    color = Muted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = Spacing.sm, start = Spacing.xs)
                )
            }
            Spacer(Modifier.height(8.dp))
            Surface(
                color = TealSoft,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    severity,
                    color = Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun SubscaleBars(scores: Map<String, Int>) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Line, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            TfiScoring.Subscales.keys.forEachIndexed { index, code ->
                if (index > 0) Spacer(Modifier.height(12.dp))
                SubscaleRow(
                    label = TfiScoring.SubscaleKoreanLabels.getValue(code),
                    score = scores[code] ?: 0
                )
            }
        }
    }
}

@Composable
private fun SubscaleRow(label: String, score: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("$score", color = Ink2, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(Spacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.sm)
                .clip(RoundedCornerShape(3.dp))
                .background(Line)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score.coerceIn(0, 100) / 100f)
                    .height(Spacing.sm)
                    .background(Teal, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun DoneButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressableClickable(onClick = onClick)
            .shadow(3.dp, RoundedCornerShape(Radius.card), spotColor = Teal.copy(alpha = 0.45f))
            .clip(RoundedCornerShape(Radius.card))
            .background(Teal)
            .padding(vertical = Spacing.md)
            .semantics { contentDescription = "완료" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "완료",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

