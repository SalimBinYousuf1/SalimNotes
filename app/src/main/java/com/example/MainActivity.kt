package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.NoteEntity
import com.example.ui.AppScreen
import com.example.ui.SalimViewModel
import com.example.ui.screens.CollectionDetailScreen
import com.example.ui.screens.DailyOverviewScreen
import com.example.ui.screens.GoodMorningScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.YourNotesScreen
import com.example.ui.theme.SalimTheme
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: SalimViewModel = viewModel()
            val isOnboarded by viewModel.isOnboardingCompleted.collectAsState()
            val themeSetting by viewModel.appTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val isDark = when (themeSetting) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            // Check if initial onboarding is needed
            LaunchedEffect(isOnboarded) {
                if (!isOnboarded && currentScreen != AppScreen.ONBOARDING) {
                    viewModel.navigateTo(AppScreen.ONBOARDING)
                }
            }

            // Handle incoming share intent
            LaunchedEffect(intent) {
                handleIncomingShare(intent, viewModel)
            }

            SalimTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    SalimAppContent(viewModel = viewModel)
                }
            }
        }
    }

    private fun handleIncomingShare(intent: Intent?, viewModel: SalimViewModel) {
        if (intent?.action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
            if (text.isNotBlank() || subject.isNotBlank()) {
                val newNote = NoteEntity(
                    id = UUID.randomUUID().toString(),
                    title = subject,
                    body = text,
                    plainText = text
                )
                viewModel.openNote(newNote)
            }
        }
    }
}

@Composable
fun SalimAppContent(viewModel: SalimViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val activeNotes by viewModel.activeNotes.collectAsState()
    val allCollections by viewModel.allCollections.collectAsState()
    val featuredNote by viewModel.featuredNote.collectAsState()
    val recentImages by viewModel.recentImages.collectAsState()
    val checklistsMap by viewModel.checklistsMap.collectAsState()
    val attachmentsMap by viewModel.attachmentsMap.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val selectedCollection by viewModel.selectedCollection.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()
    val todayDueChecklists by viewModel.todayDueChecklists.collectAsState()
    val notesUpdatedToday by viewModel.notesUpdatedToday.collectAsState()
    val currentTheme by viewModel.appTheme.collectAsState()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsState()

    when (currentScreen) {
        AppScreen.ONBOARDING -> {
            OnboardingScreen(
                onFinish = { name ->
                    viewModel.completeOnboarding(name)
                }
            )
        }

        AppScreen.GOOD_MORNING -> {
            GoodMorningScreen(
                userName = userName,
                featuredNote = featuredNote,
                recentImageAttachment = recentImages.firstOrNull(),
                recentPhotoNote = activeNotes.firstOrNull { note ->
                    attachmentsMap[note.id]?.any { it.type == "IMAGE" } == true
                },
                onLogoClick = { viewModel.navigateTo(AppScreen.YOUR_NOTES) },
                onMenuClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                onDailyOverviewClick = { viewModel.navigateTo(AppScreen.DAILY_OVERVIEW) },
                onFeaturedNoteClick = { note -> viewModel.openNote(note) },
                onHighlightsPhotoClick = { photoNote ->
                    if (photoNote != null) viewModel.openNote(photoNote)
                    else viewModel.createNewNote()
                },
                onAddClick = { viewModel.createNewNote() },
                onMicClick = { viewModel.createNewNote() }
            )
        }

        AppScreen.YOUR_NOTES -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.GOOD_MORNING)
            }
            YourNotesScreen(
                notes = activeNotes,
                checklistsMap = checklistsMap,
                attachmentsMap = attachmentsMap,
                onNoteClick = { note -> viewModel.openNote(note) },
                onCollectionClick = { colId, colName ->
                    val col = allCollections.firstOrNull { it.id == colId }
                    if (col != null) viewModel.openCollection(col)
                },
                onAddNoteClick = { viewModel.createNewNote() },
                onMicClick = { viewModel.createNewNote() },
                onToggleChecklistItem = { item -> viewModel.toggleChecklistItem(item) },
                onTogglePin = { noteId -> viewModel.togglePin(noteId) },
                onToggleImportant = { noteId -> viewModel.toggleImportant(noteId) },
                onToggleLock = { noteId -> viewModel.toggleLock(noteId) },
                onArchiveNote = { noteId -> viewModel.archiveNote(noteId) },
                onDeleteNote = { noteId -> viewModel.deleteNote(noteId) },
                onOpenMenu = { viewModel.navigateTo(AppScreen.SETTINGS) },
                onLogoClick = { viewModel.navigateTo(AppScreen.GOOD_MORNING) },
                onSearchClick = { viewModel.navigateTo(AppScreen.SEARCH) }
            )
        }

        AppScreen.COLLECTION_DETAIL -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.YOUR_NOTES)
            }
            selectedCollection?.let { collection ->
                val collectionNotes = activeNotes.filter { it.collectionId == collection.id }
                CollectionDetailScreen(
                    collection = collection,
                    notes = collectionNotes,
                    attachmentsMap = attachmentsMap,
                    onBackClick = { viewModel.navigateTo(AppScreen.YOUR_NOTES) },
                    onNoteClick = { note -> viewModel.openNote(note) },
                    onEditNoteClick = { note -> viewModel.openNote(note) },
                    onCreateNoteInCollection = { viewModel.createNewNote(collection.id) },
                    onDeleteCollection = { viewModel.deleteCollection(collection.id) }
                )
            } ?: run {
                viewModel.navigateTo(AppScreen.YOUR_NOTES)
            }
        }

        AppScreen.DAILY_OVERVIEW -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.GOOD_MORNING)
            }
            DailyOverviewScreen(
                reminders = todayReminders,
                dueChecklists = todayDueChecklists,
                notesUpdatedToday = notesUpdatedToday,
                onToggleChecklistItem = { item -> viewModel.toggleChecklistItem(item) },
                onNoteClick = { note -> viewModel.openNote(note) },
                onBackClick = { viewModel.navigateTo(AppScreen.GOOD_MORNING) }
            )
        }

        AppScreen.NOTE_EDITOR -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.YOUR_NOTES)
            }
            selectedNote?.let { note ->
                val checklists = checklistsMap[note.id] ?: emptyList()
                val attachments = attachmentsMap[note.id] ?: emptyList()
                NoteEditorScreen(
                    initialNote = note,
                    initialChecklists = checklists,
                    initialAttachments = attachments,
                    repository = viewModel.repository,
                    onBackClick = { viewModel.navigateTo(AppScreen.YOUR_NOTES) }
                )
            } ?: run {
                viewModel.navigateTo(AppScreen.YOUR_NOTES)
            }
        }

        AppScreen.SEARCH -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.YOUR_NOTES)
            }
            SearchScreen(
                notes = activeNotes,
                onNoteClick = { note -> viewModel.openNote(note) },
                onBackClick = { viewModel.navigateTo(AppScreen.YOUR_NOTES) }
            )
        }

        AppScreen.SETTINGS -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.YOUR_NOTES)
            }
            SettingsScreen(
                currentName = userName,
                currentTheme = currentTheme,
                isAppLockEnabled = isAppLockEnabled,
                totalStorageBytes = totalStorageBytes,
                dataStore = viewModel.dataStore,
                repository = viewModel.repository,
                onBackClick = { viewModel.navigateTo(AppScreen.YOUR_NOTES) }
            )
        }
    }
}
