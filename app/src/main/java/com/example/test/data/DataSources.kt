package com.example.test.data

import androidx.room.*
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<ApiUser>
}

data class ApiUser(val id: Int, val name: String)

@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val isFollowing: Boolean
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: Int,
    val name: String
)

@Entity(tableName = "profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val bio: String,
    val followerCount: Int,
    val isFollowingMainUser: Boolean
)

@Database(
    version = 1,
    entities = [FollowerEntity::class, StoryEntity::class, UserEntity::class],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ProfileDao
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM followers")
    suspend fun getFollowers(): List<FollowerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowers(list: List<FollowerEntity>)

    @Query("SELECT * FROM stories")
    suspend fun getStories(): List<StoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(list: List<StoryEntity>)

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}