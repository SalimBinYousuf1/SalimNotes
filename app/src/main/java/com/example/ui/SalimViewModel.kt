package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SalimDataStore
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.CollectionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.ReminderEntity
import com.example.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    ONBOARDING,
    GOOD_MORNING,
    YOUR_NOTES,
    COLLECTION_DETAIL,
    DAILY_OVERVIEW,
    NOTE_EDITOR,
    SEARCH,
    SETTINGS
}

class SalimViewModel(application: Application) : AndroidViewModel(application) {
    val repository = NotesRepository(application)
    val dataStore = SalimDataStore(application)

    val isOnboardingCompleted = dataStore.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val userName = dataStore.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val appTheme = dataStore.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val isAppLockEnabled = dataStore.isAppLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val totalStorageBytes = repository.totalStorageBytes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val activeNotes = repository.allActiveNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCollections = repository.allCollections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredNote = repository.featuredNote
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentImages = repository.recentImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayReminders = repository.getTodayReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDueChecklists = repository.getTodayDueChecklistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notesUpdatedToday = repository.getNotesUpdatedToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(AppScreen.GOOD_MORNING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    private val _selectedCollection = MutableStateFlow<CollectionEntity?>(null)
    val selectedCollection: StateFlow<CollectionEntity?> = _selectedCollection.asStateFlow()

    private val _checklistsMap = MutableStateFlow<Map<String, List<ChecklistItemEntity>>>(emptyMap())
    val checklistsMap: StateFlow<Map<String, List<ChecklistItemEntity>>> = _checklistsMap.asStateFlow()

    private val _attachmentsMap = MutableStateFlow<Map<String, List<AttachmentEntity>>>(emptyMap())
    val attachmentsMap: StateFlow<Map<String, List<AttachmentEntity>>> = _attachmentsMap.asStateFlow()

    init {
        // Collect checklists & attachments when activeNotes update
        viewModelScope.launch {
            activeNotes.collect { notes ->
                val newChecklistMap = mutableMapOf<String, List<ChecklistItemEntity>>()
                val newAttachmentMap = mutableMapOf<String, List<AttachmentEntity>>()
                for (note in notes) {
                    newChecklistMap[note.id] = repository.getChecklistForNoteSync(note.id)
                    newAttachmentMap[note.id] = repository.getAttachmentsForNoteSync(note.id)
                }
                _checklistsMap.value = newChecklistMap
                _attachmentsMap.value = newAttachmentMap
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openNote(note: NoteEntity) {
        _selectedNote.value = note
        _currentScreen.value = AppScreen.NOTE_EDITOR
    }

    fun createNewNote(collectionId: String? = null) {
        val newNote = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = "",
            body = "",
            plainText = "",
            collectionId = collectionId
        )
        _selectedNote.value = newNote
        _currentScreen.value = AppScreen.NOTE_EDITOR
    }

    fun openCollection(collection: CollectionEntity) {
        _selectedCollection.value = collection
        _currentScreen.value = AppScreen.COLLECTION_DETAIL
    }

    fun toggleChecklistItem(item: ChecklistItemEntity) {
        viewModelScope.launch {
            repository.toggleChecklistItem(item)
            // Refresh checklist map
            val updated = repository.getChecklistForNoteSync(item.noteId)
            _checklistsMap.value = _checklistsMap.value.toMutableMap().apply { put(item.noteId, updated) }
        }
    }

    fun togglePin(noteId: String) = viewModelScope.launch { repository.togglePin(noteId) }
    fun toggleImportant(noteId: String) = viewModelScope.launch { repository.toggleImportant(noteId) }
    fun toggleFeatured(noteId: String) = viewModelScope.launch { repository.toggleFeatured(noteId) }
    fun toggleLock(noteId: String) = viewModelScope.launch { repository.toggleLock(noteId) }
    fun archiveNote(noteId: String) = viewModelScope.launch { repository.toggleArchived(noteId) }
    fun deleteNote(noteId: String) = viewModelScope.launch { repository.moveToTrash(noteId) }

    fun deleteCollection(collectionId: String) = viewModelScope.launch {
        repository.deleteCollection(collectionId)
        _currentScreen.value = AppScreen.YOUR_NOTES
    }

    fun completeOnboarding(name: String) = viewModelScope.launch {
        dataStore.setUserName(name)
        dataStore.setOnboardingCompleted(true)
        _currentScreen.value = AppScreen.GOOD_MORNING
    }
}
