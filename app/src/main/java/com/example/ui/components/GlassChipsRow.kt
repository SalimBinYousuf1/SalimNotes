package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChipSelectedFill
import com.example.ui.theme.ChipSelectedText
import com.example.ui.theme.OutfitFontFamily

enum class NoteFilter(val label: String) {
    ALL("All Notes"),
    TODO("To-Do"),
    IMAGES("Images"),
    IMPORTANT("Important"),
    AUDIO("Audio"),
    FILES("Files"),
    PDF("PDF")
}

@Composable
fun GlassChipsRow(
    selectedFilter: NoteFilter,
    onFilterSelected: (NoteFilter) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp)
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NoteFilter.values().forEach { filter ->
            val isSelected = filter == selectedFilter
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .then(
                        if (isSelected) {
                            Modifier
                                .shadow(4.dp, CircleShape, ambientColor = Color(0x22000000))
                                .clip(CircleShape)
                                .background(ChipSelectedFill)
                        } else {
                            Modifier
                                .clip(CircleShape)
                                .background(Color(0x52FFFFFF))
                                .border(1.dp, Color(0x99FFFFFF), CircleShape)
                        }
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = if (isSelected) Color.Black else Color.White),
                        onClick = { onFilterSelected(filter) }
                    )
                    .padding(horizontal = 18.dp, vertical = 9.dp)
                    .testTag("filter_chip_${filter.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) ChipSelectedText else Color(0xFF243247)
                )
            }
        }
    }
}
