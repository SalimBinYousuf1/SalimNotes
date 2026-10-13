package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PillMicCircle
import com.example.ui.theme.PillRing

@Composable
fun BottomFloatingPill(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit
) {
    val addInteractionSource = remember { MutableInteractionSource() }
    val micInteractionSource = remember { MutableInteractionSource() }

    // Outer dark slate-purple translucent capsule
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color(0x33000000),
                spotColor = Color(0x4D000000)
            )
            .clip(CircleShape)
            .background(PillRing)
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // White circle with black "+"
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(
                        interactionSource = addInteractionSource,
                        indication = ripple(color = Color.Black),
                        onClick = onAddClick
                    )
                    .testTag("floating_add_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Note",
                    tint = Color(0xFF1F2430),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Smaller muted-mauve circle with white mic
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PillMicCircle)
                    .clickable(
                        interactionSource = micInteractionSource,
                        indication = ripple(color = Color.White),
                        onClick = onMicClick
                    )
                    .testTag("floating_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record Voice Note",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
