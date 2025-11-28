package com.example.test.data

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: Int): UserEntity
}
