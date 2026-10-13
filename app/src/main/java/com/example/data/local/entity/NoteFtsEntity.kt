package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(
    @PrimaryKey
    val rowid: Int = 0,
    val noteId: String,
    val title: String,
    val content: String
)
