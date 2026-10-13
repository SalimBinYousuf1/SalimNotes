package com.example.domain.repository

import android.content.Context
import com.example.core.receiver.ReminderReceiver
import com.example.core.security.BiometricHelper
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.CollectionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.UUID

class NotesRepository(private val context: Context) {
    private val db = SalimDatabase.getDatabase(context)
    private val noteDao = db.noteDao()
    private val checklistDao = db.checklistDao()
    private val attachmentDao = db.attachmentDao()
    private val collectionDao = db.collectionDao()
    private val reminderDao = db.reminderDao()

    val allActiveNotes: Flow<List<NoteEntity>> = noteDao.getAllActiveNotes()
    val allCollections: Flow<List<CollectionEntity>> = collectionDao.getAllCollections()
    val featuredNote: Flow<NoteEntity?> = noteDao.getFeaturedNote()
    val trashNotes: Flow<List<NoteEntity>> = noteDao.getTrashNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    val recentImages: Flow<List<AttachmentEntity>> = attachmentDao.getRecentImageAttachments()
    val totalStorageBytes: Flow<Long?> = attachmentDao.getTotalStorageBytes()

    fun getNotesByCollection(collectionId: String): Flow<List<NoteEntity>> =
        noteDao.getNotesByCollection(collectionId)

    fun getNoteById(id: String): Flow<NoteEntity?> = noteDao.getNoteById(id)

    suspend fun getNoteByIdSync(id: String): NoteEntity? = noteDao.getNoteByIdSync(id)

    fun getChecklistForNote(noteId: String): Flow<List<ChecklistItemEntity>> =
        checklistDao.getItemsForNote(noteId)

    suspend fun getChecklistForNoteSync(noteId: String): List<ChecklistItemEntity> =
        checklistDao.getItemsForNoteSync(noteId)

    fun getAttachmentsForNote(noteId: String): Flow<List<AttachmentEntity>> =
        attachmentDao.getAttachmentsForNote(noteId)

    suspend fun getAttachmentsForNoteSync(noteId: String): List<AttachmentEntity> =
        attachmentDao.getAttachmentsForNoteSync(noteId)

    fun getRemindersForNote(noteId: String): Flow<List<ReminderEntity>> =
        reminderDao.getRemindersForNote(noteId)

    fun searchNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.searchNotes(query)

    // Today Overview stats
    fun getTodayReminders(): Flow<List<ReminderEntity>> {
        val (startOfDay, endOfDay) = getTodayBounds()
        return reminderDao.getTodayReminders(startOfDay, endOfDay)
    }

    fun getTodayDueChecklistItems(): Flow<List<ChecklistItemEntity>> {
        val (_, endOfDay) = getTodayBounds()
        return checklistDao.getDueItems(endOfDay)
    }

    fun getNotesUpdatedToday(): Flow<List<NoteEntity>> {
        val (startOfDay, _) = getTodayBounds()
        return noteDao.getNotesEditedSince(startOfDay)
    }

    private fun getTodayBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    suspend fun saveNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        val existing = noteDao.getNoteByIdSync(note.id)
        if (existing == null) {
            noteDao.insertNote(note)
        } else {
            noteDao.updateNote(note)
        }
        if (note.isFeatured) {
            noteDao.clearOtherFeatured(note.id)
        }
    }

    suspend fun togglePin(noteId: String) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext
        noteDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleImportant(noteId: String) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext
        noteDao.updateNote(note.copy(isImportant = !note.isImportant, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFeatured(noteId: String) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext
        val newFeatured = !note.isFeatured
        if (newFeatured) {
            noteDao.clearOtherFeatured(noteId)
        }
        noteDao.updateNote(note.copy(isFeatured = newFeatured, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleArchived(noteId: String) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext
        noteDao.updateNote(note.copy(isArchived = !note.isArchived, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleLock(noteId: String) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext
        val newLocked = !note.isLocked
        val newBody = if (newLocked) {
            BiometricHelper.encrypt(note.body)
        } else {
            BiometricHelper.decrypt(note.body)
        }
        noteDao.updateNote(note.copy(isLocked = newLocked, body = newBody, updatedAt = System.currentTimeMillis()))
    }

    suspend fun moveToCollection(noteId: String, collectionId: String?) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdSync(noteId) ?: return@withContext
        noteDao.updateNote(note.copy(collectionId = collectionId, updatedAt = System.currentTimeMillis()))
    }

    suspend fun moveToTrash(noteId: String) = withContext(Dispatchers.IO) {
        noteDao.moveToTrash(noteId)
    }

    suspend fun restoreFromTrash(noteId: String) = withContext(Dispatchers.IO) {
        noteDao.restoreFromTrash(noteId)
    }

    suspend fun deletePermanently(noteId: String) = withContext(Dispatchers.IO) {
        // Delete attachment files from storage
        val attachments = attachmentDao.getAttachmentsForNoteSync(noteId)
        for (att in attachments) {
            val file = File(att.uri)
            if (file.exists()) file.delete()
        }
        val noteDir = File(File(context.filesDir, "attachments"), noteId)
        if (noteDir.exists()) noteDir.deleteRecursively()

        // Cancel scheduled alarms
        reminderDao.deleteRemindersForNote(noteId)

        // Delete note
        noteDao.deletePermanently(noteId)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        noteDao.emptyAllTrash()
    }

    // Checklist operations
    suspend fun saveChecklistItems(noteId: String, items: List<ChecklistItemEntity>) = withContext(Dispatchers.IO) {
        checklistDao.deleteItemsForNote(noteId)
        checklistDao.insertItems(items)
    }

    suspend fun toggleChecklistItem(item: ChecklistItemEntity) = withContext(Dispatchers.IO) {
        checklistDao.updateItem(item.copy(isChecked = !item.isChecked))
    }

    // Attachment operations
    suspend fun addAttachment(attachment: AttachmentEntity) = withContext(Dispatchers.IO) {
        attachmentDao.insertAttachment(attachment)
    }

    suspend fun removeAttachment(attachment: AttachmentEntity) = withContext(Dispatchers.IO) {
        val file = File(attachment.uri)
        if (file.exists()) file.delete()
        attachmentDao.deleteAttachment(attachment.id)
    }

    // Collections
    suspend fun createCollection(name: String): CollectionEntity = withContext(Dispatchers.IO) {
        val col = CollectionEntity(id = UUID.randomUUID().toString(), name = name)
        collectionDao.insertCollection(col)
        col
    }

    suspend fun updateCollection(collection: CollectionEntity) = withContext(Dispatchers.IO) {
        collectionDao.updateCollection(collection)
    }

    suspend fun deleteCollection(id: String) = withContext(Dispatchers.IO) {
        collectionDao.deleteCollection(id)
    }

    // Reminders
    suspend fun addReminder(noteId: String, triggerAt: Long, repeatRule: String = "NONE"): ReminderEntity = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteByIdSync(noteId)
        val title = note?.title?.ifBlank { "Note Reminder" } ?: "Note Reminder"
        val reminder = ReminderEntity(
            id = UUID.randomUUID().toString(),
            noteId = noteId,
            triggerAt = triggerAt,
            repeatRule = repeatRule
        )
        reminderDao.insertReminder(reminder)
        ReminderReceiver.scheduleAlarm(context, reminder.id, noteId, title, triggerAt)
        reminder
    }

    suspend fun removeReminder(reminderId: String) = withContext(Dispatchers.IO) {
        ReminderReceiver.cancelAlarm(context, reminderId)
        reminderDao.deleteReminder(reminderId)
    }
}
