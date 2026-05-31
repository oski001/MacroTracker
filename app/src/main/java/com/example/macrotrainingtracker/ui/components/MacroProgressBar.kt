package com.example.macrotrainingtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macrotrainingtracker.ui.theme.AccentPrimary
import com.example.macrotrainingtracker.ui.theme.SurfaceBorder

@Composable
fun MacroProgressBar(
    label: String,
    current: Int,
    target: Int
) {
    val progress = if (target <= 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$current",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "/ $target g",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Clean linear progress bar - Lyfta style
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            val strokeWidth = size.height
            val trackWidth = size.width

            // Background track
            drawRoundRect(
                color = SurfaceBorder,
                topLeft = Offset.Zero,
                size = Size(trackWidth, strokeWidth),
                cornerRadius = CornerRadius(strokeWidth / 2, strokeWidth / 2)
            )

            // Progress fill
            if (progress > 0f) {
                drawRoundRect(
                    color = AccentPrimary,
                    topLeft = Offset.Zero,
                    size = Size(trackWidth * progress, strokeWidth),
                    cornerRadius = CornerRadius(strokeWidth / 2, strokeWidth / 2)
                )
            }
        }
    }
}

