package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.CollectionEntity
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.GlassBox
import com.example.ui.components.TextNoteCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BodyText
import com.example.ui.theme.ChipSelectedFill
import com.example.ui.theme.ChipSelectedText
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PillRing
import com.example.ui.theme.detailGradient
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class CollectionFilterType { ALL, TEXT, IMAGES, AUDIOS }

@Composable
fun CollectionDetailScreen(
    collection: CollectionEntity,
    notes: List<NoteEntity>,
    attachmentsMap: Map<String, List<AttachmentEntity>>,
    onBackClick: () -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onEditNoteClick: (NoteEntity) -> Unit,
    onCreateNoteInCollection: () -> Unit,
    onDeleteCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf(CollectionFilterType.ALL) }
    var activeIndex by remember { mutableIntStateOf(0) }
    var isListView by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, filterType, attachmentsMap) {
        when (filterType) {
            CollectionFilterType.ALL -> notes
            CollectionFilterType.TEXT -> notes.filter { (attachmentsMap[it.id] ?: emptyList()).isEmpty() }
            CollectionFilterType.IMAGES -> notes.filter { attachmentsMap[it.id]?.any { a -> a.type == "IMAGE" } == true }
            CollectionFilterType.AUDIOS -> notes.filter { attachmentsMap[it.id]?.any { a -> a.type == "AUDIO" } == true }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(detailGradient())
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar: Back button and Menu button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBox(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp)
                    )
                }

                Box {
                    GlassBox(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isListView) "Stack View" else "List View") },
                            onClick = {
                                isListView = !isListView
                                menuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isListView) Icons.Default.ViewCarousel else Icons.Default.ViewAgenda,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Collection", color = Color(0xFFD32F2F)) },
                            onClick = {
                                menuExpanded = false
                                onDeleteCollection()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Huge Centered Collection Name over two lines
            Text(
                text = collection.name.replace(" ", "\n"),
                fontFamily = OutfitFontFamily,
                fontSize = 44.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Centered glass chips: Text, Images, Audios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CollectionChip(
                    label = "Text",
                    isSelected = filterType == CollectionFilterType.TEXT,
                    onClick = { filterType = if (filterType == CollectionFilterType.TEXT) CollectionFilterType.ALL else CollectionFilterType.TEXT }
                )
                Spacer(modifier = Modifier.width(10.dp))
                CollectionChip(
                    label = "Images",
                    isSelected = filterType == CollectionFilterType.IMAGES,
                    onClick = { filterType = if (filterType == CollectionFilterType.IMAGES) CollectionFilterType.ALL else CollectionFilterType.IMAGES }
                )
                Spacer(modifier = Modifier.width(10.dp))
                CollectionChip(
                    label = "Audios",
                    isSelected = filterType == CollectionFilterType.AUDIOS,
                    onClick = { filterType = if (filterType == CollectionFilterType.AUDIOS) CollectionFilterType.ALL else CollectionFilterType.AUDIOS }
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Perspective stacked card area or List view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (filteredNotes.isEmpty()) {
                    GlassBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        shape = RoundedCornerShape(32.dp),
                        backgroundColor = GlassBackground,
                        borderColor = GlassBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No notes in this collection",
                                fontFamily = OutfitFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap '+ Create new' below to add a thought.",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isListView) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredNotes, key = { it.id }) { note ->
                            TextNoteCard(
                                note = note,
                                onClick = { onNoteClick(note) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                } else {
                    // Perspective 3D Stack
                    val safeIndex = activeIndex.coerceIn(0, (filteredNotes.size - 1).coerceAtLeast(0))

                    // Peek Card 2 (backmost)
                    if (safeIndex + 2 < filteredNotes.size) {
                        val backNote2 = filteredNotes[safeIndex + 2]
                        PeekCard(
                            note = backNote2,
                            offsetY = (-28).dp,
                            scale = 0.88f,
                            alpha = 0.65f
                        )
                    }

                    // Peek Card 1 (middle)
                    if (safeIndex + 1 < filteredNotes.size) {
                        val backNote1 = filteredNotes[safeIndex + 1]
                        PeekCard(
                            note = backNote1,
                            offsetY = (-14).dp,
                            scale = 0.94f,
                            alpha = 0.85f
                        )
                    }

                    // Active Front Card
                    if (safeIndex < filteredNotes.size) {
                        val activeNote = filteredNotes[safeIndex]
                        var dragOffset by remember { mutableFloatStateOf(0f) }
                        val animatedDragOffset by animateFloatAsState(
                            targetValue = dragOffset,
                            animationSpec = spring(),
                            label = "card_drag"
                        )

                        ActiveStackedCard(
                            note = activeNote,
                            attachments = attachmentsMap[activeNote.id] ?: emptyList(),
                            modifier = Modifier
                                .offset { IntOffset(animatedDragOffset.roundToInt(), 0) }
                                .pointerInput(safeIndex) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            if (dragOffset > 140f && safeIndex > 0) {
                                                activeIndex--
                                            } else if (dragOffset < -140f && safeIndex < filteredNotes.size - 1) {
                                                activeIndex++
                                            }
                                            dragOffset = 0f
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            dragOffset += dragAmount
                                        }
                                    )
                                },
                            onClick = { onNoteClick(activeNote) },
                            onEditClick = { onEditNoteClick(activeNote) }
                        )
                    }
                }

                // Bottom "+ Create new" pill slightly overlapping the bottom card edge
                CreateNewPill(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 8.dp),
                    onClick = onCreateNoteInCollection
                )
            }
        }
    }
}

@Composable
private fun PeekCard(
    note: NoteEntity,
    offsetY: androidx.compose.ui.unit.Dp,
    scale: Float,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY)
            .scale(scale)
            .height(220.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = alpha))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Text(
            text = note.title.ifBlank { "Untitled Note" },
            fontFamily = OutfitFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = AccentBlue.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ActiveStackedCard(
    note: NoteEntity,
    attachments: List<AttachmentEntity>,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(note.updatedAt) {
        SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(note.updatedAt))
    }
    val imageAttachments = remember(attachments) { attachments.filter { it.type == "IMAGE" } }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(24.dp)
            .testTag("collection_active_card")
    ) {
        Column {
            // Top Row: Small title, timestamp in tiny gray, pencil edit button at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = note.title.ifBlank { "Morning Reflection" },
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BodyText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF8C98A8)
                    )
                }

                // Pencil edit button in glass circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F4F9))
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = AccentBlue,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Two-line bold blue heading
            Text(
                text = note.title.ifBlank { "Embrace the moment and let your spirit soar." },
                fontFamily = OutfitFontFamily,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Three-line dark body preview
            Text(
                text = note.plainText.ifBlank { "Every day is a fresh start—embrace it with hope and courage. Let each moment inspire you to grow and shine brighter than before." },
                fontFamily = OutfitFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = BodyText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Row of rounded image thumbnails
            if (imageAttachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    imageAttachments.take(2).forEach { att ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            AsyncImage(
                                model = File(att.uri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun CollectionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Color.White else Color(0x4DFFFFFF))
            .border(1.dp, if (isSelected) Color.White else Color(0x99FFFFFF), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF1F2430) else Color(0xFF223046)
        )
    }
}

@Composable
private fun CreateNewPill(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(12.dp, CircleShape, ambientColor = Color(0x33000000))
            .clip(CircleShape)
            .background(Color.White)
            .border(3.5.dp, PillRing, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("collection_create_new_button")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFF1F2430),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Create new",
                fontFamily = OutfitFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2430)
            )
        }
    }
}
