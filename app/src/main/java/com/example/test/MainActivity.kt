package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.test.data.AppDatabase
import com.example.test.repository.Follower
import com.example.test.ui.*
import dagger.hilt.android.AndroidEntryPoint
import com.example.test.ui.theme.ProfileViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppScreen()
        }
    }
}

@Composable
fun MainAppScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Feed") },
                    label = { Text("Feed") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) {
                ProfileCardScreen()
            } else {
                FeedScreen()
            }
        }
    }
}

@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val posts by viewModel.posts.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            PostItem(post, onLike = { viewModel.toggleLike(post.id) })
        }
    }
}

@Composable
fun PostItem(post: Post, onLike: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.clip(CircleShape).background(Color.LightGray))
                Spacer(modifier = Modifier.width(8.dp))
                Text(post.username, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content)
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            TextButton(onClick = onLike) {
                Icon(
                    if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${post.likes} Likes")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCardScreen(viewModel: com.example.test.ui.theme.ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showUnfollowDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ProfileUiEvent.ShowUnfollowDialog -> {
                    showUnfollowDialog.value = true
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCardContent(
    state: ProfileUiState,
    onToggleMainUserFollow: () -> Unit,
    onToggleFollowerFollow: (Int, Boolean) -> Unit,
    onConfirmUnfollow: () -> Unit,
    onRefresh: () -> Unit,
    showUnfollowDialog: Boolean,
    onDismissUnfollowDialog: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    if (showUnfollowDialog) {
        AlertDialog(
            onDismissRequest = onDismissUnfollowDialog,
            title = { Text("Отписаться?") },
            text = { Text("Вы уверены, что хотите отписаться от ${state.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmUnfollow()
                        onDismissUnfollowDialog()
                    }
                ) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = onDismissUnfollowDialog) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    Button(onClick = onRefresh) {
                        Text("Refresh")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back press */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(state = state, onToggleFollow = onToggleMainUserFollow)
            Spacer(modifier = Modifier.height(16.dp))

            FollowersList(
                followers = state.followersList,
                onFollowToggle = onToggleFollowerFollow
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Close frends Room.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Followers: ${state.followersList.size}",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
fun ProfileHeader(state: ProfileUiState, onToggleFollow: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Filled.Person, "Profile",
            modifier = Modifier.size(96.dp).clip(CircleShape).background(Color.LightGray).padding(16.dp)
        )
        Text(state.name, style = MaterialTheme.typography.headlineMedium)
        Text(state.bio, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        val buttonColor by animateColorAsState(if (state.isFollowingMainUser) Color.Gray else MaterialTheme.colorScheme.primary)
        Button(onClick = onToggleFollow, colors = ButtonDefaults.buttonColors(containerColor = buttonColor)) {
            Text(if (state.isFollowingMainUser) "Unfollow" else "Follow")
        }
    }
}

@Composable
fun FollowersList(followers: List<Follower>, onFollowToggle: (Int, Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Suggested Followers", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(followers) { follower ->
                FollowerItem(follower, onFollowToggle)
            }
        }
    }
}

@Composable
fun FollowerItem(follower: Follower, onFollowToggle: (Int, Boolean) -> Unit) {
    Card(modifier = Modifier.width(120.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Person, null, modifier = Modifier.size(40.dp))
            Text(follower.name, maxLines = 1, fontSize = 12.sp)
            Button(
                onClick = { onFollowToggle(follower.id, !follower.isFollowing) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(if (follower.isFollowing) "Unfollow" else "Follow", fontSize = 10.sp)
            }
        }
    }
}

data class Follower(
    val id: Long,
    val name: String,
    val isFollowing: Boolean
)
data class ProfileUiState(
    val name: String = "Loading...",
    val bio: String = "",
    val followerCount: Int = 0,
    val isFollowingMainUser: Boolean = false,
    val followersList: List<Follower> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
sealed class ProfileUiEvent {
    data class ShowSnackbar(val message: String) : ProfileUiEvent()
    data object ShowUnfollowDialog : ProfileUiEvent()
}

@Preview(showBackground = true, name = "Profile Content Preview")
@Composable
fun PreviewProfileCardContent() {
    val mockFollowers = listOf(
        Follower(101, "Alix person", false),
        Follower(102, "Sanzhar bot", true),
        Follower(103, "Jekson B", false),
        Follower(104, "Bani I", true),
    )

    val mockState = ProfileUiState(
        name = "Nurgalym (Preview)",
        bio = "Android learner & Compose beginner",
        followerCount = 999,
        isFollowingMainUser = true,
        followersList = mockFollowers
    )
    val dummySnackbarHostState = remember { SnackbarHostState() }

    MaterialTheme {
        ProfileCardContent(
            state = mockState,
            onToggleMainUserFollow = { /* no-op */ },
            onToggleFollowerFollow = { _, _ -> /* no-op */ },
            onConfirmUnfollow = { /* no-op */ },
            onRefresh = { /* no-op */ },
            showUnfollowDialog = false,
            onDismissUnfollowDialog = { /* no-op */ },
            snackbarHostState = dummySnackbarHostState
        )
    }
}
