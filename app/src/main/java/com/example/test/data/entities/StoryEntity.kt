package com.example.test.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: Int,
    val imageUrl: String,
    val author: String
)