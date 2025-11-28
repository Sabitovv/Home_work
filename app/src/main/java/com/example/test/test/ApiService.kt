
package com.example.test.test

import com.example.test.data.UserEntity
import retrofit2.http.GET

interface ApiService {
    @GET("user/profile")
    suspend fun getUser(): UserEntity
}