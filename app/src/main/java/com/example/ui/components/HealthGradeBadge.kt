package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GradeAColor
import com.example.ui.theme.GradeBColor
import com.example.ui.theme.GradeCColor
import com.example.ui.theme.GradeCriticalColor

@Composable
fun HealthGradeBadge(
    grade: String,
    score: Int,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    showScore: Boolean = true
) {
    val (backgroundColor, textColor, gradeLabel) = when (grade.uppercase()) {
        "A" -> Triple(GradeAColor, Color.White, "A")
        "B" -> Triple(GradeBColor, Color.White, "B")
        "C" -> Triple(GradeCColor, Color.White, "C")
        else -> Triple(GradeCriticalColor, Color.White, "!")
    }

    if (isCompact) {
        Box(
            modifier = modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gradeLabel,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor.copy(alpha = 0.15f))
                .border(1.dp, backgroundColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gradeLabel,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (showScore) {
                Text(
                    text = "$score/100",
                    color = backgroundColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
