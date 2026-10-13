package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassHighlight

/**
 * High-fidelity frosted glass surface.
 * Does NOT apply RenderEffect to the container to avoid blurring child text, icons, and buttons.
 * Uses translucent gradient layers, specular border highlights, and soft ambient elevation.
 */
@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = GlassBackground,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    blurRadius: Float = 0f, // Deprecated parameter kept for compatibility
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val baseModifier = modifier
        .then(
            if (elevation > 0.dp) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = Color(0x14000000),
                    spotColor = Color(0x1A000000)
                )
            } else Modifier
        )
        .clip(shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = Color.White),
                    onClick = onClick
                )
            } else Modifier
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundColor.copy(alpha = (backgroundColor.alpha * 1.15f).coerceAtMost(0.98f)),
                    backgroundColor
                )
            ),
            shape = shape
        )
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = listOf(
                    borderColor,
                    GlassHighlight,
                    borderColor.copy(alpha = borderColor.alpha * 0.5f)
                )
            ),
            shape = shape
        )

    Box(
        modifier = baseModifier,
        content = content
    )
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GlassBackground,
    borderColor: Color = GlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassBox(
        modifier = modifier,
        shape = CircleShape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        elevation = 2.dp,
        onClick = onClick,
        content = content
    )
}
