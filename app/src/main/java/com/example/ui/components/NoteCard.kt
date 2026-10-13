package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.NoteEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BodyText
import com.example.ui.theme.CoralCardAccent
import com.example.ui.theme.CormorantGaramondFontFamily
import com.example.ui.theme.CreamCard
import com.example.ui.theme.CreamCardText
import com.example.ui.theme.DotGothicFontFamily
import com.example.ui.theme.LavenderCard
import com.example.ui.theme.MintCardEnd
import com.example.ui.theme.MintCardStart
import com.example.ui.theme.OrchidCard
import com.example.ui.theme.OrchidCardText
import com.example.ui.theme.OutfitFontFamily
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChecklistNoteCard(
    note: NoteEntity,
    checklistItems: List<ChecklistItemEntity>,
    onToggleItem: (ChecklistItemEntity) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1F000000))
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MintCardStart, MintCardEnd)
                )
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("checklist_card_${note.id}")
    ) {
        Column {
            // Dotted monospace title
            Text(
                text = note.title.ifBlank { "Checklist" },
                fontFamily = DotGothicFontFamily,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B382F),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Show up to 3 items
            val displayed = checklistItems.take(3)
            displayed.forEach { item ->
                ChecklistPillRow(
                    item = item,
                    onToggle = { onToggleItem(item) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ChecklistPillRow(
    item: ChecklistItemEntity,
    onToggle: () -> Unit
) {
    val pillBg by animateColorAsState(
        targetValue = if (item.isChecked) Color(0x66184A3B) else Color(0x2EFFFFFF),
        label = "pill_bg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(pillBg)
            .then(
                if (!item.isChecked) Modifier.border(1.dp, Color(0x40FFFFFF), CircleShape)
                else Modifier
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Check icon circle
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (item.isChecked) Color.White else Color.Transparent)
                    .then(
                        if (!item.isChecked) Modifier.border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Checked",
                        tint = Color(0xFF184A3B),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.text,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isChecked) Color.White else Color(0xFF183D31),
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TextNoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(note.updatedAt) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(note.updatedAt))
    }

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1F000000))
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xE6FFFFFF)) // frosted white
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("text_note_card_${note.id}")
    ) {
        Column {
            Text(
                text = note.title.ifBlank { "Untitled" },
                fontFamily = OutfitFontFamily,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (note.plainText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.plainText,
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = BodyText.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = formattedDate,
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF8C98A8),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun VoiceNoteCard(
    note: NoteEntity,
    waveform: List<Int>,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1F000000))
            .clip(RoundedCornerShape(28.dp))
            .background(LavenderCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("voice_note_card_${note.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // White play circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = LavenderCard,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Rounded-bar waveform
            WaveformBars(
                amplitudes = waveform,
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
            )
        }
    }
}

@Composable
fun WaveformBars(
    amplitudes: List<Int>,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White
) {
    val bars = remember(amplitudes) {
        if (amplitudes.isEmpty()) List(24) { (15..80).random() }
        else amplitudes.take(28)
    }

    Canvas(modifier = modifier) {
        val count = bars.size
        if (count == 0) return@Canvas
        val totalWidth = size.width
        val barWidth = (totalWidth / (count * 1.6f)).coerceIn(2.5f, 5.5f)
        val spacing = (totalWidth - (barWidth * count)) / (count - 1).coerceAtLeast(1)

        bars.forEachIndexed { index, amp ->
            val fraction = (amp / 100f).coerceIn(0.12f, 1.0f)
            val barHeight = size.height * fraction
            val left = index * (barWidth + spacing)
            val top = (size.height - barHeight) / 2f

            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun PhotoNoteCard(
    note: NoteEntity,
    coverAttachment: AttachmentEntity?,
    photoCount: Int,
    isCollection: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(note.updatedAt) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(note.updatedAt))
    }

    Box(modifier = modifier) {
        // Peeking coral card behind collection
        if (isCollection) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 6.dp, y = 4.dp)
                    .rotate(5f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(CoralCardAccent)
            )
        }

        // Front Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .shadow(6.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x22000000))
                .clip(RoundedCornerShape(26.dp))
                .clickable(onClick = onClick)
                .testTag("photo_card_${note.id}")
        ) {
            // Background photo or subtle dark gradient
            if (coverAttachment != null) {
                AsyncImage(
                    model = File(coverAttachment.uri),
                    contentDescription = note.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C3240), Color(0xFF161922))
                            )
                        )
                )
            }

            // Dark vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            // Top title & date
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = note.title.ifBlank { "Photos" },
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = formattedDate,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Bottom glass chips
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (photoCount > 1) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.28f))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+$photoCount",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.28f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Photos",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilesSmartCard(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1F000000))
            .clip(RoundedCornerShape(28.dp))
            .background(CreamCard)
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("files_smart_card")
    ) {
        Column {
            // Dual page glyphs at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Files",
                fontFamily = CormorantGaramondFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = CreamCardText
            )
        }
    }
}

@Composable
fun PdfSmartCard(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1F000000))
            .clip(RoundedCornerShape(28.dp))
            .background(OrchidCard)
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("pdf_smart_card")
    ) {
        Column {
            // Dual page glyphs at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Pdf",
                fontFamily = CormorantGaramondFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = OrchidCardText
            )
        }
    }
}
