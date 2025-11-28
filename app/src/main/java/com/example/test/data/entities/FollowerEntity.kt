package com.example.test.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val isFollowing: Boolean
)