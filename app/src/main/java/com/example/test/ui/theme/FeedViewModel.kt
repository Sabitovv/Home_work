package com.example.test.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class Post(
    val id: Int,
    val username: String,
    val content: String,
    val likes: Int,
    val isLiked: Boolean
)

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    private val _posts = MutableStateFlow(
        listOf(
            Post(1, "Nurgalym", "Hilt makes DI so much easier! #AndroidDev", 45, false),
            Post(2, "ComposeFan", "Just built a custom layout. It's fluid.", 120, true),
            Post(3, "TechDaily", "New libraries released today.", 890, false)
        )
    )
    val posts = _posts.asStateFlow()

    fun toggleLike(postId: Int) {
        _posts.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    post.copy(
                        isLiked = !post.isLiked,
                        likes = if (post.isLiked) post.likes - 1 else post.likes + 1
                    )
                } else post
            }
        }
    }
}