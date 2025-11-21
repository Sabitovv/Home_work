package com.example.test.repository

import com.example.test.ProfileUiState
import com.example.test.data.*
import javax.inject.Inject

data class Follower(val id: Int, val name: String, val isFollowing: Boolean)

class ProfileRepository @Inject constructor(
    private val dao: ProfileDao,
    private val api: ApiService
) {
    suspend fun loadInitialData(): ProfileUiState {
        val user = dao.getUser()
        val followers = dao.getFollowers()

        return if (user != null) {
            ProfileUiState(
                name = user.name,
                bio = user.bio,
                followerCount = user.followerCount,
                isFollowingMainUser = user.isFollowingMainUser,
                followersList = followers.map { Follower(it.id, it.name, it.isFollowing) }
            )
        } else {
            ProfileUiState(followersList = emptyList())
        }
    }

    suspend fun refreshFromApi() {
        try {
            val apiUsers = api.getUsers()
            dao.insertFollowers(apiUsers.take(7).map { FollowerEntity(it.id, it.name, false) })
            dao.insertStories(apiUsers.take(8).map { StoryEntity(it.id, "S${it.id}") })

            val existing = dao.getUser()
            if (existing == null) {
                dao.insertUser(
                    UserEntity(
                        id = 1, name = "Nurgalym", bio = "Android learner",
                        followerCount = 1200, isFollowingMainUser = false
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveState(state: ProfileUiState) {
        dao.insertUser(
            UserEntity(
                id = 1, name = state.name, bio = state.bio,
                followerCount = state.followerCount, isFollowingMainUser = state.isFollowingMainUser
            )
        )
        dao.insertFollowers(
            state.followersList.map { FollowerEntity(it.id, it.name, it.isFollowing) }
        )
    }

//    suspend fun saveState(state: com.example.test.ui.ProfileUiState) {
//        dao.insertUser(
//            UserEntity(
//                id = 1, name = state.name, bio = state.bio,
//                followerCount = state.followerCount, isFollowingMainUser = state.isFollowingMainUser
//            )
//        )
//        dao.insertFollowers(
//            state.followersList.map { FollowerEntity(it.id, it.name, it.isFollowing) }
//        )
//    }
}