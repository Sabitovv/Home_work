package com.example.test

import com.example.test.data.entities.FollowerEntity
import com.example.test.data.entities.ProfileDao
import com.example.test.data.entities.StoryEntity
import com.example.test.data.UserEntity
class MockProfileDao : ProfileDao {

    private val dummyUser = UserEntity(
        id = 1,
        name = "Nurgalym (Preview)",
        username = "Nurgalym",
        bio = "Android learner & Compose beginner",
        followerCount = 999,
        isFollowingMainUser = true,
        email = "charlescurtis@myownpersonaldomain.com"

    )

    private val dummyFollowers = listOf(
        FollowerEntity(101, "Alix person", false),
        FollowerEntity(102, "Sanzhar bot", true),
        FollowerEntity(103, "Jekson B", false),
        FollowerEntity(104, "Bani I", true),
    )

    override suspend fun getFollowers(): List<FollowerEntity> {
        return dummyFollowers
    }

    override suspend fun insertFollowers(list: List<FollowerEntity>) {

    }

    override suspend fun getStories(): List<StoryEntity> {
        return emptyList()
    }

    override suspend fun insertStories(list: List<StoryEntity>) {

    }

    override suspend fun getUser(): UserEntity {
        return dummyUser
    }

    override suspend fun insertUser(user: UserEntity) {

    }
}
