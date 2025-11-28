package com.example.test.repository

import com.example.test.data.UserEntity
import com.example.test.data.entities.ProfileDao
import com.example.test.data.entities.FollowerEntity
import com.example.test.data.entities.StoryEntity
import com.example.test.test.ApiService

import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: ApiService,
    private val profileDao: ProfileDao
) {
    suspend fun getUserFromApi(): UserEntity = api.getUser()

    suspend fun getLocalUser(): UserEntity? = profileDao.getUser()

    suspend fun saveUser(user: UserEntity) = profileDao.insertUser(user)
}