package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.CollectionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 0 ORDER BY isImportant DESC, isPinned DESC, updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 0 ORDER BY isImportant DESC, isPinned DESC, updatedAt DESC")
    suspend fun getAllActiveNotesSync(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteByIdSync(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE collectionId = :collectionId AND deletedAt IS NULL AND isArchived = 0 ORDER BY isImportant DESC, isPinned DESC, updatedAt DESC")
    fun getNotesByCollection(collectionId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isFeatured = 1 AND deletedAt IS NULL LIMIT 1")
    fun getFeaturedNote(): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND (title LIKE '%' || :query || '%' OR plainText LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND updatedAt >= :sinceTimestamp ORDER BY updatedAt DESC")
    fun getNotesEditedSince(sinceTimestamp: Long): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET deletedAt = :deletedAt WHERE id = :id")
    suspend fun moveToTrash(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt <= :retentionTimestamp")
    suspend fun purgeTrash(retentionTimestamp: Long)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL")
    suspend fun emptyAllTrash()

    @Query("UPDATE notes SET isFeatured = 0 WHERE id != :noteId")
    suspend fun clearOtherFeatured(noteId: String)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY isChecked ASC, position ASC")
    fun getItemsForNote(noteId: String): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE noteId = :noteId ORDER BY isChecked ASC, position ASC")
    suspend fun getItemsForNoteSync(noteId: String): List<ChecklistItemEntity>

    @Query("SELECT * FROM checklist_items WHERE isChecked = 0 AND dueAt IS NOT NULL AND dueAt <= :endOfDay ORDER BY dueAt ASC")
    fun getDueItems(endOfDay: Long): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("DELETE FROM checklist_items WHERE noteId = :noteId")
    suspend fun deleteItemsForNote(noteId: String)
}

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY position ASC")
    fun getAttachmentsForNote(noteId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE noteId = :noteId ORDER BY position ASC")
    suspend fun getAttachmentsForNoteSync(noteId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE type = :type ORDER BY id DESC")
    fun getAttachmentsByType(type: String): Flow<List<AttachmentEntity>>

    @Query("SELECT a.* FROM attachments a INNER JOIN notes n ON a.noteId = n.id WHERE a.type = 'IMAGE' AND n.deletedAt IS NULL ORDER BY n.updatedAt DESC")
    fun getRecentImageAttachments(): Flow<List<AttachmentEntity>>

    @Query("SELECT SUM(sizeBytes) FROM attachments")
    fun getTotalStorageBytes(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteAttachment(id: String)

    @Query("DELETE FROM attachments WHERE noteId = :noteId")
    suspend fun deleteAttachmentsForNote(noteId: String)
}

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    fun getCollectionById(id: String): Flow<CollectionEntity?>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun getCollectionByIdSync(id: String): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity)

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollection(id: String)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isDone = 0 ORDER BY triggerAt ASC")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE noteId = :noteId")
    fun getRemindersForNote(noteId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isDone = 0 AND triggerAt >= :startOfDay AND triggerAt <= :endOfDay ORDER BY triggerAt ASC")
    fun getTodayReminders(startOfDay: Long, endOfDay: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isDone = 0 AND triggerAt > :now ORDER BY triggerAt ASC")
    suspend fun getPendingUpcomingReminders(now: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isDone = 1 WHERE id = :id")
    suspend fun markDone(id: String)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteRemindersForNote(noteId: String)
}
