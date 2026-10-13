package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StarburstLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    rayColor: Color = Color.White,
    strokeWidthDp: Dp = 3.6.dp,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false, radius = size / 2),
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val strokeWidthPx = strokeWidthDp.toPx()
            val rInner = this.size.width * 0.22f
            val rOuter = this.size.width * 0.46f

            for (i in 0 until 12) {
                val angleRad = Math.toRadians((i * 30.0) - 90.0)
                val start = Offset(
                    x = center.x + (rInner * cos(angleRad)).toFloat(),
                    y = center.y + (rInner * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = center.x + (rOuter * cos(angleRad)).toFloat(),
                    y = center.y + (rOuter * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = rayColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun GlassCircleMenuButton(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    onClick: () -> Unit
) {
    GlassBox(
        modifier = modifier.size(size),
        shape = CircleShape,
        elevation = 2.dp,
        onClick = onClick
    ) {
        // 4 dots in a 2x2 grid
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                )
            }
        }
    }
}
