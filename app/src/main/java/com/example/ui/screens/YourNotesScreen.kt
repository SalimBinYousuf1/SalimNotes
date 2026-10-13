package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.audio.AudioPlayerHelper
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.BottomFloatingPill
import com.example.ui.components.ChecklistNoteCard
import com.example.ui.components.FilesSmartCard
import com.example.ui.components.GlassBox
import com.example.ui.components.GlassChipsRow
import com.example.ui.components.GlassCircleMenuButton
import com.example.ui.components.NoteFilter
import com.example.ui.components.PdfSmartCard
import com.example.ui.components.PhotoNoteCard
import com.example.ui.components.StarburstLogo
import com.example.ui.components.TextNoteCard
import com.example.ui.components.VoiceNoteCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BodyText
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.notesGradient

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun YourNotesScreen(
    notes: List<NoteEntity>,
    checklistsMap: Map<String, List<ChecklistItemEntity>>,
    attachmentsMap: Map<String, List<AttachmentEntity>>,
    onNoteClick: (NoteEntity) -> Unit,
    onCollectionClick: (collectionId: String, collectionName: String) -> Unit,
    onAddNoteClick: () -> Unit,
    onMicClick: () -> Unit,
    onToggleChecklistItem: (ChecklistItemEntity) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleImportant: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onArchiveNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onOpenMenu: () -> Unit,
    onLogoClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(NoteFilter.ALL) }
    var selectedNoteForAction by remember { mutableStateOf<NoteEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val audioPlayer = remember { AudioPlayerHelper() }
    var playingNoteId by remember { mutableStateOf<String?>(null) }

    val filesCount = remember(attachmentsMap) {
        attachmentsMap.values.flatten().count { it.type == "FILE" }
    }
    val pdfCount = remember(attachmentsMap) {
        attachmentsMap.values.flatten().count { it.type == "PDF" }
    }

    // Filter notes
    val filteredNotes = remember(notes, selectedFilter, checklistsMap, attachmentsMap) {
        when (selectedFilter) {
            NoteFilter.ALL -> notes
            NoteFilter.TODO -> notes.filter { (checklistsMap[it.id]?.isNotEmpty() == true) }
            NoteFilter.IMAGES -> notes.filter { attachmentsMap[it.id]?.any { a -> a.type == "IMAGE" } == true }
            NoteFilter.IMPORTANT -> notes.filter { it.isImportant }
            NoteFilter.AUDIO -> notes.filter { attachmentsMap[it.id]?.any { a -> a.type == "AUDIO" } == true }
            NoteFilter.FILES -> notes.filter { attachmentsMap[it.id]?.any { a -> a.type == "FILE" } == true }
            NoteFilter.PDF -> notes.filter { attachmentsMap[it.id]?.any { a -> a.type == "PDF" } == true }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(notesGradient())
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar: Starburst Logo at left, 4-dot menu button at right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarburstLogo(
                    size = 46.dp,
                    strokeWidthDp = 3.6.dp,
                    onClick = onLogoClick
                )

                GlassCircleMenuButton(
                    size = 48.dp,
                    onClick = onOpenMenu
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Large Title: "Your Notes"
            Text(
                text = stringResource(R.string.title_your_notes),
                fontFamily = OutfitFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .padding(horizontal = 22.dp)
                    .testTag("your_notes_title")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal scrolling chip row
            GlassChipsRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2-column staggered grid
            if (filteredNotes.isEmpty() && filesCount == 0 && pdfCount == 0) {
                // Designed empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassBox(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        backgroundColor = GlassBackground,
                        borderColor = GlassBorder
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StarburstLogo(size = 48.dp, strokeWidthDp = 3.2.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No notes yet",
                                fontFamily = OutfitFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the + button below to create your first note, or mic to record a thought.",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("notes_grid"),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalItemSpacing = 14.dp
                ) {
                    // Smart collection cards if relevant files exist
                    if ((selectedFilter == NoteFilter.ALL || selectedFilter == NoteFilter.FILES) && filesCount > 0) {
                        item {
                            FilesSmartCard(
                                count = filesCount,
                                onClick = { selectedFilter = NoteFilter.FILES }
                            )
                        }
                    }

                    if ((selectedFilter == NoteFilter.ALL || selectedFilter == NoteFilter.PDF) && pdfCount > 0) {
                        item {
                            PdfSmartCard(
                                count = pdfCount,
                                onClick = { selectedFilter = NoteFilter.PDF }
                            )
                        }
                    }

                    items(filteredNotes, key = { it.id }) { note ->
                        val checklists = checklistsMap[note.id] ?: emptyList()
                        val attachments = attachmentsMap[note.id] ?: emptyList()
                        val imageAttachments = attachments.filter { it.type == "IMAGE" }
                        val audioAttachment = attachments.firstOrNull { it.type == "AUDIO" }

                        Box(
                            modifier = Modifier.combinedClickable(
                                onClick = { onNoteClick(note) },
                                onLongClick = { selectedNoteForAction = note }
                            )
                        ) {
                            when {
                                checklists.isNotEmpty() -> {
                                    ChecklistNoteCard(
                                        note = note,
                                        checklistItems = checklists,
                                        onToggleItem = onToggleChecklistItem,
                                        onClick = { onNoteClick(note) }
                                    )
                                }
                                audioAttachment != null -> {
                                    val isPlaying = playingNoteId == note.id
                                    val waveform = remember(audioAttachment.waveform) {
                                        audioAttachment.waveform.split(",")
                                            .mapNotNull { it.trim().toIntOrNull() }
                                            .ifEmpty { List(28) { (15..80).random() } }
                                    }
                                    VoiceNoteCard(
                                        note = note,
                                        waveform = waveform,
                                        isPlaying = isPlaying,
                                        onPlayPause = {
                                            if (isPlaying) {
                                                audioPlayer.pause()
                                                playingNoteId = null
                                            } else {
                                                playingNoteId = note.id
                                                audioPlayer.play(audioAttachment.uri) {
                                                    playingNoteId = null
                                                }
                                            }
                                        },
                                        onClick = { onNoteClick(note) }
                                    )
                                }
                                imageAttachments.isNotEmpty() -> {
                                    PhotoNoteCard(
                                        note = note,
                                        coverAttachment = imageAttachments.firstOrNull(),
                                        photoCount = imageAttachments.size,
                                        isCollection = note.collectionId != null,
                                        onClick = { onNoteClick(note) }
                                    )
                                }
                                else -> {
                                    TextNoteCard(
                                        note = note,
                                        onClick = { onNoteClick(note) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom soft fade scrim so floating pill never obscures cards
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            com.example.ui.theme.NotesGradientBottom.copy(alpha = 0.85f),
                            com.example.ui.theme.NotesGradientBottom
                        )
                    )
                )
        )

        // Bottom floating pill: Dark slate-purple capsule with + and mic
        BottomFloatingPill(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            onAddClick = onAddNoteClick,
            onMicClick = onMicClick
        )

        // Glass Action Sheet on Long-Press
        selectedNoteForAction?.let { note ->
            ModalBottomSheet(
                onDismissRequest = { selectedNoteForAction = null },
                sheetState = sheetState,
                containerColor = Color(0xF0FFFFFF),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = note.title.ifBlank { "Note Options" },
                        fontFamily = OutfitFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ActionRow(
                        icon = Icons.Default.PushPin,
                        label = if (note.isPinned) "Unpin Note" else "Pin Note",
                        onClick = {
                            onTogglePin(note.id)
                            selectedNoteForAction = null
                        }
                    )
                    ActionRow(
                        icon = Icons.Default.Star,
                        label = if (note.isImportant) "Remove Important" else "Mark Important",
                        onClick = {
                            onToggleImportant(note.id)
                            selectedNoteForAction = null
                        }
                    )
                    ActionRow(
                        icon = Icons.Default.Lock,
                        label = if (note.isLocked) "Unlock Note" else "Lock with Biometric",
                        onClick = {
                            onToggleLock(note.id)
                            selectedNoteForAction = null
                        }
                    )
                    ActionRow(
                        icon = Icons.Default.Archive,
                        label = "Archive Note",
                        onClick = {
                            onArchiveNote(note.id)
                            selectedNoteForAction = null
                        }
                    )
                    ActionRow(
                        icon = Icons.Default.Delete,
                        label = "Move to Trash",
                        color = Color(0xFFD32F2F),
                        onClick = {
                            onDeleteNote(note.id)
                            selectedNoteForAction = null
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = BodyText,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
