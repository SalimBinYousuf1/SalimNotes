package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.GlassBox
import com.example.ui.components.TextNoteCard
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.notesGradient

@Composable
fun SearchScreen(
    notes: List<NoteEntity>,
    onNoteClick: (NoteEntity) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val searchResults = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            notes.filter { note ->
                note.title.lowercase().contains(q) || note.plainText.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(notesGradient())
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar with Search Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassBox(
                    modifier = Modifier.size(44.dp),
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

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search notes...", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                            }
                        }
                    },
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        focusedContainerColor = Color(0x33FFFFFF),
                        unfocusedContainerColor = Color(0x22FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_notes_input")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Type to search your notes and thoughts",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes matched '$searchQuery'",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(searchResults, key = { it.id }) { note ->
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
