package com.example.macrotrainingtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.example.macrotrainingtracker.ui.theme.SurfaceBorder

@Composable
fun CalorieRing(remaining: Int, consumed: Int, totalBudget: Int) {
    val spent = consumed.coerceAtLeast(0)
    val budget = totalBudget.coerceAtLeast(1)
    val progress = (spent.toFloat() / budget).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Minimalist progress ring - Lyfta style
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            // Background track - thin, muted
            drawArc(
                color = SurfaceBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc - vibrant accent
            if (progress > 0f) {
                drawArc(
                    color = AccentPrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center content - bold numbers, muted labels
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$remaining",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                text = "KCAL LEFT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    }
}

