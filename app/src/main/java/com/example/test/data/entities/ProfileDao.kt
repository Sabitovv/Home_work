package com.example.test.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test.data.UserEntity

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

    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getUser(): UserEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}