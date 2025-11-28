package com.example.test.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val username: String,
    val bio: String,
    val email: String,
    val followerCount: Int,
    val isFollowingMainUser: Boolean
)
