

package com.example.test

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.test.data.UserEntity
import com.example.test.data.FollowerEntity
import com.example.test.data.ProfileDao
import com.example.test.data.StoryEntity

@Database(
    entities = [UserEntity::class, FollowerEntity::class, StoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ProfileDao
}