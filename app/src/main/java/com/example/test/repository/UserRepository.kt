package com.example.test.repository

import com.example.test.ApiService
import com.example.test.data.ProfileDao
import com.example.test.data.UserEntity
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: ApiService,
    private val profileDao: ProfileDao
) {
    suspend fun getUserFromApi(): UserEntity = api.getUser()

    suspend fun getLocalUser(): UserEntity? = profileDao.getUser()

    suspend fun saveUser(user: UserEntity) = profileDao.insertUser(user)
}