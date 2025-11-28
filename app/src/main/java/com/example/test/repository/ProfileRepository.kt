package com.example.test.repository

import com.example.test.ProfileUiState
import com.example.test.data.ApiService
import com.example.test.data.UserEntity
import com.example.test.data.entities.FollowerEntity
import com.example.test.data.entities.ProfileDao
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

                bio = "Email: ${user.email}",
                followerCount = followers.size,
                isFollowingMainUser = (user.id % 2 == 0),
                followersList = followers.map { follower ->
                    Follower(follower.id, follower.name, follower.isFollowing)
                }
            )
        } else {
            ProfileUiState()
        }
    }


    suspend fun refreshFromApi() {
        try {
            val apiUser = api.getUser(1)
            val mockFollowers = listOf(
                FollowerEntity(101, "follower_one", false),
                FollowerEntity(102, "follower_two", true)
            )
            dao.insertFollowers(mockFollowers)

            val existingUser = dao.getUser()
            if (existingUser == null || existingUser.id != apiUser.id) {
                dao.insertUser(apiUser)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveState(state: ProfileUiState) {
        val userToSave = UserEntity(
            id = 1,
            name = state.name,
            bio = state.bio,
            username = state.name.replace(" ", "").toLowerCase(),
            email = state.bio,
            followerCount = state.followerCount,
            isFollowingMainUser = state.isFollowingMainUser
        )
        dao.insertUser(userToSave)

        dao.insertFollowers(
            state.followersList.map { follower ->
                FollowerEntity(follower.id, follower.name, follower.isFollowing)
            }
        )
    }
}
