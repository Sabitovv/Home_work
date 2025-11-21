
package com.example.test.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val bio: String,
    val followerCount: Int,
    val isFollowingMainUser: Boolean
)
@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val isFollowing: Boolean
)
@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val url: String
)