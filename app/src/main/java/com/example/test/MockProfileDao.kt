package com.example.test

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.data.FollowerEntity
import com.example.test.data.StoryEntity
import com.example.test.data.UserEntity
import retrofit2.http.GET
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.test.data.ProfileDao

@Dao
//interface ProfileDao {
//
//    @Query("SELECT * FROM followers")
//    suspend fun getFollowers(): List<FollowerEntity>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertFollowers(list: List<FollowerEntity>)
//
//    @Query("SELECT * FROM stories")
//    suspend fun getStories(): List<StoryEntity>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertStories(list: List<StoryEntity>)
//
//    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
//    suspend fun getUser(): UserEntity?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertUser(user: UserEntity)
//}

class MockProfileDao : ProfileDao {
    private val dummyUser = UserEntity(
        id = 1,
        name = "Nurgalym (Preview)",
        bio = "Android learner & Compose beginner",
        followerCount = 999,
        isFollowingMainUser = true
    )
    private val dummyFollowers = listOf(
        FollowerEntity(101, "Alix person", false),
        FollowerEntity(102, "Sanzhar bot", true),
        FollowerEntity(103, "Jekson B", false),
        FollowerEntity(104, "Bani I", true),
    )

    override suspend fun getFollowers(): List<FollowerEntity> = dummyFollowers
    override suspend fun insertFollowers(list: List<FollowerEntity>) {}
    override suspend fun getStories(): List<StoryEntity> = emptyList()
    override suspend fun insertStories(list: List<StoryEntity>) {}
    override suspend fun getUser(): UserEntity = dummyUser
    override suspend fun insertUser(user: UserEntity) {}
}
interface ApiService {
    @GET("user/profile")
    suspend fun getUser(): UserEntity
}

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