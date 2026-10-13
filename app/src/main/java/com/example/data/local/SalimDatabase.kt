package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AttachmentDao
import com.example.data.local.dao.ChecklistDao
import com.example.data.local.dao.CollectionDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.ChecklistItemEntity
import com.example.data.local.entity.CollectionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NoteFtsEntity
import com.example.data.local.entity.NoteTagCrossRef
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.TagEntity

@Database(
    entities = [
        NoteEntity::class,
        ChecklistItemEntity::class,
        AttachmentEntity::class,
        CollectionEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
        ReminderEntity::class,
        NoteFtsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SalimDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun collectionDao(): CollectionDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: SalimDatabase? = null

        fun getDatabase(context: Context): SalimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalimDatabase::class.java,
                    "salim_notes.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
