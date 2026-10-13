package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notes",
    indices = [
        Index("collectionId"),
        Index("isPinned"),
        Index("isImportant"),
        Index("isFeatured"),
        Index("isArchived"),
        Index("deletedAt"),
        Index("updatedAt")
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val plainText: String = "",
    val collectionId: String? = null,
    val color: String? = null,
    val isPinned: Boolean = false,
    val isImportant: Boolean = false,
    val isFeatured: Boolean = false,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId"), Index("position")]
)
data class ChecklistItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int = 0,
    val indent: Int = 0,
    val dueAt: Long? = null
)

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId"), Index("type")]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val type: String, // "IMAGE", "AUDIO", "PDF", "FILE"
    val uri: String,
    val displayName: String = "",
    val mimeType: String = "",
    val sizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val waveform: String = "", // 64 comma-separated integer amplitudes 0..100
    val width: Int = 0,
    val height: Int = 0,
    val position: Int = 0
)

@Entity(
    tableName = "collections",
    indices = [Index("name")]
)
data class CollectionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val coverAttachmentId: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tags"
)
data class TagEntity(
    @PrimaryKey
    val name: String
)

@Entity(
    tableName = "note_tags",
    primaryKeys = ["noteId", "tagName"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagName"), Index("noteId")]
)
data class NoteTagCrossRef(
    val noteId: String,
    val tagName: String
)

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId"), Index("triggerAt"), Index("isDone")]
)
data class ReminderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val triggerAt: Long,
    val repeatRule: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    val isDone: Boolean = false,
    val systemAlarmId: Int = (System.currentTimeMillis() % 1000000).toInt()
)
