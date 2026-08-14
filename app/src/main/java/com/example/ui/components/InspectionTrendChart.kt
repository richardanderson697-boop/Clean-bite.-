package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InspectionReport
import com.example.ui.theme.GradeAColor
import com.example.ui.theme.GradeBColor
import com.example.ui.theme.GradeCColor

@Composable
fun InspectionTrendChart(
    reports: List<InspectionReport>,
    modifier: Modifier = Modifier
) {
    if (reports.isEmpty()) return

    val sortedReports = reports.sortedBy { it.date }
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Text(
            text = "Health Score History & Trend",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Official Municipal Inspection Reports",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val padding = 32f

                val maxScore = 100f
                val minScore = 60f
                val range = maxScore - minScore

                // Grade Threshold reference lines
                val yA = height - ((90f - minScore) / range * height)
                val yB = height - ((80f - minScore) / range * height)

                // Draw Threshold dashed lines
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Grade A threshold line
                drawLine(
                    color = GradeAColor.copy(alpha = 0.3f),
                    start = Offset(0f, yA),
                    end = Offset(width, yA),
                    strokeWidth = 2f,
                    pathEffect = dashPathEffect
                )
                // Grade B threshold line
                drawLine(
                    color = GradeBColor.copy(alpha = 0.3f),
                    start = Offset(0f, yB),
                    end = Offset(width, yB),
                    strokeWidth = 2f,
                    pathEffect = dashPathEffect
                )

                if (sortedReports.size < 2) {
                    // Single point fallback
                    val rep = sortedReports[0]
                    val x = width / 2f
                    val y = height - ((rep.score.toFloat() - minScore) / range * height)
                    val pointColor = when {
                        rep.score >= 90 -> GradeAColor
                        rep.score >= 80 -> GradeBColor
                        else -> GradeCColor
                    }
                    drawCircle(color = pointColor, radius = 12f, center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 6f, center = Offset(x, y))
                    return@Canvas
                }

                // Plot points
                val points = sortedReports.mapIndexed { index, report ->
                    val x = padding + index * ((width - 2 * padding) / (sortedReports.size - 1))
                    val scoreClamped = report.score.coerceIn(60, 100).toFloat()
                    val y = height - ((scoreClamped - minScore) / range * height)
                    Offset(x, y)
                }

                // Draw connecting path
                val path = Path()
                points.forEachIndexed { i, pt ->
                    if (i == 0) path.moveTo(pt.x, pt.y)
                    else {
                        val prev = points[i - 1]
                        val midX = (prev.x + pt.x) / 2
                        path.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                    }
                }

                val lineTrendColor = if (sortedReports.last().score >= sortedReports.first().score) GradeAColor else GradeBColor

                drawPath(
                    path = path,
                    color = lineTrendColor,
                    style = Stroke(width = 6f)
                )

                // Draw Point Circles
                points.forEachIndexed { index, pt ->
                    val rep = sortedReports[index]
                    val ptColor = when {
                        rep.score >= 90 -> GradeAColor
                        rep.score >= 80 -> GradeBColor
                        else -> GradeCColor
                    }

                    drawCircle(color = ptColor, radius = 14f, center = pt)
                    drawCircle(color = Color.White, radius = 6f, center = pt)
                }
            }
        }
    }
}
