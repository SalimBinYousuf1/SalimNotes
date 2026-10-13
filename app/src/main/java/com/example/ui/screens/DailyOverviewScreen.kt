package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.ReminderEntity
import com.example.ui.components.GlassBox
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BodyText
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.homeGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyOverviewScreen(
    reminders: List<ReminderEntity>,
    dueChecklists: List<ChecklistItemEntity>,
    notesUpdatedToday: List<NoteEntity>,
    onToggleChecklistItem: (ChecklistItemEntity) -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateString = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(homeGradient())
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Daily Overview",
                        fontFamily = OutfitFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = dateString,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat overview cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBadge(
                    label = "Reminders",
                    count = reminders.size,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Due Tasks",
                    count = dueChecklists.size,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Edited Today",
                    count = notesUpdatedToday.size,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (reminders.isNotEmpty()) {
                    item {
                        SectionHeader("Reminders Today")
                    }
                    items(reminders, key = { it.id }) { reminder ->
                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(reminder.triggerAt))
                        GlassBox(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0xE6FFFFFF)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Scheduled at $timeStr",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BodyText
                                )
                            }
                        }
                    }
                }

                if (dueChecklists.isNotEmpty()) {
                    item {
                        SectionHeader("Due Checklist Items")
                    }
                    items(dueChecklists, key = { it.id }) { item ->
                        GlassBox(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0xE6FFFFFF)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleChecklistItem(item) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.isChecked) Icons.Default.CheckCircle else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (item.isChecked) Color(0xFF2E7D32) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.text,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BodyText
                                )
                            }
                        }
                    }
                }

                if (notesUpdatedToday.isNotEmpty()) {
                    item {
                        SectionHeader("Notes Edited Today")
                    }
                    items(notesUpdatedToday, key = { it.id }) { note ->
                        GlassBox(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0xE6FFFFFF),
                            onClick = { onNoteClick(note) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = note.title.ifBlank { "Untitled Note" },
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BodyText
                                    )
                                    if (note.plainText.isNotBlank()) {
                                        Text(
                                            text = note.plainText,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = OutfitFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun StatBadge(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    GlassBox(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = GlassBackground,
        borderColor = GlassBorder
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontFamily = OutfitFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}
