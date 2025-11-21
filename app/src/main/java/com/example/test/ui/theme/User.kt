package com.example.test.ui.theme
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "user_profile", primaryKeys = ["id"])
data class User(
    val id: Int,
    val name: String,
    val bio: String,
    val followerCount: Int
)
@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
}

@Entity(tableName = "followers", primaryKeys = ["id"])
data class FollowerEntity(
    val id: Long,
    val name: String,
    val isFollowing: Boolean
)