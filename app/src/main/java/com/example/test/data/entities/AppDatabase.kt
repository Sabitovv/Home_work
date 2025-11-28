package com.example.test.data.entities

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.test.data.UserEntity

@Database(entities = [UserEntity::class, FollowerEntity::class, StoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}