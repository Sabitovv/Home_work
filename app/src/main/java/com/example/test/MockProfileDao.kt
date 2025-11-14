package com.example.test

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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